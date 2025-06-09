package com.university.marathononline.data.models

import java.io.Serializable

data class RecordApproval (
    var id: Long,
    var approvalStatus: ERecordApprovalStatus,
    var fraudRisk: Double,
    var fraudType: String,
    var reviewNote: String
): Serializable

enum class ERecordApprovalStatus {
    PENDING, APPROVED, REJECTED
}