package com.university.marathononline.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.ERole
import com.university.marathononline.data.repository.UserRepository
import com.university.marathononline.data.request.CheckEmailRequest
import com.university.marathononline.data.response.CheckEmailResponse
import kotlinx.coroutines.launch

class RegisterBasicInformationViewModel(
    private val repository: UserRepository
): BaseViewModel(repository) {
    private val _role: MutableLiveData<ERole> = MutableLiveData()
    val role: LiveData<ERole> get() = _role
    private val _email: MutableLiveData<String> = MutableLiveData()
    val email: LiveData<String> get() = _email
    private val _checkEmailResponse: MutableLiveData<Resource<CheckEmailResponse>> = MutableLiveData()
    val checkEmailResponse: LiveData<Resource<CheckEmailResponse>> get() = _checkEmailResponse

    fun setEmail(email: String){
        _email.value = email
    }

    fun checkEmail(){
        viewModelScope.launch {
            _checkEmailResponse.value = Resource.Loading
            _checkEmailResponse.value = repository.checkEmail(CheckEmailRequest(_email.value.toString()))
        }
    }

    fun selectedRole(role: ERole){
        try {
            _role.value = role
            Log.d("RegisterBasicInformationViewModel", "Selected role: ${_role.value}")
        } catch (e: IllegalArgumentException) {
            Log.e("RoleSelectionViewModel", "Invalid role: $role")
        }
    }
}