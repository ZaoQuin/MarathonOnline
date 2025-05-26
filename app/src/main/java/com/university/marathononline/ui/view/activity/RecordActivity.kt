package com.university.marathononline.ui.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
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
import com.university.marathononline.data.api.trainingDay.TrainingDayApiService
import com.university.marathononline.data.models.TrainingDay
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
        handleIntentExtras(intent)
        showModeSelectionDialog()
        initializeUI()
        setupObservers()
        checkAndRequestPermissions()
        viewModel.initializeLocationTracking(this)
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
                        guidedModeFragment?.updateCurrentStats(pace, binding.tvDistance.text.toString())
                    }
                }
                launch {
                    viewModel.distance.collect { distance ->
                        binding.tvDistance.text = distance
                        // Update guided mode fragment if active
                        guidedModeFragment?.updateCurrentStats(binding.tvPace.text.toString(), distance)
                    }
                }
                viewModel.isRecording.collect { isRecording ->
                    binding.playButton.visible(!isRecording)
                    binding.stopButton.visible(isRecording)
                }
            }
        }

        viewModel.createRecordResponse.observe(this) {
            when (it) {
                is Resource.Success -> {
                    viewModel.saveRecordIntoRegistration(it.value)
                    viewModel.saveRecordIntoTrainingDay(it.value)
                }
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }

        viewModel.saveRecordIntoRegistration.observe(this) {
            when (it) {
                is Resource.Success -> Toast.makeText(this, "Save to Registration Completed", Toast.LENGTH_SHORT)
                    .show()
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }

        viewModel.saveRecordIntoTrainingDay.observe(this) {
            when (it) {
                is Resource.Success -> Toast.makeText(this, "Save to Training Day Completed", Toast.LENGTH_SHORT)
                    .show()
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }

        viewModel.isGPSEnabled.observe(this, Observer { isEnabled ->
            if (isEnabled) {
                binding.checkGPS.text = "GPS is Enabled"
                handler.postDelayed({
                    binding.checkGPS.visible(false)
                    binding.recordLayout.visible(true)
                    binding.playButton.enable(true)
                }, 3000)
            } else {
                binding.playButton.enable(false)
                binding.checkGPS.text = "GPS is Disabled"
            }
        })
    }

    private fun initializeUI() {
        binding.apply {
            playButton.enable(hasLocationPermissions())
            buttonBack.setOnClickListener { finishAndGoBack() }
            playButton.setOnClickListener {
                viewModel.startRecording(this@RecordActivity)
            }
            stopButton.setOnClickListener { viewModel.stopRecording() }
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
            binding.playButton.enable(true)
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
                    if (routes.isNotEmpty()) {
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