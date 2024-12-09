package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.response.GetContestsResponse
import kotlinx.coroutines.launch

class ContestViewModel(
    private val authRepository: AuthRepository,
    private val repository: ContestRepository
): BaseViewModel(listOf(authRepository, repository)) {
    private val _getContestsResponse: MutableLiveData<Resource<GetContestsResponse>> = MutableLiveData()
    val getContestsResponse: LiveData<Resource<GetContestsResponse>> get() = _getContestsResponse

    fun getActiveContests(){
        viewModelScope.launch {
            _getContestsResponse.value = Resource.Loading
            _getContestsResponse.value = repository.getContests()
        }
    }
}