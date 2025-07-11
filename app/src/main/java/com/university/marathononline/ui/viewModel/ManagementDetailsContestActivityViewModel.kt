package com.university.marathononline.ui.viewModel

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.ENotificationType
import com.university.marathononline.data.models.Registration
import com.university.marathononline.data.models.Reward
import com.university.marathononline.data.models.RewardGroup
import com.university.marathononline.data.models.Rule
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.repository.NotificationRepository
import com.university.marathononline.data.repository.RegistrationRepository
import com.university.marathononline.data.api.notify.CreateGroupNotificationRequest
import com.university.marathononline.data.api.notify.CreateIndividualNotificationRequest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class ManagementDetailsContestActivityViewModel(
    private val contestRepository: ContestRepository,
    private val registrationRepository: RegistrationRepository,
    private val notificationRepository: NotificationRepository
): BaseViewModel(listOf(contestRepository)) {
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

    private val _cancelResponse = MutableLiveData<Resource<Contest>>()
    val cancelResponse: LiveData<Resource<Contest>> get() = _cancelResponse

    private val _blockRegistration = MutableLiveData<Resource<Registration>>()
    val blockRegistration: LiveData<Resource<Registration>> get() = _blockRegistration

    private val _prizesReponse = MutableLiveData<Resource<List<Registration>>>()
    val prizesReponse: LiveData<Resource<List<Registration>>> get() = _prizesReponse

    private val _completedResponse = MutableLiveData<Resource<Contest>>()
    val completedResponse: LiveData<Resource<Contest>> get() = _completedResponse

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

    fun cancel(){
        val contest = contest.value
        viewModelScope.launch {
            _cancelResponse.value = Resource.Loading
            _cancelResponse.value = contestRepository.cancel(contest!!)
        }
    }

    fun block(registration: Registration) {
        viewModelScope.launch {
            _blockRegistration.value = Resource.Loading
            _blockRegistration.value = registrationRepository.block(registration)
        }
    }

    fun prizes() {
        val contest = contest.value
        viewModelScope.launch {
            _prizesReponse.value = Resource.Loading
            _prizesReponse.value = registrationRepository.prizes(contest!!)
        }
    }

    fun completed() {
        val contest = contest.value
        viewModelScope.launch {
            _completedResponse.value = Resource.Loading
            _completedResponse.value = contestRepository.completed(contest!!)
        }
    }

    fun blockNotification(receiver: User) {
        viewModelScope.launch {
            val theContest = contest.value
            val request = CreateIndividualNotificationRequest(
                objectId = theContest!!.id,
                title = ENotificationType.BLOCK_CONTEST.value,
                content = "Bạn đã bị chặn ra khỏi cuộc thi ${theContest?.name} vì gian lận, liên hệ " +
                        "${theContest?.organizer?.email} để tìm hiểu thêm thông tin.",
                type = ENotificationType.BLOCK_CONTEST,
                receiver = receiver
            )
            notificationRepository.addIndividualNotification(request)
        }
    }

    fun rewardNotification(registrations: List<Registration>) {
        viewModelScope.launch {
            val runners = registrations.map { it.runner }
            val theContest = contest.value
            val request = CreateGroupNotificationRequest(
                objectId = theContest!!.id,
                title = ENotificationType.REWARD.value,
                content = "Bạn đã nhận được phần quà từ cuộc thi ${theContest?.name}.",
                type = ENotificationType.REWARD,
                receivers = runners
            )
            notificationRepository.addGroupNotification(request)
        }
    }
}