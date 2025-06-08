package com.university.marathononline.ui.viewModel

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.ERegistrationStatus
import com.university.marathononline.data.models.Registration
import com.university.marathononline.data.models.Reward
import com.university.marathononline.data.models.RewardGroup
import com.university.marathononline.data.models.Rule
import com.university.marathononline.data.repository.ContestRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class ContestDetailsViewModel(
    private val repository: ContestRepository
): BaseViewModel(listOf(repository)) {

    private val _contest = MutableLiveData<Contest>()
    val contest: LiveData<Contest> get() = _contest

    private val _rules = MutableLiveData<List<Rule>>()
    val rules: LiveData<List<Rule>> get() = _rules

    private val _rewardGroup = MutableLiveData<List<RewardGroup>>()
    val rewardGroup: LiveData<List<RewardGroup>> get() = _rewardGroup

    private val _selectedTab = MutableLiveData<Int>()
    val selectedTab: LiveData<Int> get() = _selectedTab

    private val _deadlineTime = MutableLiveData<String>()
    val deadlineTime: LiveData<String> get() = _deadlineTime

    private val _remainingTime = MutableLiveData<String>()
    val remainingTime: LiveData<String> get() = _remainingTime

    private val _isRegistered = MutableLiveData<Boolean>()
    val isRegistered: LiveData<Boolean> get() = _isRegistered

    private val _isBlocked = MutableLiveData<Boolean>()
    val isBlocked: LiveData<Boolean> get() = _isBlocked

    private val _registration = MutableLiveData<Registration?>()
    val registration: LiveData<Registration?> get() = _registration

    private val _refreshContest = MutableLiveData<Resource<Contest>>()
    val refreshContest: LiveData<Resource<Contest>> get() = _refreshContest

    private var countdownTimer: CountDownTimer? = null

    fun startCountdown() {
        // Cancel existing timer trước khi tạo timer mới
        countdownTimer?.cancel()

        viewModelScope.launch {
            val currentTime = LocalDateTime.now()
            val deadlineString = _deadlineTime.value

            if (deadlineString.isNullOrEmpty()) {
                Log.w("ContestDetailsViewModel", "Deadline time is null or empty")
                return@launch
            }

            try {
                val remainingSeconds = ChronoUnit.SECONDS.between(
                    currentTime,
                    LocalDateTime.parse(deadlineString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )

                if (remainingSeconds > 0) {
                    countdownTimer = object : CountDownTimer(remainingSeconds * 1000, 1000) {
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
            } catch (e: Exception) {
                Log.e("ContestDetailsViewModel", "Error parsing deadline time: ${e.message}")
                updateTime(0)
            }
        }
    }

    fun refreshContest(contestId: Long) {
        viewModelScope.launch {
            _refreshContest.value = Resource.Loading
            _refreshContest.value = repository.getById(contestId)
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

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun setContest(contest: Contest) {
        Log.d("ContestDetailsViewModel", "Setting contest: ${contest.name}")
        Log.d("ContestDetailsViewModel", "Contest registrations: ${contest.registrations?.size}")
        _contest.value = contest
    }

    fun setRules(rules: List<Rule>) {
        _rules.value = rules
    }

    fun setDeadlineTime(deadlineTime: String) {
        _deadlineTime.value = deadlineTime
    }

    fun setRewardGroups(rewards: List<Reward>) {
        _rewardGroup.value = rewards.groupBy { it.rewardRank }
            .map { RewardGroup(it.key, it.value) }
    }

    /**
     * Reset registration state - cần thiết khi refresh data
     */
    fun resetRegistrationState() {
        Log.d("ContestDetailsViewModel", "Resetting registration state")
        _registration.value = null
        _isRegistered.value = false
        _isBlocked.value = false
    }

    /**
     * Check register với logic cải thiện
     */
    fun checkRegister(email: String) {
        Log.d("ContestDetailsViewModel", "Checking registration for email: $email")

        val contestRegistrations = contest.value?.registrations

        if (contestRegistrations.isNullOrEmpty()) {
            Log.d("ContestDetailsViewModel", "No registrations found in contest")
            _isRegistered.value = false
            _registration.value = null
            _isBlocked.value = false
            return
        }

        Log.d("ContestDetailsViewModel", "Checking ${contestRegistrations.size} registrations")

        // Tìm registration của user
        val userRegistration = contestRegistrations.find { registration ->
            Log.d("ContestDetailsViewModel", "Comparing: ${registration.runner.email} with $email")
            registration.runner.email.equals(email, ignoreCase = true)
        }

        if (userRegistration != null) {
            Log.d(
                "ContestDetailsViewModel",
                "Found user registration with status: ${userRegistration.status}"
            )
            _registration.value = userRegistration
            _isRegistered.value = true
            _isBlocked.value = (userRegistration.status == ERegistrationStatus.BLOCK)
        } else {
            Log.d("ContestDetailsViewModel", "No registration found for user")
            _registration.value = null
            _isRegistered.value = false
            _isBlocked.value = false
        }
    }

    /**
     * Force refresh registration status - dùng khi cần refresh ngay lập tức
     */
    fun forceRefreshRegistrationStatus(email: String) {
        Log.d("ContestDetailsViewModel", "Force refreshing registration status for: $email")
        resetRegistrationState()

        // Delay nhỏ để đảm bảo reset state hoàn thành
        viewModelScope.launch {
            kotlinx.coroutines.delay(100)
            checkRegister(email)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cancel timer khi ViewModel bị destroy
        countdownTimer?.cancel()
        countdownTimer = null
    }
}