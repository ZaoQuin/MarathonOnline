package com.university.marathononline.ui.view.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.record.RecordApiService
import com.university.marathononline.data.api.registration.RegistrationApiService
import com.university.marathononline.data.api.training.TrainingDayApiService
import com.university.marathononline.data.models.ERecordSource
import com.university.marathononline.data.models.WearHealthData
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.data.repository.RegistrationRepository
import com.university.marathononline.data.repository.TrainingDayRepository
import com.university.marathononline.data.api.record.CreateRecordRequest
import com.university.marathononline.databinding.ActivityRecordBinding
import com.university.marathononline.service.foreground.RunningForegroundService
import com.university.marathononline.ui.components.ModeSelectionDialog
import com.university.marathononline.ui.view.fragment.GuidedModeFragment
import com.university.marathononline.ui.viewModel.RecordViewModel
import com.university.marathononline.utils.FgRecordConstants
import com.university.marathononline.utils.enable
import com.university.marathononline.utils.finishAndGoBack
import com.university.marathononline.utils.visible
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class RecordActivity : BaseActivity<RecordViewModel, ActivityRecordBinding>(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var currentMarker: Marker? = null
    private var polyline: Polyline? = null
    private val handler = Handler(Looper.getMainLooper())

    private var guidedModeFragment: GuidedModeFragment? = null

    private var isSavingRecord = false
    private var saveRecordCompleted = false
    private var saveRegistrationCompleted = false
    private var saveTrainingDayCompleted = false
    private var hasProcessedStopped = false

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted && coarseLocationGranted) {
            checkActivityRecognitionPermission()
        } else {
            Toast.makeText(
                this,
                "Ứng dụng cần quyền vị trí để hoạt động bình thường.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val runningUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                FgRecordConstants.RUNNING_UPDATE -> {
                    intent.let {
                        Log.d("RecordActivity", "Received RUNNING_UPDATE")
                        val isStopping = it.getBooleanExtra("isStopping", false)

                        if (isStopping) {
                            Log.d("RecordActivity", "Service is stopping, ignoring update")
                            binding.playButton.visible(true)
                            binding.stopButton.visible(false)
                            return
                        }

                        binding.tvTime.text = it.getStringExtra(FgRecordConstants.time) ?: "--:--:--"
                        binding.tvDistance.text = it.getStringExtra(FgRecordConstants.distance) ?: "- km"
                        binding.tvPace.text = it.getStringExtra(FgRecordConstants.pace) ?: "-- min/km"
                        val isRecording = it.getBooleanExtra(FgRecordConstants.isRecording, false)
                        val isPaused = it.getBooleanExtra(FgRecordConstants.isPaused, false)

                        if (!isSavingRecord && !hasProcessedStopped) {
                            binding.playButton.visible(!isRecording || isPaused)
                            binding.stopButton.visible(isRecording && !isPaused)
                        }
                    }
                }
                FgRecordConstants.RUNNING_STOPPED -> {
                    if (!hasProcessedStopped) {
                        hasProcessedStopped = true
                        intent.let {
                            Log.d("RecordActivity", "Received RUNNING_STOPPED, hasProcessedStopped=$hasProcessedStopped")

                            binding.playButton.visible(false)
                            binding.stopButton.visible(false)

                            viewModel.forceStopRecording()

                            val steps = it.getIntExtra(FgRecordConstants.steps, 0)
                            val distance = it.getDoubleExtra(FgRecordConstants.distance, 0.0)
                            val avgSpeed = it.getDoubleExtra(FgRecordConstants.avgSpeed, 0.0)
                            val startTime = it.getStringExtra(FgRecordConstants.startTime) ?: LocalDateTime.now().toString()
                            val endTime = it.getStringExtra(FgRecordConstants.endTime) ?: LocalDateTime.now().toString()

                            val createRecordRequest = CreateRecordRequest(
                                steps = steps,
                                distance = distance,
                                avgSpeed = avgSpeed,
                                heartRate = 0.0,
                                startTime = startTime,
                                endTime = endTime,
                                source = ERecordSource.DEVICE
                            )
                            viewModel.createRecord(createRecordRequest)
                        }
                    }
                }
            }
        }
    }

    private val activityRecognitionPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            binding.playButton.enable(true)
        } else {
            Toast.makeText(
                this,
                "Ứng dụng cần quyền nhận dạng hoạt động để hoạt động bình thường.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.initializeLocationTracking(this)
        viewModel.initializeWearIntegration()

        if (savedInstanceState != null) {
            isSavingRecord = savedInstanceState.getBoolean(FgRecordConstants.isSavingRecord, false)
        } else {
            val prefs = getSharedPreferences("RunningData", Context.MODE_PRIVATE)
            if (prefs.contains(FgRecordConstants.steps) && !isSavingRecord) {
                val steps = prefs.getInt(FgRecordConstants.steps, 0)
                val distance = prefs.getFloat(FgRecordConstants.distance, 0f).toDouble()
                val avgSpeed = prefs.getFloat(FgRecordConstants.avgSpeed, 0f).toDouble()
                val startTime = prefs.getString(FgRecordConstants.startTime, LocalDateTime.now().toString()) ?: LocalDateTime.now().toString()
                val endTime = prefs.getString(FgRecordConstants.endTime, LocalDateTime.now().toString()) ?: LocalDateTime.now().toString()

                val createRecordRequest = CreateRecordRequest(
                    steps = steps,
                    distance = distance,
                    avgSpeed = avgSpeed,
                    heartRate = 0.0,
                    startTime = startTime,
                    endTime = endTime,
                    source = ERecordSource.DEVICE
                )
                viewModel.createRecord(createRecordRequest)
                prefs.edit().clear().apply()
            }

            viewModel.restoreRunningData(this)?.let { request ->
                if (!isSavingRecord) {
                    viewModel.createRecord(request)
                    Log.d("RecordActivity", "Restored and saved record from RecordPreferences")
                }
            }
        }

        showModeSelectionDialog()
        initializeUI()
        setupObservers()
        checkAndRequestPermissions()

        val filter = IntentFilter().apply {
            addAction(FgRecordConstants.RUNNING_UPDATE)
            addAction(FgRecordConstants.RUNNING_STOPPED)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(runningUpdateReceiver, filter)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(FgRecordConstants.isSavingRecord, isSavingRecord)
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.time.collect { binding.tvTime.text = it } }
                launch {
                    viewModel.averagePace.collect { pace ->
                        binding.tvPace.text = pace
                        guidedModeFragment?.updateCurrentStats(
                            pace,
                            binding.tvDistance.text.toString()
                        )
                    }
                }
                launch {
                    viewModel.distance.collect { distance ->
                        binding.tvDistance.text = distance
                        guidedModeFragment?.updateCurrentStats(
                            binding.tvPace.text.toString(),
                            distance
                        )
                    }
                }

                launch {
                    viewModel.isWearConnected.collect { isConnected ->
                        updateWearConnectionStatus(isConnected)
                        updateButtonVisibility(isConnected)

                        handleMapTrackingMode(isConnected)
                    }
                }

                launch {
                    viewModel.wearHealthData.collect { wearData ->
                        wearData?.let {
                            updateUIWithWearData(it)
                        }
                    }
                }

                launch {
                    viewModel.isRecording.collect { isRecording ->
                        val isWearConnected = viewModel.isWearConnected.value
                        if (!isSavingRecord && !hasProcessedStopped) {
                            if (!isWearConnected) {
                                binding.playButton.visible(!isRecording)
                                binding.stopButton.visible(isRecording)
                            } else {
                                binding.playButton.visible(false)
                                binding.stopButton.visible(false)
                            }
                        }
                    }
                }
            }
        }

        viewModel.createRecordResponse.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    Toast.makeText(this, "Đang lưu kết quả...", Toast.LENGTH_SHORT).show()
                    startSavingProcess()
                }
                is Resource.Success -> {
                    saveRecordCompleted = true
                    checkSavingComplete()
                    viewModel.saveRecordIntoRegistration(it.value)
                    viewModel.saveRecordIntoTrainingDay(it.value)
                    viewModel.clearLocal()
                }
                is Resource.Failure -> {
                    completeSavingProcess()
                    handleApiError(it)
                }
                else -> Unit
            }
        }

        viewModel.saveRecordIntoRegistration.observe(this) {
            when (it) {
                is Resource.Success -> {
                    saveRegistrationCompleted = true
                    checkSavingComplete()
                    Toast.makeText(this, "Lưu vào đăng ký thành công", Toast.LENGTH_SHORT).show()
                }
                is Resource.Failure -> {
                    completeSavingProcess()
                    handleApiError(it)
                }
                else -> Unit
            }
        }

        viewModel.saveRecordIntoTrainingDay.observe(this) {
            when (it) {
                is Resource.Success -> {
                    saveTrainingDayCompleted = true
                    checkSavingComplete()
                    Toast.makeText(this, "Lưu vào kế hoạch tập luyện thành công", Toast.LENGTH_SHORT).show()
                }
                is Resource.Failure -> {
                    completeSavingProcess()
                    handleApiError(it)
                }
                else -> Unit
            }
        }

        viewModel.isGPSEnabled.observe(this, Observer { isEnabled ->
            if (isEnabled) {
                binding.checkGPS.text = "GPS có sẵn"
                handler.postDelayed({
                    binding.checkGPS.visible(false)
                    binding.recordLayout.visible(true)
                    if (!isSavingRecord) {
                        binding.playButton.enable(true)
                    }
                }, 3000)
            } else {
                binding.playButton.enable(false)
                binding.checkGPS.text = "GPS không có sẵn"
            }
        })
    }

    private fun startSavingProcess() {
        isSavingRecord = true
        saveRecordCompleted = false
        saveRegistrationCompleted = false
        saveTrainingDayCompleted = false

        updateUIForSaving(true)
    }

    private fun checkSavingComplete() {
        if (saveRecordCompleted && saveRegistrationCompleted && saveTrainingDayCompleted) {
            completeSavingProcess()
        }
    }

    private fun completeSavingProcess() {
        isSavingRecord = false
        hasProcessedStopped = false
        updateUIForSaving(false)
        if (saveRecordCompleted && saveRegistrationCompleted && saveTrainingDayCompleted) {
            Toast.makeText(this, "Lưu kết quả hoàn tất!", Toast.LENGTH_LONG).show()
            updateButtonVisibilityAfterSave()
            handler.postDelayed({
                finishAndGoBack()
            }, 2000)
        }
    }

    private fun updateButtonVisibilityAfterSave() {
        val isWearConnected = viewModel.isWearConnected.value
        val isRecording = viewModel.isRecording.value

        binding.apply {
            if (isWearConnected) {
                playButton.visible(false)
                stopButton.visible(false)
            } else {
                playButton.visible(true)
                stopButton.visible(false)
                playButton.enable(hasLocationPermissions())
            }
        }
    }

    private fun updateUIForSaving(isSaving: Boolean) {
        binding.apply {
            buttonBack.isEnabled = !isSaving
            buttonBack.alpha = if (isSaving) 0.5f else 1.0f

            playButton.isEnabled = !isSaving
            stopButton.isEnabled = !isSaving
            playButton.alpha = if (isSaving) 0.5f else 1.0f
            stopButton.alpha = if (isSaving) 0.5f else 1.0f

            if (isSaving) {
                checkGPS.visible(true)
                checkGPS.text = "Đang lưu kết quả tập luyện..."
                checkGPS.setBackgroundColor(ContextCompat.getColor(this@RecordActivity, R.color.light_main_color))
                recordLayout.visible(false)
            } else {
                checkGPS.visible(false)
                recordLayout.visible(true)
                if (!isSavingRecord) {
                    updateButtonVisibilityAfterSave()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isSavingRecord) {
            Toast.makeText(this, "Vui lòng chờ quá trình lưu hoàn tất", Toast.LENGTH_SHORT).show()
            return
        }
        super.onBackPressed()
    }

    private fun handleMapTrackingMode(isWearConnected: Boolean) {
        if (isWearConnected) {
            currentMarker?.remove()
            currentMarker = null
            polyline?.remove()
            polyline = null

            showWearTrackingMessage()
        } else {
            hideWearTrackingMessage()
        }
    }

    private fun showWearTrackingMessage() {
        if (::googleMap.isInitialized) {
            val defaultLocation = LatLng(10.762622, 106.660172)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

            val wearTrackingMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(defaultLocation)
                    .title("Tracking với Wear OS")
                    .snippet("Vị trí đang được theo dõi bởi đồng hồ thông minh")
            )
        }
    }

    private fun hideWearTrackingMessage() {
    }

    private fun updateButtonVisibility(isWearConnected: Boolean) {
        binding.apply {
            if (isWearConnected || isSavingRecord) {
                playButton.visible(false)
                stopButton.visible(false)
                if (isWearConnected && !isSavingRecord) {
                    showWearControlMessage(true)
                }
            } else {
                if (!isSavingRecord && !hasProcessedStopped) {
                    val isRecording = viewModel.isRecording.value
                    playButton.visible(!isRecording)
                    stopButton.visible(isRecording)
                } else if (!isSavingRecord && hasProcessedStopped) {
                    playButton.visible(true)
                    stopButton.visible(false)
                }
                showWearControlMessage(false)
            }
        }
    }

    private fun showWearControlMessage(show: Boolean) {
        binding.apply {
            if (show) {
                Toast.makeText(
                    this@RecordActivity,
                    "Sử dụng đồng hồ Wear OS để bắt đầu/dừng ghi lại",
                    Toast.LENGTH_LONG
                ).show()
            } else {
            }
        }
    }

    private fun updateWearConnectionStatus(isConnected: Boolean) {
        binding.apply {
            if (isConnected) {
                wearStatusIcon.setImageResource(R.drawable.ic_watch)
                wearStatusIcon.setColorFilter(ContextCompat.getColor(this@RecordActivity, R.color.light_main_color))
                wearStatusText.text = "Đã kết nối"
                wearStatusText.setTextColor(ContextCompat.getColor(this@RecordActivity, R.color.light_main_color))

                wearConnectionCard.visible(true)
                wearConnectionText.text = "Đồng hồ Wear OS đã kết nối"

                Handler(Looper.getMainLooper()).postDelayed({
                    wearConnectionCard.visible(false)
                }, 3000)

            } else {
                wearStatusIcon.setImageResource(R.drawable.ic_watch)
                wearStatusIcon.setColorFilter(ContextCompat.getColor(this@RecordActivity, R.color.text_color))
                wearStatusText.text = "Chưa kết nối"
                wearStatusText.setTextColor(ContextCompat.getColor(this@RecordActivity, R.color.text_color))

                wearConnectionCard.visible(false)
            }
        }

        updateButtonVisibility(isConnected)
    }

    private fun updateUIWithWearData(wearData: WearHealthData) {
        binding.apply {
            if (wearData.heartRate > 0) {
                heartRateCard.visible(true)
                tvHeartRate.text = "${wearData.heartRate.toInt()} bpm"

                val heartRateColor = when {
                    wearData.heartRate < 60 -> R.color.main_color
                    wearData.heartRate > 160 -> R.color.light_red
                    else -> R.color.dark_main_color
                }
                heartRateCard.setBackgroundColor(ContextCompat.getColor(this@RecordActivity, heartRateColor))
            } else {
                heartRateCard.visible(false)
            }

            if (heartRateCard.visibility == View.VISIBLE) {
                try {
                    val recordLayoutChildren = recordLayout.childCount
                    for (i in 0 until recordLayoutChildren) {
                        val child = recordLayout.getChildAt(i)
                        if (child is LinearLayout) {
                            val layoutParams = child.layoutParams as? LinearLayout.LayoutParams
                            layoutParams?.weight = 1f
                            child.layoutParams = layoutParams
                        }
                    }
                } catch (e: ClassCastException) {
                    Log.e("RecordActivity", "Error adjusting layout weights: ${e.message}")
                }
            } else {
                try {
                    val recordLayoutChildren = recordLayout.childCount
                    var visibleCardCount = 0

                    for (i in 0 until recordLayoutChildren) {
                        val child = recordLayout.getChildAt(i)
                        if (child is LinearLayout && child != heartRateCard && child.visibility == View.VISIBLE) {
                            visibleCardCount++
                        }
                    }

                    for (i in 0 until recordLayoutChildren) {
                        val child = recordLayout.getChildAt(i)
                        if (child is LinearLayout && child != heartRateCard && child.visibility == View.VISIBLE) {
                            val layoutParams = child.layoutParams as? LinearLayout.LayoutParams
                            layoutParams?.weight = 1f
                            child.layoutParams = layoutParams
                        }
                    }
                } catch (e: ClassCastException) {
                    Log.e("RecordActivity", "Error resetting layout weights: ${e.message}")
                }
            }
        }

        Log.d("RecordActivity", "Updated UI with wear data: HR=${wearData.heartRate}, Steps=${wearData.steps}")
    }

    private fun startRunningService() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            val serviceIntent = Intent(this, RunningForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } else {
            Toast.makeText(this, "Không thể bắt đầu theo dõi khi ứng dụng không ở trạng thái hoạt động.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRunningService() {
        viewModel.forceStopRecording()

        val serviceIntent = Intent(this, RunningForegroundService::class.java)
        serviceIntent.action = FgRecordConstants.ACTION_STOP
        startService(serviceIntent)
    }

    private fun initializeUI() {
        binding.apply {
            playButton.enable(hasLocationPermissions())
            buttonBack.setOnClickListener {
                if (isSavingRecord) {
                    Toast.makeText(this@RecordActivity, "Vui lòng chờ quá trình lưu hoàn tất", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                finishAndGoBack()
            }
            playButton.setOnClickListener {
                if (!isSavingRecord) {
                    viewModel.startRecording(this@RecordActivity)
                    startRunningService()
                }
            }
            stopButton.setOnClickListener {
                if (!isSavingRecord) {
                    viewModel.stopRecording()
                    stopRunningService()
                }
            }
        }

        initMap()
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun showModeSelectionDialog() {
        println("Attempting to show mode selection dialog")
        ModeSelectionDialog(
            this,
            onNormalModeSelected = {
                println("Normal mode selected")
                viewModel.setGuidedMode(false)
                hideGuidedModeFragment()
            },
            onGuidedModeSelected = {
                println("Guided mode selected")
                viewModel.setGuidedMode(true, this)
                initGuidedMode()
            }
        ).show()
    }

    private fun initGuidedMode() {
        Toast.makeText(
            this,
            "Chế độ luyện tập có hướng dẫn được kích hoạt",
            Toast.LENGTH_SHORT
        ).show()

        showGuidedModeFragment()
    }

    private fun showGuidedModeFragment() {
        if (!isFinishing && !supportFragmentManager.isStateSaved) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.guided_mode_container, GuidedModeFragment())
                .commit()
        }
    }

    private fun hideGuidedModeFragment() {
        guidedModeFragment?.let {
            supportFragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }
        guidedModeFragment = null
    }

    private fun requestLocationPermissions() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun requestActivityRecognitionPermission() {
        activityRecognitionPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
    }

    private fun checkAndRequestPermissions() {
        if (hasLocationPermissions()) {
            checkActivityRecognitionPermission()
        } else {
            requestLocationPermissions()
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkActivityRecognitionPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (!isSavingRecord) {
                binding.playButton.enable(true)
            }
        } else {
            requestActivityRecognitionPermission()
        }
    }

    override fun getViewModel() = RecordViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityRecordBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiRegistration = retrofitInstance.buildApi(RegistrationApiService::class.java, token)
        val apiRecord = retrofitInstance.buildApi(RecordApiService::class.java, token)
        val apiTrainingDay = retrofitInstance.buildApi(TrainingDayApiService::class.java, token)
        return listOf(
            RegistrationRepository(apiRegistration),
            RecordRepository(apiRecord),
            TrainingDayRepository(apiTrainingDay)
        )
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.routes.collect { routes ->
                    if (!viewModel.isUsingWearTracking() && routes.isNotEmpty()) {
                        drawRoute(routes)

                        val lastPoint = routes.last()
                        if (currentMarker == null) {
                            val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_runner)
                            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 48, 48, false)
                            val customIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
                            currentMarker = googleMap.addMarker(
                                MarkerOptions()
                                    .position(lastPoint)
                                    .title("Current Location")
                                    .icon(customIcon)
                                    .anchor(0.5f, 0.5f))
                        } else {
                            currentMarker?.position = lastPoint
                        }

                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPoint, 16f))
                    } else if (viewModel.isUsingWearTracking()) {
                        currentMarker?.remove()
                        currentMarker = null
                        polyline?.remove()
                        polyline = null
                    }
                }
            }
        }
    }

    private fun drawRoute(route: List<LatLng>) {
        polyline?.remove()
        if (route.size > 1) {
            val polylineOptions = PolylineOptions()
                .addAll(route)
                .color(getColor(R.color.light_main_color))
                .width(30f)
                .jointType(JointType.ROUND)
                .startCap(RoundCap())
                .endCap(RoundCap())
            polyline = googleMap.addPolyline(polylineOptions)
        }

        if (route.isNotEmpty()) {
            val bounds = LatLngBounds.builder()
            route.forEach { bounds.include(it) }
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds.build(), 100)
            googleMap.animateCamera(cameraUpdate, 1000, null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(runningUpdateReceiver)
        viewModel.stopRecording()
        stopRunningService()
        hideGuidedModeFragment()
        handler.removeCallbacksAndMessages(null)
        hasProcessedStopped = false
    }
}