package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.university.marathononline.R
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.record.RecordApiService
import com.university.marathononline.data.models.Record
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.databinding.FragmentMonthlyStatisticsBinding
import com.university.marathononline.ui.components.MonthPickerBottomSheetFragment
import com.university.marathononline.ui.viewModel.MonthlyStatisticsViewModel
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_RECORDS
import com.university.marathononline.utils.KEY_USER
import com.university.marathononline.utils.formatCalogies
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.formatPace
import com.university.marathononline.utils.formatSpeed
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.Calendar
import java.util.Date

class MonthlyStatisticsFragment : BaseFragment<MonthlyStatisticsViewModel, FragmentMonthlyStatisticsBinding>() {

    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun getViewModel(): Class<MonthlyStatisticsViewModel> = MonthlyStatisticsViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMonthlyStatisticsBinding {
        return FragmentMonthlyStatisticsBinding.inflate(inflater, container, false)
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
            viewModel.setSelectedTime(currentMonth + 1, currentYear)
            viewModel.getRecords(currentMonth + 1, currentYear)
        }
        initUI()
        observeViewModel()
    }

    private fun initUI() {
        binding.filterText.text = DateUtils.getFormattedMonthYear(currentMonth, currentYear)
        binding.filterButton.setOnClickListener { showMonthPickerBottomSheet() }

        Glide.with(this)
            .asGif()
            .load(R.drawable.ic_calories)
            .into(binding.calogiesIcon)
    }

    private fun setUpLineChart(record: Map<String, String>) {
        val lineChart = binding.lineChart
        val year = viewModel.selectedYear.value ?: currentYear
        val month = viewModel.selectedMonth.value ?: (currentMonth + 1)
        val numberOfDaysInMonth = LocalDate.of(year, month, 1).lengthOfMonth()

        val xAxis = lineChart.xAxis
        val leftAxis = lineChart.axisLeft
        val rightAxis = lineChart.axisRight

        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setLabelCount(numberOfDaysInMonth, true)
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
        for (day in 1..numberOfDaysInMonth) {
            val formattedDay = day.toString().padStart(2, '0')
            val formattedMonth = month.toString().padStart(2, '0')
            val key = "$year-$formattedMonth-$formattedDay"

            val distance = record[key]?.toFloatOrNull() ?: 0f
            entries.add(Entry(day.toFloat(), distance))
        }

        entries.sortBy { it.x }

        val dataSet = LineDataSet(entries, "Quá trình chạy hằng ngày").apply {
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

        Log.d("MonthlyStatisticsFragment", "Line chart updated with ${entries.size} entries")
    }

    private fun observeViewModel() {
        viewModel.getRecordResponse.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Log.d("MonthlyStatisticsFragment", "Records loaded successfully: ${resource.value.size} records")
                    viewModel.processRecords(resource.value)
                }
                is Resource.Failure -> {
                    Log.e("MonthlyStatisticsFragment", "Failed to load records")
                    handleApiError(resource)
                }
                is Resource.Loading -> {
                    Log.d("MonthlyStatisticsFragment", "Loading records...")
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
            setUpLineChart(chartData)
        }
    }

    private fun showMonthPickerBottomSheet() {
        val bottomSheet = MonthPickerBottomSheetFragment(currentMonth, currentYear) { month, year ->
            currentMonth = month
            currentYear = year

            binding.filterText.text = DateUtils.getFormattedMonthYear(month, year)
            viewModel.setSelectedTime(month + 1, year)
            viewModel.getRecords(month + 1, year)
        }
        bottomSheet.show(parentFragmentManager, bottomSheet.tag)
    }
}