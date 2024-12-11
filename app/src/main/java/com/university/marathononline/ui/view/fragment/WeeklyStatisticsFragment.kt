package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.university.marathononline.R
import com.university.marathononline.ui.viewModel.WeeklyStatisticsViewModel
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.race.RaceApiService
import com.university.marathononline.data.models.Race
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.RaceRepository
import com.university.marathononline.databinding.FragmentWeeklyStatisticsBinding
import com.university.marathononline.ui.components.WeekPickerBottomSheetFragment
import com.university.marathononline.utils.KEY_RACES
import com.university.marathononline.utils.KEY_USER
import com.university.marathononline.utils.formatCalogies
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.formatPace
import com.university.marathononline.utils.formatSpeed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeeklyStatisticsFragment : BaseFragment<WeeklyStatisticsViewModel, FragmentWeeklyStatisticsBinding>() {

    override fun getViewModel(): Class<WeeklyStatisticsViewModel> = WeeklyStatisticsViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentWeeklyStatisticsBinding {
        return FragmentWeeklyStatisticsBinding.inflate(inflater, container, false)
    }

    override fun getFragmentRepositories():  List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(RaceApiService::class.java, token)
        return listOf(RaceRepository(api))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (arguments?.getSerializable(KEY_USER) as? User)?.let { viewModel.setUser(it)
            (arguments?.getSerializable(KEY_RACES) as? List<Race>)?.let { viewModel.setRaces(it) }
        }
        initUI()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.races.observe(viewLifecycleOwner){
            val calendar = Calendar.getInstance()

            val today = calendar.time

            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val startDate = calendar.time

            calendar.add(Calendar.DAY_OF_YEAR, 6)
            val endDate = calendar.time

            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val startDateString = dateFormat.format(startDate)
            val endDateString = dateFormat.format(endDate)

            val currentWeek = "$startDateString - $endDateString"
            binding.filterText.text = currentWeek
            viewModel.filterDataByWeek(currentWeek)
        }

        viewModel.distance.observe(viewLifecycleOwner){
            binding.tvDistance.text = formatDistance(it)
        }

        viewModel.timeTaken.observe(viewLifecycleOwner){
            binding.tvTime.text = DateUtils.convertSecondsToHHMMSS(it)
        }

        viewModel.avgSpeed.observe(viewLifecycleOwner){
            binding.tvSpeed.text = formatSpeed(it)
        }

        viewModel.steps.observe(viewLifecycleOwner){
            binding.tvSteps.text = it.toString()
        }

        viewModel.calories.observe(viewLifecycleOwner){
            binding.tvCalories.text = formatCalogies(it)
        }

        viewModel.pace.observe(viewLifecycleOwner){
            binding.tvPace.text = formatPace(it)
        }


        viewModel.dataLineChart.observe(viewLifecycleOwner){
            viewModel.dataLineChart.value?.let { it1 ->
                Log.d("YearlyStatisticsFragment", it1.toString())
                setUpLineChart(it1)
            }
        }
    }

    private fun initUI() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        binding.filterButton.setOnClickListener {
            showYearPickerBottomSheet()
        }

        Glide.with(this)
            .asGif()
            .load(R.drawable.ic_calories)
            .into(binding.calogiesIcon)
    }

    private fun setUpLineChart(race: Map<String, String>) {
        binding.apply {
            val lineChart = binding.lineChart

            val xAxis = lineChart.xAxis
            val leftAxis = lineChart.axisLeft
            val rightAxis = lineChart.axisRight

            xAxis.apply {
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setLabelCount(7, true)
                textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
                gridColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
                axisLineColor = ContextCompat.getColor(requireContext(), R.color.dark_main_color)
                granularity = 1f
                isGranularityEnabled = true
            }

            leftAxis.apply {
                textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
                gridColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
                axisLineColor = ContextCompat.getColor(requireContext(), R.color.dark_main_color)
                axisMinimum = 0f
            }

            rightAxis.isEnabled = false

            val entries = ArrayList<Entry>()

            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val today = calendar.time
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            calendar.add(Calendar.DAY_OF_YEAR, - (dayOfWeek - 2))

            val weekDates = ArrayList<String>()
            for (i in 0..6) {
                weekDates.add(dateFormat.format(calendar.time))
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            weekDates.forEach { date ->
                val day = date.split("-")[2].toFloat()
                val distance = race[date]?.toFloatOrNull() ?: 0f
                entries.add(Entry(day, distance))
            }

            val dataSet = LineDataSet(entries, "Quá trình chạy trong tuần")
            dataSet.apply {
                color = ContextCompat.getColor(requireContext(), R.color.main_color)
                lineWidth = 2f
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.light_main_color))
                circleRadius = 5f
                setDrawFilled(true)
                fillColor = ContextCompat.getColor(requireContext(), R.color.light_main_color)
                fillAlpha = 80
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            val lineData = LineData(dataSet)
            lineChart.data = lineData

            lineChart.apply {
                setDrawGridBackground(false)
                description.isEnabled = false
                legend.apply {
                    isEnabled = true
                    textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
                }
                setTouchEnabled(true)
                animateXY(1500, 1500)
                setPinchZoom(true)
                setScaleEnabled(true)
            }
        }
    }

    private fun showYearPickerBottomSheet() {
        val yearPicker = WeekPickerBottomSheetFragment { selectedWeek ->
            binding.filterText.text = selectedWeek
            println("Tuần được chọn: $selectedWeek")
            viewModel.filterDataByWeek(selectedWeek)
        }
        yearPicker.show(parentFragmentManager, "YearPicker")
    }
}
