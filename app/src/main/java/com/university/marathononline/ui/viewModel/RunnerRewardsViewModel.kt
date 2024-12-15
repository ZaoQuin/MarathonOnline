package com.university.marathononline.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.Reward

class RunnerRewardsViewModel(

): BaseViewModel(listOf()) {

    private val _contests = MutableLiveData<List<Contest>>()
    val contests: LiveData<List<Contest>> get() = _contests

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _rewardOfContests = MutableLiveData<List<Pair<Contest, Reward>>>()
    val rewardOfContests: LiveData<List<Pair<Contest, Reward>>> get() = _rewardOfContests

    fun setContests(contests: List<Contest>){
        _contests.value = contests
    }

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
}