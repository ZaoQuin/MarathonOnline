package com.university.marathononline.ui.viewModel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.request.CreateContestRequest
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AddContestViewModel(
    private val contestRepository: ContestRepository
): BaseViewModel(listOf(contestRepository)) {
    private val _addContestResponse = MutableLiveData<Resource<Contest>>()
    val addContestResponse: LiveData<Resource<Contest>> get() = _addContestResponse

    private val _updateContestResponse = MutableLiveData<Resource<Contest>>()
    val updateContestResponse: LiveData<Resource<Contest>> get() = _updateContestResponse

    private val _start = MutableLiveData<LocalDateTime>()
    val start: LiveData<LocalDateTime> get() = _start

    private val _end = MutableLiveData<LocalDateTime>()
    val end: LiveData<LocalDateTime> get() = _end

    private val _deadline = MutableLiveData<LocalDateTime>()
    val deadline: LiveData<LocalDateTime> get() = _deadline

    fun addContest(createContestRequest: CreateContestRequest){
        viewModelScope.launch {
            _addContestResponse.value = Resource.Loading

            try {
                val response = contestRepository.addContest(createContestRequest)
                _addContestResponse.value = response
            } catch (e: Exception) {
                Log.e("NetworkError", "Error during addContest request: ${e.message}")
            }
        }
    }

    fun updateContest(contest: Contest){
        viewModelScope.launch {
            _updateContestResponse.value = Resource.Loading

            try {
                val response = contestRepository.updateContest(contest)
                _updateContestResponse.value = response
            } catch (e: Exception) {
                Log.e("NetworkError", "Error during updateContest request: ${e.message}")
            }
        }
    }

    fun selectedEndDate(localDateTime: LocalDateTime) {
        _end.value = localDateTime
    }

    fun selectedRegistrationDeadlineDate(localDateTime: LocalDateTime) {
        _deadline.value = localDateTime
    }

    fun selectedStartDate(localDateTime: LocalDateTime) {
        _start.value = localDateTime
    }
}