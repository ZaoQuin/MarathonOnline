package com.university.marathononline.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.ERegistrationStatus
import com.university.marathononline.data.models.Payment
import com.university.marathononline.data.models.Registration
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.repository.PaymentRepository
import com.university.marathononline.data.repository.RegistrationRepository
import com.university.marathononline.data.request.CreatePaymentRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class PaymentConfirmationViewModel(
    private val authRepository: AuthRepository,
    private val registrationRepository: RegistrationRepository,
    private val paymentRepository: PaymentRepository,
    private val contestRepository: ContestRepository
): BaseViewModel(listOf(authRepository, registrationRepository, paymentRepository)){
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
    val getContestById : LiveData<Resource<Contest>> get() = _getContestById

    fun setContest(contest: Contest){
        _contest.value = contest
    }

    fun setRegistration(registration: Registration){
        _registration.value = registration
    }

    fun getUser(){
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

    fun payment(){
        viewModelScope.launch {
            try {
                Log.d("Payment", "Registration: ${registration.value}")
                Log.d("Payment", "Contest Fee: ${contest.value?.fee}")

                _addPayment.value = Resource.Loading

                val paymentRequest = CreatePaymentRequest(
                    amount = contest.value?.fee,
                    registration = registration.value
                )

                Log.d("Payment", "Payment Request: $paymentRequest")

                val result = paymentRepository.addPayment(paymentRequest)
                _addPayment.value = result
            } catch (e: HttpException) {
                val errorResponse = e.response()?.errorBody()?.string()
                Log.e("HTTP Error", "Error: $errorResponse")
            }catch (e: Exception) {
                Log.e("Payment", "Error adding payment", e)
            }
        }
    }
    fun getContestById(){
        viewModelScope.launch {
            _getContestById.value = Resource.Loading
            _getContestById.value = contest.value?.id?.let { contestRepository.getById(it) }
        }
    }
}