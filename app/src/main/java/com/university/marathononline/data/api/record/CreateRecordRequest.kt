package com.university.marathononline.data.api.record

import com.university.marathononline.data.models.ERecordSource

data class CreateRecordRequest(
    var steps: Int,
    var distance: Double,
    var avgSpeed: Double,
    var heartRate: Double,
    var startTime: String,
    var endTime: String,
    var source: ERecordSource
)