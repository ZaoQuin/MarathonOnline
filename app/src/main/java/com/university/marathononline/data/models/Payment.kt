package com.university.marathononline.data.models

import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

data class Payment(
    var id: Long? = null,
    var amount: BigDecimal,
    var paymentDate: LocalDateTime,
    val transactionRef: String,
    val responseCode: String,
    val bankCode: String,
    var status: EPaymentStatus
): Serializable

enum class EPaymentStatus {
    SUCCESS, FAILED, PENDING
}


