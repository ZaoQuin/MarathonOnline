package com.university.marathononline.data.models

import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

data class Payment(
    var id: Long,
    var amount: BigDecimal,
    var paymentDate: LocalDateTime,
    var status: EPaymentStatus
): Serializable

enum class EPaymentStatus {
    PENDING, COMPLETED, FAILED, EXPIRED, CANCELLED
}

