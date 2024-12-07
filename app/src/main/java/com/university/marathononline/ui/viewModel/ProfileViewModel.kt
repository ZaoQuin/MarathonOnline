package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.Race
import com.university.marathononline.data.models.Reward
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.repository.RaceRepository
import com.university.marathononline.data.response.GetContestsResponse
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val raceRepository: RaceRepository,
    private val contestRepository: ContestRepository
): BaseViewModel(listOf(authRepository, raceRepository, contestRepository)) {

    private val _getUserResponse: MutableLiveData<Resource<User>> = MutableLiveData()
    val getUserResponse: LiveData<Resource<User>> get() = _getUserResponse

    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> get() = _user

    private val _getRaceResponse: MutableLiveData<Resource<List<Race>>> = MutableLiveData()
    val getRaceResponse: LiveData<Resource<List<Race>>> get() = _getRaceResponse

    private val _getMyContestResponse: MutableLiveData<Resource<GetContestsResponse>> = MutableLiveData()
    val getMyContestResponse: LiveData<Resource<GetContestsResponse>> get() = _getMyContestResponse

    private val _contests: MutableLiveData<List<Contest>> = MutableLiveData()
    val contests: LiveData<List<Contest>> get() = _contests

    private val _rewards: MutableLiveData<List<Reward>> = MutableLiveData()
    val rewards: LiveData<List<Reward>> get() = _rewards

    fun setContests(contests: List<Contest>){
        _contests.value = contests
    }

    fun setRewards(email: String){
        _rewards.value = contests.value?.flatMap { contest ->
            contest.registrations?.filter { registration ->
                registration.runner.email == email
            }?.flatMap { registration ->
                registration.rewards!!
            } ?: emptyList()
        }
    }

    fun getRaces(){
        viewModelScope.launch {
            _getRaceResponse.value = Resource.Loading
            _getRaceResponse.value = raceRepository.getByRunner()
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