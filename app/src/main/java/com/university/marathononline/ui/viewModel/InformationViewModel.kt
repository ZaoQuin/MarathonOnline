package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.AuthRepository
import kotlinx.coroutines.launch

class InformationViewModel(
    private val repository: AuthRepository
): BaseViewModel(repository) {

    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> get() = _user
    private val _getUser: MutableLiveData<Resource<User>> = MutableLiveData()
    val getUser: LiveData<Resource<User>> get() = _getUser

    fun setUser(user: User){
        _user.value = user
    }

    fun getUser() = viewModelScope.launch {
        _getUser.value = Resource.Loading
        _getUser.value = repository.getUser()
    }
}