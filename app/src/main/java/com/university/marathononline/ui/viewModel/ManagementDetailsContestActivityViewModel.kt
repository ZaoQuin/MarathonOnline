package com.university.marathononline.ui.viewModel

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.Reward
import com.university.marathononline.data.models.RewardGroup
import com.university.marathononline.data.models.Rule
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.response.DeleteResponse
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class ManagementDetailsContestActivityViewModel(
    private val repository: ContestRepository
): BaseViewModel(listOf(repository)) {
    private val _contest = MutableLiveData<Contest>()
    val contest: LiveData<Contest> get() = _contest

    private val _rules = MutableLiveData<List<Rule>>()
    val rules: LiveData<List<Rule>> get() = _rules

    private val _rewardGroup = MutableLiveData<List<RewardGroup>>()
    val rewardGroup: LiveData<List<RewardGroup>> get() = _rewardGroup

    private val _remainingTime = MutableLiveData<String>()
    val remainingTime: LiveData<String> get() = _remainingTime

    private val _deadlineTime = MutableLiveData<String>()
    val deadlineTime: LiveData<String> get() = _deadlineTime

    private val _deleteResponse = MutableLiveData<Resource<DeleteResponse>>()
    val deleteResponse: LiveData<Resource<DeleteResponse>> get() = _deleteResponse

    fun setContest(contest: Contest){
        _contest.value = contest
    }

    fun setRules(rules: List<Rule>){
        _rules.value = rules
    }
    fun setRewardGroups(rewards: List<Reward>){
        _rewardGroup.value = rewards.groupBy { it.rewardRank }
            .map { RewardGroup(it.key, it.value) }
    }

    fun setDeadlineTime(deadlineTime: String){
        _deadlineTime.value = deadlineTime
    }

    fun startCountdown() {
        viewModelScope.launch {
            val currentTime = LocalDateTime.now()
            val remainingSeconds =
                ChronoUnit.SECONDS.between(
                    currentTime,
                    LocalDateTime.parse(
                        _deadlineTime.value,
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    ))
            if (remainingSeconds > 0) {
                object : CountDownTimer(remainingSeconds * 1000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        updateTime(millisUntilFinished / 1000)
                    }
                    override fun onFinish() {
                        updateTime(0)
                    }
                }.start()
            } else {
                updateTime(0)
            }
        }
    }

    private fun updateTime(remainingSeconds: Long) {
        val days = TimeUnit.SECONDS.toDays(remainingSeconds)
        val hours = TimeUnit.SECONDS.toHours(remainingSeconds) % 24
        val minutes = TimeUnit.SECONDS.toMinutes(remainingSeconds) % 60
        val seconds = remainingSeconds % 60

        val timeString = String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds)
        _remainingTime.postValue(timeString)
    }

    fun delete(){
        val contest = contest.value
        val contestId = contest?.id
        viewModelScope.launch {
            _deleteResponse.value = Resource.Loading
            _deleteResponse.value = repository.deleteById(contestId!!)
        }
    }

}