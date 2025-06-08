package com.university.marathononline.data.models

import java.io.Serializable
import java.math.BigDecimal

data class Payment(
    var id: Long,
    var amount: BigDecimal,
    var paymentDate: String,
    val transactionRef: String,
    val responseCode: String,
    val bankCode: String,
    var status: EPaymentStatus
): Serializable

enum class EPaymentStatus {
    SUCCESS, FAILED, PENDING
}


