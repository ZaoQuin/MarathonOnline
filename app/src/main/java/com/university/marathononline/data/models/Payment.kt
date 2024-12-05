package com.university.marathononline.data.models

import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

data class Payment(
    var id: Long? = null,
    var amount: BigDecimal? = null,
    var paymentDate: String? = null
): Serializable


