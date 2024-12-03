package com.university.marathononline.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.models.Race
import com.university.marathononline.data.repository.RaceRepository
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_AVG_SPEED
import com.university.marathononline.utils.KEY_TOTAL_DISTANCE
import com.university.marathononline.utils.KEY_TOTAL_STEPS
import com.university.marathononline.utils.KEY_TOTAL_TIME
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date

class DailyStatisticsViewModel(
    private val repository: RaceRepository
): BaseViewModel(listOf(repository)){

    private val _races: MutableLiveData<List<Race>> = MutableLiveData()
    val races: LiveData<List<Race>> get() = _races

    private val _distance: MutableLiveData<Double> = MutableLiveData(0.0)
    val distance: LiveData<Double> get() = _distance

    private val _steps: MutableLiveData<Int> = MutableLiveData(0)
    val steps: LiveData<Int> get() = _steps

    private val _avgSpeed: MutableLiveData<Double> = MutableLiveData(0.0)
    val avgSpeed: LiveData<Double> get() = _avgSpeed

    private val _timeTaken: MutableLiveData<String> = MutableLiveData("00:00:00")
    val timeTaken: LiveData<String> get() = _timeTaken

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

    fun filterDataByDay(selectedDate: Date) {
        val dateKey = DateUtils.convertDateToLocalDate(selectedDate).toString()
        val result = statsByDay[dateKey]
        Log.d("DailyStatisticsViewModel", result.toString())
        _distance.value = result?.get(KEY_TOTAL_DISTANCE) as? Double ?: 0.0
        _timeTaken.value = result?.get(KEY_TOTAL_TIME) as? String ?: "00:00:00"
        _avgSpeed.value = result?.get(KEY_AVG_SPEED) as? Double ?: 0.0
        _steps.value = result?.get(KEY_TOTAL_STEPS) as? Int ?: 0
        _dataLineChart.value = groupedByDay[dateKey]
    }

    private fun calculateStats(group: List<Race>): Map<String, Any> {
        val totalDistance = group.sumOf { it.distance }
        val totalTime = DateUtils.convertSecondsToHHMMSS(group.sumOf { it.timeTaken })
        val totalSteps = group.sumOf { it.steps }
        val avgSpeed = (group.sumOf { it.avgSpeed } / group.size)
        val roundedTotalDistance = BigDecimal(totalDistance).setScale(2, RoundingMode.HALF_UP).toDouble()
        val roundedAvgSpeed = BigDecimal(avgSpeed).setScale(2, RoundingMode.HALF_UP).toDouble()
        return mapOf(
            KEY_TOTAL_DISTANCE to roundedTotalDistance,
            KEY_TOTAL_TIME to totalTime,
            KEY_TOTAL_STEPS to totalSteps,
            KEY_AVG_SPEED to roundedAvgSpeed
        )
    }
}