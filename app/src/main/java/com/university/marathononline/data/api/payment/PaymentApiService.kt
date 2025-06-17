package com.university.marathononline.data.api.payment

import com.university.marathononline.data.models.Payment
import com.university.marathononline.data.api.share.StringResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface PaymentApiService {
    @POST("/api/v1/payment")
    suspend fun addPayment(@Body newPayment: CreatePaymentRequest): Payment

    @GET("/api/v1/payment/create-vnpay")
    suspend fun createVNPay(
        @Query("amount") amount: Int,
        @Query("registrationId") registrationId: Long
    ): StringResponse

    @GET("/api/v1/payment/vnpay-return")
    suspend fun getVNPayReturn(
        @QueryMap params: Map<String, String>
    ): CreatePaymentRequest
}