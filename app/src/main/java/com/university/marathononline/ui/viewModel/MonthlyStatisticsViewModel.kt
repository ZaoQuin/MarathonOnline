package com.university.marathononline.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.models.Record
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.RecordRepository
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

class MonthlyStatisticsViewModel(
    private val repository: RecordRepository
): BaseViewModel(listOf(repository)) {
    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> get() = _user

    private val _records: MutableLiveData<List<Record>> = MutableLiveData()
    val records: LiveData<List<Record>> get() = _records

    private val _distance: MutableLiveData<Double> = MutableLiveData(0.0)
    val distance: LiveData<Double> get() = _distance

    private val _selectedMonth: MutableLiveData<Int> = MutableLiveData()
    val selectedMonth: LiveData<Int> get() = _selectedMonth

    private val _selectedYear: MutableLiveData<Int> = MutableLiveData()
    val selectedYear: LiveData<Int> get() = _selectedYear

    private val _steps: MutableLiveData<Int> = MutableLiveData(0)
    val steps: LiveData<Int> get() = _steps

    private val _avgSpeed: MutableLiveData<Double> = MutableLiveData(0.0)
    val avgSpeed: LiveData<Double> get() = _avgSpeed

    private val _calories: MutableLiveData<Double> = MutableLiveData(0.0)
    val calories: LiveData<Double> get() = _calories

    private val _pace: MutableLiveData<Double> = MutableLiveData(0.0)
    val pace: LiveData<Double> get() = _pace

    private val _timeTaken: MutableLiveData<Long> = MutableLiveData(0)
    val timeTaken: LiveData<Long> get() = _timeTaken

    private var statsByMonth: Map<String,  Map<String, Any>> = emptyMap()

    private var groupedByMonth: Map<String,  List<Record>> = emptyMap()

    private var _dataLineChart: MutableLiveData<Map<String, String>> = MutableLiveData(emptyMap())
    val dataLineChart: LiveData<Map<String, String>> get() = _dataLineChart

    fun filterDataByMonth(month: Int, year: Int) {
        try {
            val dateKey = "$year-$month"
            Log.d("MonthlyStatisticsViewModel", "dateKey" + dateKey)
            val result = statsByMonth[dateKey]?: emptyMap()
            Log.d("MonthlyStatisticsViewModel", result.toString())

            _distance.value = result[KEY_TOTAL_DISTANCE] as? Double ?: 0.0
            _timeTaken.value = result[KEY_TOTAL_TIME] as? Long ?: 0
            _avgSpeed.value = result[KEY_AVG_SPEED] as? Double ?: 0.0
            _steps.value = result[KEY_TOTAL_STEPS] as? Int ?: 0
            _calories.value = result?.get(KEY_CALORIES) as? Double ?: 0.0
            _pace.value = result?.get(KEY_PACE) as? Double ?: 0.0
            val recordOfMonth = groupedByMonth[dateKey]
            val groupedByDate = recordOfMonth?.groupBy {
                DateUtils.convertStringToLocalDateTime(it.startTime).toLocalDate().toString()
            }
            val dailyDistances = groupedByDate?.mapValues { entry ->
                val totalDistanceForDay = entry.value.sumOf { it.distance }
                "${totalDistanceForDay}"
            } ?: emptyMap()

            _dataLineChart.value = dailyDistances
        } catch (e: Exception) {
            _dataLineChart.value = emptyMap()
            _distance.value = 0.0
            _timeTaken.value = 0
            _avgSpeed.value = 0.0
            _steps.value = 0
        }
    }

    fun setRecords(records: List<Record>) {
        _records.value = records
        groupedByMonth = records.groupBy {
            val dateTime = DateUtils.convertStringToLocalDateTime(it.startTime)
            "${dateTime.year}-${dateTime.monthValue}"
        }
        statsByMonth = groupedByMonth.mapValues { calculateStats(it.value) }
    }

    fun setSelectedTime(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    private fun calculateStats(group: List<Record>): Map<String, Any> {
        val totalDistance = group.sumOf { it.distance }
        val totalTime = group.sumOf { DateUtils.getDurationBetween(it.startTime, it.endTime).seconds }
        val totalSteps = group.sumOf { it.steps }
        val avgSpeed = (totalDistance/ (totalTime /3600.0))
        val currUser = _user.value
        val age = currUser?.let { getAge(it.birthday) }
        val gender = currUser?.let { it.gender }
        val avgWeight = getAvgWeightByGenderAndAge(gender!!, age!!)
        val calories = group.sumOf { calCalogies(it.avgSpeed, avgWeight, DateUtils.getDurationBetween(it.startTime, it.endTime).seconds) }
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

    fun setUser(user: User) {
        _user.value = user
    }
}