package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.user.UserApiService
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.AuthRepository
import kotlinx.coroutines.launch

class PaymentConfirmationViewModel(
    private val repository: AuthRepository
): BaseViewModel(listOf(repository)){
    private val _contest: MutableLiveData<Contest> = MutableLiveData()
    val contest: LiveData<Contest> get() = _contest

    private val _user: MutableLiveData<Resource<User>> = MutableLiveData()
    val user: LiveData<Resource<User>> get() = _user

    fun setContest(contest: Contest){
        _contest.value = contest
    }

    fun getUser(){
        viewModelScope.launch {
            _user.value = Resource.Loading
            _user.value = repository.getUser()
        }
    }
}