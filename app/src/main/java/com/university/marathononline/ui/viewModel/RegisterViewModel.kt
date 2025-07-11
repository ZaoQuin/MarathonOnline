package com.university.marathononline.ui.viewModel

import android.util.Log
import androidx.lifecycle.*
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.*
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.data.api.user.CheckPhoneNumberRequest
import com.university.marathononline.data.api.user.CheckUsernameRequest
import com.university.marathononline.data.api.user.CreateUserRequest
import com.university.marathononline.data.api.user.CheckPhoneNumberResponse
import com.university.marathononline.data.api.user.CheckUsernameResponse
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: UserRepository
): BaseViewModel(listOf(repository)) {

    private val _fullName: MutableLiveData<String> = MutableLiveData()
    val fullName: LiveData<String> get() = _fullName

    private val _email: MutableLiveData<String> = MutableLiveData()
    val email: LiveData<String> get() = _email

    private val _password: MutableLiveData<String> = MutableLiveData()
    val password: LiveData<String> get() = _password

    private val _gender: MutableLiveData<EGender> = MutableLiveData()
    val gender: LiveData<EGender> get() = _gender

    private val _registerResponse: MutableLiveData<Resource<User>> = MutableLiveData()
    val registerResponse: LiveData<Resource<User>> get() = _registerResponse.distinctUntilChanged()

    private val _checkUsernameResponse: MutableLiveData<Resource<CheckUsernameResponse>> = MutableLiveData()
    val checkUsernameResponse: LiveData<Resource<CheckUsernameResponse>> get() = _checkUsernameResponse

    private val _checkPhoneNumberResponse: MutableLiveData<Resource<CheckPhoneNumberResponse>> = MutableLiveData()
    val checkPhoneNumberResponse: LiveData<Resource<CheckPhoneNumberResponse>> get() = _checkPhoneNumberResponse

    fun register(username: String, phoneNumber: String, birthday: String, address: String, role: ERole){
        viewModelScope.launch {
            _registerResponse.value = Resource.Loading
            val request = CreateUserRequest(
                fullName.value.toString(),
                email.value.toString(),
                phoneNumber,
                address,
                gender.value?:EGender.MALE,
                birthday,
                username,
                password.value.toString(),
                role
            )
            _registerResponse.value = repository.createUser(request)
        }
    }

    fun setFullName(fullName: String) {
        _fullName.value = fullName
    }

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun selectedGender(role: EGender) {
        try {
            _gender.value = role
        } catch (e: IllegalArgumentException) {
            Log.e("RoleSelectionViewModel", "Invalid role: $role")
        }
    }

    fun checkUsername(username: String) {
        println("Username " + username)
        viewModelScope.launch {
            _checkUsernameResponse.value = Resource.Loading
            _checkUsernameResponse.value = repository.checkUsername(CheckUsernameRequest(username))
        }
    }

    fun checkPhoneNumber(phoneNumber: String) {
        println("phoneNumber " + phoneNumber)
        viewModelScope.launch {
            _checkPhoneNumberResponse.value = Resource.Loading
            _checkPhoneNumberResponse.value = repository.checkPhoneNumber(CheckPhoneNumberRequest(phoneNumber))
        }
    }
}