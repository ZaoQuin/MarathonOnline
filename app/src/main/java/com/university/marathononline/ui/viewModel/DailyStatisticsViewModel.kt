package com.university.marathononline.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.models.Race
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.RaceRepository
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_AVG_SPEED
import com.university.marathononline.utils.KEY_CALORIES
import com.university.marathononline.utils.KEY_PACE
import com.university.marathononline.utils.KEY_TOTAL_DISTANCE
import com.university.marathononline.utils.KEY_TOTAL_STEPS
import com.university.marathononline.utils.KEY_TOTAL_TIME
import com.university.marathononline.utils.calCalogies
import com.university.marathononline.utils.calPace
import com.university.marathononline.utils.getAge
import com.university.marathononline.utils.getAvgWeightByGenderAndAge
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date

class DailyStatisticsViewModel(
    private val raceRepository: RaceRepository
): BaseViewModel(listOf(raceRepository)){
    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> get() = _user

    private val _races: MutableLiveData<List<Race>> = MutableLiveData()
    val races: LiveData<List<Race>> get() = _races

    private val _distance: MutableLiveData<Double> = MutableLiveData(0.0)
    val distance: LiveData<Double> get() = _distance

    private val _steps: MutableLiveData<Int> = MutableLiveData(0)
    val steps: LiveData<Int> get() = _steps

    private val _avgSpeed: MutableLiveData<Double> = MutableLiveData(0.0)
    val avgSpeed: LiveData<Double> get() = _avgSpeed

    private val _timeTaken: MutableLiveData<Long> = MutableLiveData(0)
    val timeTaken: LiveData<Long> get() = _timeTaken

    private val _calories: MutableLiveData<Double> = MutableLiveData(0.0)
    val calories: LiveData<Double> get() = _calories

    private val _pace: MutableLiveData<Double> = MutableLiveData(0.0)
    val pace: LiveData<Double> get() = _pace

    private var statsByDay: Map<String,  Map<String, Any>> = emptyMap()

    private var groupedByDay: Map<String,  List<Race>> = emptyMap()

    private var _dataLineChart: MutableLiveData<List<Race>> = MutableLiveData()
    val dataLineChart: LiveData<List<Race>> get() = _dataLineChart

    fun setRaces(races: List<Race>){
        _races.value = races
        groupedByDay = races.groupBy {
            Log.d("DateTimeTest", "Original timestamp: ${it.timestamp}")
            DateUtils.convertStringToLocalDateTime(it.timestamp).toLocalDate().toString()
        }
        statsByDay = groupedByDay.mapValues { calculateStats(it.value) }
    }

    fun setUser(user: User){
        _user.value = user
    }

    fun filterDataByDay(selectedDate: Date) {
        val dateKey = DateUtils.convertDateToLocalDate(selectedDate).toString()
        val result = statsByDay[dateKey]
        Log.d("DailyStatisticsViewModel", result.toString())
        _distance.value = result?.get(KEY_TOTAL_DISTANCE) as? Double ?: 0.0
        _timeTaken.value = result?.get(KEY_TOTAL_TIME) as? Long ?: 0
        _avgSpeed.value = result?.get(KEY_AVG_SPEED) as? Double ?: 0.0
        _steps.value = result?.get(KEY_TOTAL_STEPS) as? Int ?: 0
        _calories.value = result?.get(KEY_CALORIES) as? Double ?: 0.0
        _pace.value = result?.get(KEY_PACE) as? Double ?: 0.0
        _dataLineChart.value = groupedByDay[dateKey]
    }

    private fun calculateStats(group: List<Race>): Map<String, Any> {
        val totalDistance = group.sumOf { it.distance }
        val totalTime = group.sumOf { it.timeTaken }
        val totalSteps = group.sumOf { it.steps }
        val avgSpeed = (totalDistance/ (totalTime /3600.0))
        val currUser = _user.value
        val age = currUser?.let { getAge(it.birthday) }
        val gender = currUser?.let { it.gender }
        val avgWeight = getAvgWeightByGenderAndAge(gender!!, age!!)
        val calories = group.sumOf { calCalogies(it.avgSpeed, avgWeight, it.timeTaken)}
        val pace = calPace(avgSpeed)
        val roundedTotalDistance = BigDecimal(totalDistance).setScale(2, RoundingMode.HALF_UP).toDouble()
        val roundedAvgSpeed = BigDecimal(avgSpeed).setScale(2, RoundingMode.HALF_UP).toDouble()
        return mapOf(
            KEY_TOTAL_DISTANCE to roundedTotalDistance,
            KEY_TOTAL_TIME to totalTime,
            KEY_TOTAL_STEPS to totalSteps,
            KEY_AVG_SPEED to roundedAvgSpeed,
            KEY_CALORIES to calories,
            KEY_PACE to pace
        )
    }
}