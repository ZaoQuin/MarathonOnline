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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeeklyStatisticsViewModel(
    private val repository: RaceRepository
): BaseViewModel(listOf(repository)) {
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

    private val _calories: MutableLiveData<Double> = MutableLiveData(0.0)
    val calories: LiveData<Double> get() = _calories

    private val _pace: MutableLiveData<Double> = MutableLiveData(0.0)
    val pace: LiveData<Double> get() = _pace

    private val _timeTaken: MutableLiveData<Long> = MutableLiveData(0)
    val timeTaken: LiveData<Long> get() = _timeTaken

    private var _dataLineChart: MutableLiveData<Map<String, String>> = MutableLiveData(emptyMap())
    val dataLineChart: LiveData<Map<String, String>> get() = _dataLineChart

    fun setUser(user: User) {
        _user.value = user
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
        val calories = group.sumOf { calCalogies(it.avgSpeed, avgWeight, it.timeTaken) }
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

    private var groupedByDay: Map<String,  List<Race>> = emptyMap()

    private var groupedByWeek: Map<String,  List<Race>> = emptyMap()

    fun setRaces(races: List<Race>) {
        _races.value = races
        groupedByDay = races.groupBy {
            Log.d("DateTimeTest", "Original timestamp: ${it.timestamp}")
            DateUtils.convertStringToLocalDateTime(it.timestamp).toLocalDate().toString()
        }
    }


    fun filterDataByWeek(selectedWeek: String) {
        val dateKeys = generateKeyDatesInWeek(selectedWeek)
        groupedByWeek = groupedByDay.filter { it.key in dateKeys }
        val statsByDayOfWeek = groupedByWeek.mapValues { calculateStats(it.value) }
        val results = dateKeys.mapNotNull{ statsByDayOfWeek[it] }
        Log.d("WeekStatisticsViewModel", groupedByDay.toString())
        Log.d("WeekStatisticsViewModel", statsByDayOfWeek.toString())
        Log.d("WeekStatisticsViewModel", results.toString())
        val distance = results.sumOf { it[KEY_TOTAL_DISTANCE] as? Double ?: 0.0 }
        val timeTaken = results.sumOf { it[KEY_TOTAL_TIME] as? Long ?: 0}
        val avgSpeed = (distance/ (timeTaken /3600.0))
        _distance.value = distance
        _timeTaken.value = timeTaken
        _avgSpeed.value = (avgSpeed?:0) as Double?
        _steps.value  = results.sumOf { it[KEY_TOTAL_STEPS] as? Int ?: 0}
        _calories.value = results.sumOf { it[KEY_CALORIES] as? Double ?: 0.0}
        _pace.value = calPace(avgSpeed)
        var raceOfWeek = groupedByWeek.filter { it.key in dateKeys }.values.flatten()
        val groupedByDate = raceOfWeek?.groupBy {
            DateUtils.convertStringToLocalDateTime(it.timestamp).toLocalDate().toString()
        }
        val dailyDistances = groupedByDate?.mapValues { entry ->
            val totalDistanceForDay = entry.value.sumOf { it.distance }
            "${totalDistanceForDay}"
        } ?: emptyMap()

        Log.d("WeekStatisticsViewModel", dailyDistances.toString())
        _dataLineChart.value = dailyDistances
    }


    fun generateKeyDatesInWeek(input: String): List<String> {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val keyDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val dates = input.split(" - ")
        val startDate = dateFormat.parse(dates[0]) ?: throw IllegalArgumentException("Invalid start date")
        val endDate = dateFormat.parse(dates[1]) ?: throw IllegalArgumentException("Invalid end date")

        val calendar = Calendar.getInstance()
        val keyDates = mutableListOf<String>()

        calendar.time = startDate
        while (!calendar.time.after(endDate)) {
            val keyDate = keyDateFormat.format(calendar.time)
            keyDates.add(keyDate)

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return keyDates
    }
}