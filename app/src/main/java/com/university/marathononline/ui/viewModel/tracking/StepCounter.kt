package com.university.marathononline.ui.viewModel.tracking

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StepCounter(private val context: Context) {

    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null && event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                _steps.value += 1
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    init {
        initializeSensor()
    }

    private fun initializeSensor() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    }

    fun startCounting() {
        stepSensor?.let {
            sensorManager.registerListener(stepListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopCounting() {
        if (::sensorManager.isInitialized) {
            sensorManager.unregisterListener(stepListener)
        }
    }

    fun reset() {
        _steps.value = 0
    }

    fun setSteps(steps: Int) {
        _steps.value = steps
    }
}