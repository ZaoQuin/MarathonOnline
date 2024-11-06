package com.university.marathononline.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.request.AuthRequest
import com.university.marathononline.data.response.AuthResponse
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository
): BaseViewModel(repository) {
    private val _loginResponse: MutableLiveData<Resource<AuthResponse>> = MutableLiveData()
    val loginResponse: LiveData<Resource<AuthResponse>> get() = _loginResponse.distinctUntilChanged()

    fun login(email: String, password: String) {

        viewModelScope.launch {
            _loginResponse.value = Resource.Loading
            Log.d("LoginViewModel", "Email: ${email} Password: ${password}")
            _loginResponse.value = repository.authenticate(
                AuthRequest(
                    email.trim(),
                    password.trim()
                )
            )
        }
    }

    suspend fun saveAuthToken(token: String) = viewModelScope.launch {
        repository.saveAuthToken(token)
    }
}