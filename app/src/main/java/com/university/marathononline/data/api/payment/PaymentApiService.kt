package com.university.marathononline.data.api.payment

import com.university.marathononline.data.models.Payment
import com.university.marathononline.data.request.CreatePaymentRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentApiService {
    @POST("/api/v1/payment")
    suspend fun addPayment(@Body newPayment: CreatePaymentRequest): Payment

}