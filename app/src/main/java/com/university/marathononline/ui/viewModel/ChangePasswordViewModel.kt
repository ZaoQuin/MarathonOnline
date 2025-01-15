package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.data.request.UpdatePasswordRequest
import com.university.marathononline.data.response.UpdatePasswordResponse
import com.university.marathononline.utils.EMAIL
import com.university.marathononline.utils.SENDER_PASS
import kotlinx.coroutines.launch
import papaya.`in`.sendmail.SendMail
import kotlin.random.Random
import kotlin.random.nextInt

class ChangePasswordViewModel(
    private val repository: UserRepository
): BaseViewModel(listOf(repository))  {
    private val _email: MutableLiveData<String> = MutableLiveData()
    val email: LiveData<String> get() = _email

    private val _originOtp: MutableLiveData<String> = MutableLiveData()
    private val _otp: MutableLiveData<String> = MutableLiveData()

    private val _password: MutableLiveData<String> = MutableLiveData()
    val password: LiveData<String> get() = _password
    private val _updatePasswordResponse: MutableLiveData<Resource<UpdatePasswordResponse>> = MutableLiveData()
    val updatePasswordResponse: LiveData<Resource<UpdatePasswordResponse>> get() = _updatePasswordResponse

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setOTP(otp: String) {
        _otp.value = otp.trim()
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun sendOtp() {
        Random.nextInt(0..999999).also {
            val temp = "%06d".format(it)
            _originOtp.value = temp
        }

        var mail = SendMail(
            EMAIL,
            SENDER_PASS,
            _email.value,
            "Thay đổi mật khẩu tài khoản Marathon Online",
            "Mã xác thực của bạn là ${_originOtp.value}")
        mail.execute()
    }

    fun isOtpValid(): Boolean{
        return _originOtp.value ==_otp.value
    }

    fun updatePassword() {
        viewModelScope.launch {
            _updatePasswordResponse.value = Resource.Loading
            _updatePasswordResponse.value = repository.updatePassword(
                UpdatePasswordRequest(
                    _email.value.toString(),
                    _password.value.toString()
                )
            )
        }
    }
}