package com.university.marathononline.ui.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.university.marathononline.data.models.TrainingDay
import com.university.marathononline.data.models.WearHealthData
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.data.repository.RegistrationRepository
import com.university.marathononline.data.repository.TrainingDayRepository
import com.university.marathononline.databinding.ActivityRecordBinding
import com.university.marathononline.ui.components.ModeSelectionDialog
import com.university.marathononline.ui.view.fragment.GuidedModeFragment
import com.university.marathononline.ui.viewModel.RecordViewModel
import com.university.marathononline.utils.KEY_TRAINING_DAY
import com.university.marathononline.utils.enable
import com.university.marathononline.utils.finishAndGoBack
import com.university.marathononline.utils.visible
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

        // Initialize location tracking FIRST, before handling intent extras
        viewModel.initializeLocationTracking(this)
        viewModel.initializeWearIntegration()

        // Now it's safe to handle intent extras that might call setCurrentTrainingDay
        handleIntentExtras(intent)

        showModeSelectionDialog()
        initializeUI()
        setupObservers()
        checkAndRequestPermissions()
    }

    private fun handleIntentExtras(intent: Intent) {
        intent.apply {
            viewModel.apply {
                (getSerializableExtra(KEY_TRAINING_DAY) as? TrainingDay)?.let { trainingDay ->
                    setCurrentTrainingDay(trainingDay)
                }
            }
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.time.collect { binding.tvTime.text = it } }
                launch {
                    viewModel.averagePace.collect { pace ->
                        binding.tvPace.text = pace
                        // Update guided mode fragment if active
                        guidedModeFragment?.updateCurrentStats(
                            pace,
                            binding.tvDistance.text.toString()
                        )
                    }
                }
                launch {
                    viewModel.distance.collect { distance ->
                        binding.tvDistance.text = distance
                        // Update guided mode fragment if active
                        guidedModeFragment?.updateCurrentStats(
                            binding.tvPace.text.toString(),
                            distance
                        )
                    }
                }

                // CẬP NHẬT: Observer cho trạng thái kết nối Wear - ẩn/hiện nút start/stop
                launch {
                    viewModel.isWearConnected.collect { isConnected ->
                        updateWearConnectionStatus(isConnected)
                        // Ẩn nút start/stop khi kết nối với Wear
                        updateButtonVisibility(isConnected)

                        // CẬP NHẬT: Xử lý map khi chuyển đổi giữa Wear và Phone tracking
                        handleMapTrackingMode(isConnected)
                    }
                }

                // Observer cho dữ liệu sức khỏe từ Wear
                launch {
                    viewModel.wearHealthData.collect { wearData ->
                        wearData?.let {
                            updateUIWithWearData(it)
                        }
                    }
                }

                // CẬP NHẬT: Chỉ hiển thị nút start/stop khi không kết nối Wear và không đang save
                launch {
                    viewModel.isRecording.collect { isRecording ->
                        val isWearConnected = viewModel.isWearConnected.value
                        if (!isWearConnected && !isSavingRecord) {
                            binding.playButton.visible(!isRecording)
                            binding.stopButton.visible(isRecording)
                        } else {
                            // Khi kết nối Wear hoặc đang save, ẩn cả hai nút
                            binding.playButton.visible(false)
                            binding.stopButton.visible(false)
                        }
                    }
                }
            }
        }

        // Observer cho Create Record Response
        viewModel.createRecordResponse.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    startSavingProcess()
                }
                is Resource.Success -> {
                    saveRecordCompleted = true
                    checkSavingComplete()
                    viewModel.saveRecordIntoRegistration(it.value)
                    viewModel.saveRecordIntoTrainingDay(it.value)
                }
                is Resource.Failure -> {
                    completeSavingProcess()
                    handleApiError(it)
                }
                else -> Unit
            }
        }

        // Observer cho Save Registration Response
        viewModel.saveRecordIntoRegistration.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    // Loading đã được bắt đầu từ createRecord
                }
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

        // Observer cho Save Training Day Response
        viewModel.saveRecordIntoTrainingDay.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    // Loading đã được bắt đầu từ createRecord
                }
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
                binding.checkGPS.text = "GPS is Enabled"
                handler.postDelayed({
                    binding.checkGPS.visible(false)
                    binding.recordLayout.visible(true)
                    if (!isSavingRecord) {
                        binding.playButton.enable(true)
                    }
                }, 3000)
            } else {
                binding.playButton.enable(false)
                binding.checkGPS.text = "GPS is Disabled"
            }
        })
    }

    private fun startSavingProcess() {
        isSavingRecord = true
        saveRecordCompleted = false
        saveRegistrationCompleted = false
        saveTrainingDayCompleted = false

        updateUIForSaving(true)

        Toast.makeText(this, "Đang lưu kết quả...", Toast.LENGTH_SHORT).show()
    }

    private fun checkSavingComplete() {
        if (saveRecordCompleted && saveRegistrationCompleted && saveTrainingDayCompleted) {
            completeSavingProcess()
        }
    }

    private fun completeSavingProcess() {
        isSavingRecord = false
        updateUIForSaving(false)

        if (saveRecordCompleted && saveRegistrationCompleted && saveTrainingDayCompleted) {
            Toast.makeText(this, "Lưu kết quả hoàn tất!", Toast.LENGTH_LONG).show()

            // Optional: Auto close activity after successful save
            handler.postDelayed({
                finishAndGoBack()
            }, 2000)
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

            // Hiển thị thông báo trên map
            showWearTrackingMessage()
        } else {
            // Khi dùng phone, bật lại GPS tracking trên map
            hideWearTrackingMessage()
            // Map sẽ tự động cập nhật khi có location data từ GPS
        }
    }

    private fun showWearTrackingMessage() {
        if (::googleMap.isInitialized) {
            // Có thể thêm một custom info window hoặc overlay
            // Hoặc chỉ đơn giản là để map trống và hiển thị message

            // Option 1: Zoom về vị trí mặc định
            val defaultLocation = LatLng(10.762622, 106.660172) // HCM City
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

            // Option 2: Có thể thêm marker thông báo
            val wearTrackingMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(defaultLocation)
                    .title("Tracking với Wear OS")
                    .snippet("Vị trí đang được theo dõi bởi đồng hồ thông minh")
            )
        }
    }

    private fun hideWearTrackingMessage() {
        // Remove wear tracking message/marker if any
        // Map sẽ tự động hiển thị GPS tracking khi có data
    }

    private fun updateButtonVisibility(isWearConnected: Boolean) {
        binding.apply {
            if (isWearConnected || isSavingRecord) {
                // Khi kết nối Wear hoặc đang save, ẩn cả hai nút và hiển thị thông báo
                playButton.visible(false)
                stopButton.visible(false)

                // Có thể thêm một TextView hoặc thông báo cho user biết điều khiển từ Wear
                if (isWearConnected && !isSavingRecord) {
                    showWearControlMessage(true)
                }
            } else {
                // Khi không kết nối Wear và không đang save, hiển thị nút theo trạng thái recording
                val isRecording = viewModel.isRecording.value
                playButton.visible(!isRecording)
                stopButton.visible(isRecording)

                showWearControlMessage(false)
            }
        }
    }

    private fun showWearControlMessage(show: Boolean) {
        binding.apply {
            if (show) {
                // Tạo hoặc hiển thị TextView thông báo (cần thêm vào layout)
                // wearControlMessage.visible(true)
                // wearControlMessage.text = "Điều khiển từ đồng hồ Wear OS"

                // Hoặc hiển thị Toast
                Toast.makeText(
                    this@RecordActivity,
                    "Sử dụng đồng hồ Wear OS để bắt đầu/dừng ghi lại",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // wearControlMessage.visible(false)
            }
        }
    }

    private fun updateWearConnectionStatus(isConnected: Boolean) {
        binding.apply {
            if (isConnected) {
                // Hiển thị trạng thái đã kết nối
                wearStatusIcon.setImageResource(R.drawable.ic_watch)
                wearStatusIcon.setColorFilter(ContextCompat.getColor(this@RecordActivity, R.color.light_main_color))
                wearStatusText.text = "Đã kết nối"
                wearStatusText.setTextColor(ContextCompat.getColor(this@RecordActivity, R.color.light_main_color))

                // Hiển thị card thông báo kết nối thành công
                wearConnectionCard.visible(true)
                wearConnectionText.text = "Đồng hồ Wear OS đã kết nối"

                // Ẩn card sau 3 giây
                Handler(Looper.getMainLooper()).postDelayed({
                    wearConnectionCard.visible(false)
                }, 3000)

            } else {
                // Hiển thị trạng thái chưa kết nối
                wearStatusIcon.setImageResource(R.drawable.ic_watch)
                wearStatusIcon.setColorFilter(ContextCompat.getColor(this@RecordActivity, R.color.text_color))
                wearStatusText.text = "Chưa kết nối"
                wearStatusText.setTextColor(ContextCompat.getColor(this@RecordActivity, R.color.text_color))

                wearConnectionCard.visible(false)
            }
        }

        // Cập nhật visibility của các nút
        updateButtonVisibility(isConnected)
    }

    private fun updateUIWithWearData(wearData: WearHealthData) {
        binding.apply {
            // Hiển thị heart rate card nếu có dữ liệu nhịp tim
            if (wearData.heartRate > 0) {
                heartRateCard.visible(true)
                tvHeartRate.text = "${wearData.heartRate.toInt()} bpm"

                // Thay đổi màu theo nhịp tim
                val heartRateColor = when {
                    wearData.heartRate < 60 -> R.color.main_color  // Nhịp tim thấp
                    wearData.heartRate > 160 -> R.color.light_red  // Nhịp tim cao
                    else -> R.color.dark_main_color                // Nhịp tim bình thường
                }
                heartRateCard.setBackgroundColor(ContextCompat.getColor(this@RecordActivity, heartRateColor))
            } else {
                heartRateCard.visible(false)
            }

            // Cập nhật layout để phù hợp với việc hiển thị heart rate
            if (heartRateCard.visibility == View.VISIBLE) {
                // Điều chỉnh weight cho các LinearLayout con trong recordLayout
                try {
                    // Lấy các LinearLayout con của recordLayout
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
                    // Fallback: don't adjust weights if casting fails
                }
            } else {
                // Khi heart rate card ẩn, reset weight cho distance và pace cards
                try {
                    val recordLayoutChildren = recordLayout.childCount
                    var visibleCardCount = 0

                    // Đếm số card visible (không bao gồm heart rate card)
                    for (i in 0 until recordLayoutChildren) {
                        val child = recordLayout.getChildAt(i)
                        if (child is LinearLayout && child != heartRateCard && child.visibility == View.VISIBLE) {
                            visibleCardCount++
                        }
                    }

                    // Điều chỉnh weight cho các card visible
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
                }
            }
            stopButton.setOnClickListener {
                if (!isSavingRecord) {
                    viewModel.stopRecording()
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
                    // Chỉ vẽ route khi không dùng Wear tracking
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
                        // Clear map khi dùng Wear tracking
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
        viewModel.stopRecording() // Đảm bảo tắt các dịch vụ khi activity bị hủy
        hideGuidedModeFragment()
    }
}