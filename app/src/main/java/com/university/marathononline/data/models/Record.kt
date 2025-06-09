package com.university.marathononline.data.models

import java.io.Serializable

data class Record(
    var id: Long,
    var user: User,
    var steps: Int,
    var distance: Double,
    var timeTaken: Long,
    var avgSpeed: Double,
    var heartRate: Double,
    var startTime: String,
    var endTime: String,
    var source: ERecordSource,
    var approval: RecordApproval?= null
): Serializable

enum class ERecordSource {
    DEVICE, THIRD, MERGED
}