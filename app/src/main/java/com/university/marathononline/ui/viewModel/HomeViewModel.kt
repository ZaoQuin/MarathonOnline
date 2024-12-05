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

class HomeViewModel(
    private val contestRepository: ContestRepository,
    private val authRepository: AuthRepository
) : BaseViewModel(listOf(contestRepository, authRepository)) {

    private val _contests: MutableLiveData<Resource<GetContestsResponse>> = MutableLiveData()
    val contests: LiveData<Resource<GetContestsResponse>> get() = _contests

    fun getActiveContests(){
        viewModelScope.launch {
            _contests.value = Resource.Loading
            _contests.value = contestRepository.getHomeContests()
        }
    }
}
