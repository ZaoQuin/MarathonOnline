package com.university.marathononline.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.Payment
import com.university.marathononline.data.models.Registration
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.repository.PaymentRepository
import com.university.marathononline.data.repository.RegistrationRepository
import com.university.marathononline.data.api.payment.CreatePaymentRequest
import com.university.marathononline.data.api.share.StringResponse
import kotlinx.coroutines.launch

class PaymentConfirmationViewModel(
    private val authRepository: AuthRepository,
    private val registrationRepository: RegistrationRepository,
    private val paymentRepository: PaymentRepository,
    private val contestRepository: ContestRepository
) : BaseViewModel(listOf(authRepository, registrationRepository, paymentRepository)) {
    private val _contest: MutableLiveData<Contest> = MutableLiveData()
    val contest: LiveData<Contest> get() = _contest

    private val _registration: MutableLiveData<Registration> = MutableLiveData()
    val registration: LiveData<Registration> get() = _registration

    private val _user: MutableLiveData<Resource<User>> = MutableLiveData()
    val user: LiveData<Resource<User>> get() = _user

    private val _registerResponse: MutableLiveData<Resource<Registration>> = MutableLiveData()
    val registerResponse: LiveData<Resource<Registration>> get() = _registerResponse

    private val _addPayment: MutableLiveData<Resource<Payment>> = MutableLiveData()
    val addPayment: LiveData<Resource<Payment>> get() = _addPayment

    private val _getContestById: MutableLiveData<Resource<Contest>> = MutableLiveData()
    val getContestById: LiveData<Resource<Contest>> get() = _getContestById

    private val _vnpayPaymentUrl: MutableLiveData<Resource<StringResponse>> = MutableLiveData()
    val vnpayPaymentUrl: LiveData<Resource<StringResponse>> get() = _vnpayPaymentUrl

    private val _processVNPayResult: MutableLiveData<Resource<CreatePaymentRequest>> =
        MutableLiveData()
    val processVNPayResult: LiveData<Resource<CreatePaymentRequest>> get() = _processVNPayResult

    fun setContest(contest: Contest) {
        _contest.value = contest
    }

    fun setRegistration(registration: Registration) {
        _registration.value = registration
    }

    fun getUser() {
        viewModelScope.launch {
            _user.value = Resource.Loading
            _user.value = authRepository.getUser()
        }
    }

    fun registerContest() {
        viewModelScope.launch {
            _registerResponse.value = Resource.Loading
            _registerResponse.value = contest.value?.let {
                registrationRepository.registerForContest(
                    it
                )
            }
        }
    }

    fun getContestById() {
        viewModelScope.launch {
            _getContestById.value = Resource.Loading
            _getContestById.value = contest.value?.id?.let { contestRepository.getById(it) }
        }
    }

    // Tạo VNPay payment URL
    fun createVNPayPayment() {
        viewModelScope.launch {
            Log.d("VNPay", "Creating VNPay payment...")
            Log.d("VNPay", "Registration: ${registration.value}")
            Log.d("VNPay", "Contest Fee: ${contest.value?.fee}")

            _vnpayPaymentUrl.value = Resource.Loading
            _vnpayPaymentUrl.value =
                paymentRepository.createVNPay(contest.value?.fee!!.toInt(), registration.value!!.id)
        }
    }

    fun processVNPayReturn(params: Map<String, String>) {
        viewModelScope.launch {
            _processVNPayResult.value = Resource.Loading
            _processVNPayResult.value = paymentRepository.getVNPayReturn(params)
        }
    }

    fun addPayment(request: CreatePaymentRequest) {
        viewModelScope.launch {
            _addPayment.value = Resource.Loading
            _addPayment.value = paymentRepository.addPayment(request)
        }
    }
}