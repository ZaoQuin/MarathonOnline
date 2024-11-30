package com.university.marathononline.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.response.GetContestsResponse
import kotlinx.coroutines.launch

class ContestViewModel(
    private val repository: ContestRepository
): BaseViewModel(listOf(repository)) {
    private val _contests: MutableLiveData<Resource<GetContestsResponse>> = MutableLiveData()
    val contests: LiveData<Resource<GetContestsResponse>> get() = _contests

    fun getActiveContests(){
        viewModelScope.launch {
            _contests.value = Resource.Loading
            _contests.value = repository.getContests()
        }
    }
}