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
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.record.RecordApiService
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.databinding.FragmentWeeklyStatisticsBinding
import com.university.marathononline.ui.components.WeekPickerBottomSheetFragment
import com.university.marathononline.utils.KEY_USER
import com.university.marathononline.utils.formatCalogies
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.formatPace
import com.university.marathononline.utils.formatSpeed
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class WeeklyStatisticsFragment : BaseFragment<WeeklyStatisticsViewModel, FragmentWeeklyStatisticsBinding>() {

    private var currentSelectedWeek: String? = null

    override fun getViewModel(): Class<WeeklyStatisticsViewModel> = WeeklyStatisticsViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentWeeklyStatisticsBinding {
        return FragmentWeeklyStatisticsBinding.inflate(inflater, container, false)
    }

    override fun getFragmentRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(RecordApiService::class.java, token)
        return listOf(RecordRepository(api))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (arguments?.getSerializable(KEY_USER) as? User)?.let { user ->
            viewModel.setUser(user)

            // Lấy tuần hiện tại và load dữ liệu
            currentSelectedWeek = viewModel.getCurrentWeekRange()
            binding.filterText.text = currentSelectedWeek
            viewModel.setSelectedTime(currentSelectedWeek!!)
            viewModel.getRecordsForWeek(currentSelectedWeek!!)
        }
        initUI()
        observeViewModel()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.shimmerContainer.visibility = View.VISIBLE
            binding.mainContent.visibility = View.GONE
            binding.shimmerContainer.startShimmer()
        } else {
            binding.shimmerContainer.stopShimmer()
            binding.shimmerContainer.visibility = View.GONE
            binding.mainContent.visibility = View.VISIBLE
        }
    }

    private fun observeViewModel() {
        viewModel.getRecordResponse.observe(viewLifecycleOwner) { resource ->
            showLoading(false)
            when (resource) {
                is Resource.Success -> {
                    Log.d("WeeklyStatisticsFragment", "Records loaded successfully: ${resource.value.size} records")
                    viewModel.processRecords(resource.value)
                }
                is Resource.Failure -> {
                    Log.e("WeeklyStatisticsFragment", "Failed to load records")
                    handleApiError(resource)
                }
                is Resource.Loading -> {

                    showLoading(true)
                    Log.d("WeeklyStatisticsFragment", "Loading records...")
                }
                else -> Unit
            }
        }

        viewModel.distance.observe(viewLifecycleOwner) { distance ->
            binding.tvDistance.text = formatDistance(distance)
        }

        viewModel.timeTaken.observe(viewLifecycleOwner) { time ->
            binding.tvTime.text = DateUtils.convertSecondsToHHMMSS(time)
        }

        viewModel.avgSpeed.observe(viewLifecycleOwner) { speed ->
            binding.tvSpeed.text = formatSpeed(speed)
        }

        viewModel.steps.observe(viewLifecycleOwner) { steps ->
            binding.tvSteps.text = steps.toString()
        }

        viewModel.calories.observe(viewLifecycleOwner) { calories ->
            binding.tvCalories.text = formatCalogies(calories)
        }

        viewModel.pace.observe(viewLifecycleOwner) { pace ->
            binding.tvPace.text = formatPace(pace)
        }

        viewModel.dataLineChart.observe(viewLifecycleOwner) { chartData ->
            Log.d("WeeklyStatisticsFragment", "Chart data received: $chartData")
            setUpLineChart(chartData)
        }
    }

    private fun initUI() {
        binding.filterButton.setOnClickListener {
            showWeekPickerBottomSheet()
        }

        Glide.with(this)
            .asGif()
            .load(R.drawable.ic_calories)
            .into(binding.calogiesIcon)

        showLoading(true)
    }

    private fun setUpLineChart(record: Map<String, String>) {
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
        val selectedDate = viewModel.selectedTime.value

        if (selectedDate != null) {
            val weekDates = getWeekDates(selectedDate)

            weekDates.forEachIndexed { index, date ->
                val distance = record[date]?.toFloatOrNull() ?: 0f
                entries.add(Entry((index + 1).toFloat(), distance)) // 1-7 cho các ngày trong tuần
            }
        }

        val dataSet = LineDataSet(entries, "Quá trình chạy trong tuần").apply {
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
        lineChart.apply {
            data = lineData
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
            invalidate()
        }

        Log.d("WeeklyStatisticsFragment", "Line chart updated with ${entries.size} entries")
    }

    private fun showWeekPickerBottomSheet() {
        val weekPicker = WeekPickerBottomSheetFragment(currentSelectedWeek) { selectedWeek ->
            currentSelectedWeek = selectedWeek
            binding.filterText.text = selectedWeek
            viewModel.setSelectedTime(selectedWeek)
            viewModel.getRecordsForWeek(selectedWeek)
            Log.d("WeeklyStatisticsFragment", "Selected week: $selectedWeek")
        }
        weekPicker.show(parentFragmentManager, "WeekPicker")
    }

    private fun getWeekDates(dateRange: String): List<String> {
        val inputFormatter = DateTimeFormatter.ofPattern("d-M-yyyy")
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val parts = dateRange.split(" - ")
        if (parts.size != 2) {
            Log.e("WeeklyStatisticsFragment", "Invalid date range format: $dateRange")
            return emptyList()
        }

        return try {
            val startDate = LocalDate.parse(parts[0], inputFormatter)
            val endDate = LocalDate.parse(parts[1], inputFormatter)
            val daysBetween = ChronoUnit.DAYS.between(startDate, endDate).toInt()

            val weekDates = mutableListOf<String>()
            for (i in 0..daysBetween) {
                weekDates.add(startDate.plusDays(i.toLong()).format(outputFormatter))
            }
            weekDates
        } catch (e: Exception) {
            Log.e("WeeklyStatisticsFragment", "Error parsing dates", e)
            emptyList()
        }
    }
}