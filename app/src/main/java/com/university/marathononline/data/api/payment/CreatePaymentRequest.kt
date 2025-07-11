package com.university.marathononline.data.api.payment

import com.university.marathononline.data.models.EPaymentStatus
import java.math.BigDecimal

data class CreatePaymentRequest (
    var amount: BigDecimal,
    var paymentDate: String,
    var transactionRef: String,
    var responseCode: String,
    var bankCode: String,
    var status: EPaymentStatus,
    var registrationId: Long
)