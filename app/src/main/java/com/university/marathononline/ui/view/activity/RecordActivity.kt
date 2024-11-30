package com.university.marathononline.ui.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.*
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.race.RaceApiService
import com.university.marathononline.data.api.registration.RegistrationApiService
import com.university.marathononline.data.repository.RaceRepository
import com.university.marathononline.data.repository.RegistrationRepository
import com.university.marathononline.databinding.ActivityRecordBinding
import com.university.marathononline.ui.viewModel.RecordViewModel
import com.university.marathononline.utils.*
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RecordActivity : BaseActivity<RecordViewModel, ActivityRecordBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeUI()
        setupObservers()
        checkAndRequestActivityRecognitionPermission()
        viewModel.initializeLocationTracking(this)
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.time.collect { binding.tvTime.text = it } }
                launch { viewModel.speed.collect { binding.tvSpeed.text = it } }
                launch { viewModel.distance.collect { binding.tvDistance.text = it } }
                viewModel.isRecording.collect { isRecording ->
                    binding.playButton.visible(!isRecording)
                    binding.stopButton.visible(isRecording)
                }
            }
        }

        viewModel.createRaceResponse.observe(this) {
            when (it) {
                is Resource.Success -> viewModel.saveRaceIntoRegistration(it.value)
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }

        viewModel.saveRaceIntoRegistration.observe(this) {
            when (it) {
                is Resource.Success -> Toast.makeText(this, "Save Completed", Toast.LENGTH_SHORT)
                    .show()

                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }
    }

    private fun initializeUI() {
        binding.apply {
            playButton.enable(hasLocationPermission())
            buttonBack.setOnClickListener { finishAndGoBack() }
            playButton.setOnClickListener { viewModel.startRecording(this@RecordActivity) }
            stopButton.setOnClickListener { viewModel.stopRecording() }
        }
        requestLocationPermissionsIfNeeded()
    }


    @SuppressLint("ObsoleteSdkInt")
    private fun checkAndRequestActivityRecognitionPermission() {
        if ((SDK_INT >= Q) &&
            (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                ACTIVITY_RECOGNITION_REQUEST_CODE
            )
        }
    }

    // Checks if both Fine and Coarse Location permissions are granted
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissionsIfNeeded() {
        if (!hasLocationPermission()) {
            val locationPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

                if (!fineLocationGranted || !coarseLocationGranted) {
                    Toast.makeText(
                        this,
                        "Ứng dụng cần cấp quyền để có thể hoạt động bình thường",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    override fun getViewModel() = RecordViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityRecordBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiRegistration = retrofitInstance.buildApi(RegistrationApiService::class.java, token)
        val apiRace = retrofitInstance.buildApi(RaceApiService::class.java, token)
        return listOf(
            RegistrationRepository(apiRegistration),
            RaceRepository(apiRace)
        )
    }
}
