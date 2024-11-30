package com.university.marathononline.utils

class KalmanFilter(private val q: Double, private val r: Double) {
    private var x: Double = 0.0
    private var p: Double = 1.0
    private var k: Double = 0.0

    private var isInitialized = false

    fun processMeasurement(measurement: Double): Double {
        if (!isInitialized) {
            x = measurement
            isInitialized = true
        } else {
            p += q

            k = p / (p + r)
            x += k * (measurement - x)
            p *= (1 - k)
        }
        return x
    }
}