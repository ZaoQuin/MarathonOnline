package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.Reward
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.api.contest.GetContestsResponse
import kotlinx.coroutines.launch

class RunnerRewardsViewModel(
    private val contestRepository: ContestRepository
): BaseViewModel(listOf()) {

    private val _contests = MutableLiveData<List<Contest>>()
    val contests: LiveData<List<Contest>> get() = _contests

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _rewardOfContests = MutableLiveData<List<Pair<Contest, Reward>>>()
    val rewardOfContests: LiveData<List<Pair<Contest, Reward>>> get() = _rewardOfContests

    private val _getContest = MutableLiveData<Resource<Contest>>()
    val getContest: LiveData<Resource<Contest>> get() = _getContest

    private val _getContests = MutableLiveData<Resource<GetContestsResponse>>()
    val getContests: LiveData<Resource<GetContestsResponse>> get() = _getContests

    fun setEmail(email: String){
        _email.value = email
    }

    fun setRewardOfContest(contests: List<Contest>) {
        val email = email?.value
        val theRewardOfContest = mutableListOf<Pair<Contest, Reward>>()

        contests.forEach { contest ->
            val rewards = contest.registrations?.filter { reg ->
                reg.runner.email == email
            }?.flatMap { reg ->
                reg.rewards ?: emptyList()
            } ?: emptyList()

            rewards.forEach { reward ->
                theRewardOfContest.add(Pair(contest, reward))
            }
        }

        _rewardOfContests.value = theRewardOfContest
    }

    fun loadAllUserRewards() {
        viewModelScope.launch {
            _getContests.value = Resource.Loading
            _getContests.value = contestRepository.getContests()
        }
    }
}