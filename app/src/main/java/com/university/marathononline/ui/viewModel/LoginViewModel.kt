package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.ERole
import com.university.marathononline.data.models.LoginInfo
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.request.AuthRequest
import com.university.marathononline.data.response.AuthResponse
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository
): BaseViewModel(listOf(repository)) {
    private val _selectedRole: MutableLiveData<ERole> = MutableLiveData()
    val selectedRole: LiveData<ERole> get() = _selectedRole

    private val _loginResponse: MutableLiveData<Resource<AuthResponse>> = MutableLiveData()
    val loginResponse: LiveData<Resource<AuthResponse>> get() = _loginResponse.distinctUntilChanged()
    private val _loginInfo: MutableLiveData<LoginInfo> = MutableLiveData()
    val loginInfo: LiveData<LoginInfo> get() = _loginInfo

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginResponse.value = Resource.Loading
            _loginResponse.value = repository.authenticate(AuthRequest(email, password))
        }
    }

    suspend fun saveAuthenticatedUser(authResponse: AuthResponse){
        repository.saveAuthenticatedUser(authResponse)
    }

    fun saveLoginInfo(email: String, password: String) {
        viewModelScope.launch {
            repository.saveLoginInfo(email, password)
        }
    }

    fun clearLoginInfo() {
        viewModelScope.launch {
            repository.clearLoginInfo()
        }
    }

    fun getLoginInfo() {
        viewModelScope.launch {
            _loginInfo.value = repository.getLoginInfo()
        }
    }

    fun selectedRole(role: ERole) {
        _selectedRole.value = role
    }
}