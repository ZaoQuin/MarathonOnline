package com.university.marathononline.data.request

import com.university.marathononline.data.models.Registration
import java.math.BigDecimal
import java.time.LocalDateTime

data class CreatePaymentRequest (
    val amount: BigDecimal? = null,
    val registration: Registration? = null,
)