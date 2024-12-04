package com.university.marathononline.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.models.ContestHistory
import com.university.marathononline.data.models.EGender
import com.university.marathononline.data.models.EPaymentStatus
import com.university.marathononline.data.models.ERegistrationStatus
import com.university.marathononline.data.models.ERewardType
import com.university.marathononline.data.models.ERole
import com.university.marathononline.data.models.Payment
import com.university.marathononline.data.models.Race
import com.university.marathononline.data.models.Registration
import com.university.marathononline.data.models.Reward
import com.university.marathononline.data.models.User
import com.university.marathononline.utils.DateUtils
import java.math.BigDecimal
import java.time.LocalDateTime


class LeaderBoardViewModel(): BaseViewModel(listOf()) {
    private val _registrations = MutableLiveData<List<Registration>>()
    val registrations: LiveData<List<Registration>> get() = _registrations
    private val _rankUser = MutableLiveData<List<Registration>>()
    val rankUsers: LiveData<List<Registration>> get() = _rankUser
    private val _top1 = MutableLiveData<Registration>()
    val top1: LiveData<Registration> get() = _top1
    private val _top2 = MutableLiveData<Registration>()
    val top2: LiveData<Registration> get() = _top2
    private val _top3 = MutableLiveData<Registration>()
    val top3: LiveData<Registration> get() = _top3

    fun setRegistrations(registrations: List<Registration>){
        _registrations.value = registrations
    }


    fun rankUsers() {
        val sortedRegistrations = registrations.value?.sortedWith(
            compareByDescending<Registration> { reg ->
                reg.raceResults?.sumOf { it.distance } ?: 0.0
            }.thenBy { reg ->
                reg.raceResults?.sumOf { it.timeTaken } ?: 0L
            }.thenBy { reg ->
                reg.raceResults?.map { it.avgSpeed }?.average() ?: 0.0
            }.thenBy { reg ->
                DateUtils.convertStringToLocalDateTime(reg.registrationDate)
            }
        )

        val topThree = sortedRegistrations?.take(3) ?: emptyList()
        val remainingRegistrations = sortedRegistrations?.drop(3) ?: emptyList()

        _rankUser.value = remainingRegistrations

        _top1.value = topThree.getOrNull(0)
        _top2.value = topThree.getOrNull(1)
        _top3.value = topThree.getOrNull(2)
    }
}