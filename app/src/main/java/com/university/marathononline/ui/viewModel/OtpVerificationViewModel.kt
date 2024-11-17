package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.utils.EMAIL
import com.university.marathononline.utils.SENDER_PASS
import kotlinx.coroutines.launch
import papaya.`in`.sendmail.SendMail
import kotlin.random.Random
import kotlin.random.nextInt

class OtpVerificationViewModel(
    private val repository: AuthRepository
): BaseViewModel(repository) {
    private val _originOtp: MutableLiveData<String> = MutableLiveData()
    private val _otp: MutableLiveData<String> = MutableLiveData()
    private val _user: MutableLiveData<Resource<User>> = MutableLiveData()
    val user: LiveData<Resource<User>> get() = _user
    private val _email: MutableLiveData<String> = MutableLiveData()
    val email: LiveData<String> get() = _email
    private val _verifyResponse: MutableLiveData<Resource<User>> = MutableLiveData()
    val verifyResponse: LiveData<Resource<User>> get() = _verifyResponse

    fun setOTP(otp1: String, otp2: String, otp3: String, otp4: String, otp5: String, otp6: String) {
        _otp.value = "${otp1}${otp2}${otp3}${otp4}${otp5}${otp6}"
    }

    fun getUser() = viewModelScope.launch {
        _user.value = Resource.Loading
        _user.value = repository.getUser()
    }

    fun random() {
        Random.nextInt(0..999999).also {
            _originOtp.value = "%06d".format(it)
        }
        var mail = SendMail(
            EMAIL,
            SENDER_PASS,
            _email.value,
            "Xác thực tài khoản Marathon Online",
            "Mã xác thực của bạn là ${_originOtp.value}")

        mail.execute()
    }

    fun isOtpValid(): Boolean{
        return _originOtp.value ==_otp.value
    }

    fun setEmail(email: String) {
        _email.value = email
    }

    fun verifyAccount() = viewModelScope.launch {
        _verifyResponse.value = Resource.Loading
        _verifyResponse.value = repository.verifyAccount()
    }

    suspend fun saveStatusUser(isVerify: Boolean) =
        repository.updateStatusUser(isVerify)

}