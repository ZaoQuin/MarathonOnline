package com.university.marathononline.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
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
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

class DailyStatisticsViewModel(
    private val recordRepository: RecordRepository
): BaseViewModel(listOf(recordRepository)){
    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> get() = _user

    private val _records: MutableLiveData<List<Record>> = MutableLiveData()
    val records: LiveData<List<Record>> get() = _records

    private val _distance: MutableLiveData<Double> = MutableLiveData(0.0)
    val distance: LiveData<Double> get() = _distance

    private val _selectedDate: MutableLiveData<Date> = MutableLiveData()
    val selectedDate: LiveData<Date> get() = _selectedDate

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

    private var _dataLineChart: MutableLiveData<List<Record>> = MutableLiveData()
    val dataLineChart: LiveData<List<Record>> get() = _dataLineChart

    private val _getRecordResponse: MutableLiveData<Resource<List<Record>>> = MutableLiveData()
    val getRecordResponse: LiveData<Resource<List<Record>>> get() = _getRecordResponse

    fun processRecords(records: List<Record>) {
        _records.value = records
        calculateAndUpdateStats(records)
    }

    private fun calculateAndUpdateStats(records: List<Record>) {
        try {
            val stats = calculateStats(records)

            _distance.value = stats[KEY_TOTAL_DISTANCE] as? Double ?: 0.0
            _timeTaken.value = stats[KEY_TOTAL_TIME] as? Long ?: 0
            _avgSpeed.value = stats[KEY_AVG_SPEED] as? Double ?: 0.0
            _steps.value = stats[KEY_TOTAL_STEPS] as? Int ?: 0
            _calories.value = stats[KEY_CALORIES] as? Double ?: 0.0
            _pace.value = stats[KEY_PACE] as? Double ?: 0.0
            _dataLineChart.value = records

        } catch (e: Exception) {
            Log.e("DailyStatisticsViewModel", "Error processing records", e)
            resetStats()
        }
    }

    private fun resetStats() {
        _dataLineChart.value = emptyList()
        _distance.value = 0.0
        _timeTaken.value = 0
        _avgSpeed.value = 0.0
        _steps.value = 0
        _calories.value = 0.0
        _pace.value = 0.0
    }

    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
    }

    private fun calculateStats(records: List<Record>): Map<String, Any> {
        if (records.isEmpty()) {
            return mapOf(
                KEY_TOTAL_DISTANCE to 0.0,
                KEY_TOTAL_TIME to 0L,
                KEY_TOTAL_STEPS to 0,
                KEY_AVG_SPEED to 0.0,
                KEY_CALORIES to 0.0,
                KEY_PACE to 0.0
            )
        }

        val totalDistance = records.sumOf { it.distance }
        val totalTime = records.sumOf { DateUtils.getDurationBetween(it.startTime, it.endTime).seconds }
        val totalSteps = records.sumOf { it.steps }
        val avgSpeed = if (totalTime > 0) (totalDistance / (totalTime / 3600.0)) else 0.0

        val currUser = _user.value
        val calories = if (currUser != null) {
            val age = getAge(currUser.birthday!!)
            val gender = currUser.gender
            val avgWeight = getAvgWeightByGenderAndAge(gender!!, age)
            records.sumOf {
                calCalogies(
                    it.avgSpeed,
                    avgWeight,
                    DateUtils.getDurationBetween(it.startTime, it.endTime).seconds
                )
            }
        } else {
            0.0
        }

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

    fun getRecords(selectedDate: Date) {
        val zoneId = ZoneId.systemDefault()
        val localDate = DateUtils.convertDateToLocalDate(selectedDate)

        val startDate = localDate.atStartOfDay()
        val endDate = localDate.atTime(23, 59, 59)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val formattedStart = startDate.format(formatter)
        val formattedEnd = endDate.format(formatter)

        Log.d("DailyStatisticsViewModel", "Getting records from $formattedStart to $formattedEnd")

        viewModelScope.launch {
            _getRecordResponse.value = Resource.Loading
            _getRecordResponse.value = recordRepository.getByRunner(formattedStart, formattedEnd)
        }
    }
}