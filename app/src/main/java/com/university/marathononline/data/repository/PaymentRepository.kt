package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.payment.PaymentApiService
import com.university.marathononline.data.api.payment.CreatePaymentRequest

class PaymentRepository (
    private val api: PaymentApiService
): BaseRepository() {

    suspend fun addPayment(newPaymentRequest: CreatePaymentRequest) = safeApiCall {
        api.addPayment(newPaymentRequest)
    }

    suspend fun createVNPay(amount: Int, registrationId: Long) = safeApiCall {
        api.createVNPay(amount, registrationId)
    }

    suspend fun getVNPayReturn(params: Map<String, String>) = safeApiCall {
        api.getVNPayReturn(params)
    }
}