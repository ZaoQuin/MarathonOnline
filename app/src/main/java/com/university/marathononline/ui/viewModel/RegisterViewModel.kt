package com.university.marathononline.ui.viewModel

import android.util.Log
import androidx.lifecycle.*
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.*
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.data.request.CreateUserRequest
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: UserRepository
): BaseViewModel(repository) {

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

    fun register(username: String, phoneNumber: String, birthday: String, address: String){
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
                ERole.RUNNER
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
}