package com.university.marathononline.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.data.models.EGender
import com.university.marathononline.data.models.ERegistrationStatus
import com.university.marathononline.data.models.Registration
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.repository.ContestRepository
import kotlinx.coroutines.launch
import java.math.BigDecimal

class OrganizerStatisticsViewModel(
    private val authRepository: AuthRepository,
    private val contestRepository: ContestRepository
): BaseViewModel(listOf(authRepository, contestRepository)){
    private val _contests = MutableLiveData<List<Contest>> ()
    val contests: LiveData<List<Contest>> get() = _contests

    private val _activeRegistration = MutableLiveData<List<Registration>> ()
    val activeRegistration: LiveData<List<Registration>> get() = _activeRegistration

    private val _contestsCount = MutableLiveData<Int> ()
    val contestsCount: LiveData<Int> get() = _contestsCount

    private val _totalDistance = MutableLiveData<Double> ()
    val totalDistance: LiveData<Double> get() = _totalDistance

    private val _totalSteps = MutableLiveData<Int> ()
    val totalSteps: LiveData<Int> get() = _totalSteps

    private val _totalTime = MutableLiveData<Long> ()
    val totalTime: LiveData<Long> get() = _totalTime

    private val _fee = MutableLiveData<BigDecimal> ()
    val fee: LiveData<BigDecimal> get() = _fee

    private val _genderStatistics = MutableLiveData<Map<EGender, Int>> ()
    val genderStatistics: LiveData<Map<EGender, Int>> get() = _genderStatistics

    private val _contestStatus = MutableLiveData<Map<EContestStatus, Int>> ()
    val contestStatus: LiveData<Map<EContestStatus, Int>> get() = _contestStatus

    private val _getContestByJwtResponse = MutableLiveData<Resource<List<Contest>>>()
    val getContestByJwtResponse: LiveData<Resource<List<Contest>>> get() = _getContestByJwtResponse

    fun setContest(contests: List<Contest>){
        _contests.value = contests
    }

    fun getContest(){
        viewModelScope.launch {
            _getContestByJwtResponse.value = Resource.Loading
            _getContestByJwtResponse.value = contestRepository.getContestsByJwt()
        }
    }

    fun statistics(contests: List<Contest>) {
        try {
            Log.d("OrganizerStatistics", "Total contests: ${contests.size}")
            _contestsCount.value = contests.size

            val activeRegistrations = contests.flatMap { it.registrations ?: emptyList() }
                .filter { it.status != ERegistrationStatus.PENDING }
            Log.d("OrganizerStatistics", "Active registrations: ${activeRegistrations.size}")
            _activeRegistration.value = activeRegistrations

            val totalFee = activeRegistrations.sumOf { registration ->
                registration.payment?.amount ?: BigDecimal.ZERO
            }
            Log.d("OrganizerStatistics", "Total fee: $totalFee")
            _fee.value = totalFee

            val totalSteps = activeRegistrations.sumOf { registration ->
                registration.races?.sumOf { it.steps ?: 0 } ?: 0
            }

            _totalSteps.value = totalSteps

            val races = activeRegistrations.flatMap { it.races }
            val totalDistance = races.sumOf { it.distance }
            val totalTime = races.sumOf { it.timeTaken }
            Log.d("OrganizerStatistics", "Total distance: $totalDistance")
            Log.d("OrganizerStatistics", "Total time: $totalTime")
            _totalDistance.value = totalDistance
            _totalTime.value = totalTime

            val runners = contests.flatMap { contest ->
                contest.registrations?.map { it.runner } ?: emptyList()
            }.distinctBy { it.id }
            Log.d("OrganizerStatistics", "Total runners: ${runners.size}")
            _genderStatistics.value = runners.groupingBy { it.gender }.eachCount()
            Log.d("OrganizerStatistics", "Gender statistics: ${_genderStatistics.value}")

            val contestStatusCount = contests.filterNot { it.status == null }
                .groupingBy { it.status!! }
                .eachCount()
            Log.d("OrganizerStatistics", "Contest status statistics: $contestStatusCount")
            _contestStatus.value = contestStatusCount

        } catch (e: Exception) {
            Log.e("OrganizerStatistics", "Error calculating statistics: ${e.localizedMessage}")
        }
    }
}