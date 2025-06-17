package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Record
import com.university.marathononline.data.models.Reward
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.data.api.contest.GetContestsResponse
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val recordRepository: RecordRepository,
    private val contestRepository: ContestRepository
): BaseViewModel(listOf(authRepository, recordRepository, contestRepository)) {

    private val _getUserResponse: MutableLiveData<Resource<User>> = MutableLiveData()
    val getUserResponse: LiveData<Resource<User>> get() = _getUserResponse

    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> get() = _user

    private val _getRecordResponse: MutableLiveData<Resource<List<Record>>> = MutableLiveData()
    val getRecordResponse: LiveData<Resource<List<Record>>> get() = _getRecordResponse

    private val _getMyContestResponse: MutableLiveData<Resource<GetContestsResponse>> = MutableLiveData()
    val getMyContestResponse: LiveData<Resource<GetContestsResponse>> get() = _getMyContestResponse

    private val _rewards: MutableLiveData<List<Reward>> = MutableLiveData()
    val rewards: LiveData<List<Reward>> get() = _rewards

    fun getRecords(){
        viewModelScope.launch {
            _getRecordResponse.value = Resource.Loading
            _getRecordResponse.value = recordRepository.getByRunner(null, null)
        }
    }

    fun getMyContest() {
        viewModelScope.launch {
            _getMyContestResponse.value = Resource.Loading
            _getMyContestResponse.value = contestRepository.getByRunner()
        }
    }

    fun getUser() {
        viewModelScope.launch {
            _getUserResponse.value = Resource.Loading
            _getUserResponse.value = authRepository.getUser()
        }
    }

    fun setUser(user: User) {
        _user.value = user
    }
}