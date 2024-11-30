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

class DeleteUserAccountViewModel (
    private val repository: AuthRepository
): BaseViewModel(listOf(repository))  {
    private val _email: MutableLiveData<String> = MutableLiveData()
    val email: LiveData<String> get() = _email

    private val _originOtp: MutableLiveData<String> = MutableLiveData()
    private val _otp: MutableLiveData<String> = MutableLiveData()

    private val _deleteAccountResponse: MutableLiveData<Resource<User>> = MutableLiveData()
    val deleteAccountResponse: LiveData<Resource<User>> get() = _deleteAccountResponse

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setOTP(otp: String) {
        _otp.value = otp.trim()
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
            "Xác thực xóa tài khoản khoản Marathon Online",
            "Mã xác thực của bạn là ${_originOtp.value}")

        mail.execute()
    }

    fun isOtpValid(): Boolean{
        return _originOtp.value ==_otp.value
    }

    fun delete() {
        viewModelScope.launch {
            _deleteAccountResponse.value = Resource.Loading
            _deleteAccountResponse.value = repository.deleteAccount()
        }
    }
}