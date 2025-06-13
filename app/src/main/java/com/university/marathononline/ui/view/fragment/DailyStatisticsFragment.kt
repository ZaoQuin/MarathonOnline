package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.university.marathononline.databinding.FragmentDailyStatisticsBinding
import com.university.marathononline.ui.viewModel.DailyStatisticsViewModel
import com.university.marathononline.ui.components.DatePickerBottomSheetFragment
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.utils.KEY_USER
import com.university.marathononline.utils.formatCalogies
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.formatPace
import com.university.marathononline.utils.formatSpeed
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Date

class DailyStatisticsFragment : BaseFragment<DailyStatisticsViewModel, FragmentDailyStatisticsBinding>() {

    private var currentSelectedDate = Date()

    override fun getViewModel(): Class<DailyStatisticsViewModel> = DailyStatisticsViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentDailyStatisticsBinding {
        return FragmentDailyStatisticsBinding.inflate(inflater, container, false)
    }

    override fun getFragmentRepositories():  List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(RecordApiService::class.java, token)
        return listOf(RecordRepository(api))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (arguments?.getSerializable(KEY_USER) as? User)?.let { user ->
            viewModel.setUser(user)
            viewModel.setSelectedDate(currentSelectedDate)
            viewModel.getRecords(currentSelectedDate)
        }

        initUI()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.getRecordResponse.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Log.d("DailyStatisticsFragment", "Records loaded successfully: ${resource.value.size} records")
                    viewModel.processRecords(resource.value)
                }
                is Resource.Failure -> {
                    Log.e("DailyStatisticsFragment", "Failed to load records")
                    handleApiError(resource)
                }
                is Resource.Loading -> {
                    Log.d("DailyStatisticsFragment", "Loading records...")
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

        viewModel.dataLineChart.observe(viewLifecycleOwner) { records ->
            setUpLineChart(records)
            Log.d("DailyStatisticsFragment", "Chart data updated with ${records.size} records")
        }
    }

    private fun initUI() {
        binding.filterText.text = DateUtils.getCurrentDate()
        binding.filterButton.setOnClickListener { showDatePickerBottomSheet() }

        Glide.with(this)
            .asGif()
            .load(R.drawable.ic_calories)
            .into(binding.calogiesIcon)
    }

    private fun setUpLineChart(records: List<Record>) {
        binding.apply {
            val xAxis = lineChart.xAxis
            val leftAxis = lineChart.axisLeft
            val rightAxis = lineChart.axisRight

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setLabelCount(24, true)
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

            val hourlyDistances = mutableMapOf<Int, Double>()
            records.forEach { record ->
                val recordTime = DateUtils.convertStringToLocalDateTime(record.startTime)
                val hour = recordTime.hour
                hourlyDistances[hour] = hourlyDistances.getOrDefault(hour, 0.0) + record.distance
            }

            for (hour in 0..23) {
                if (!hourlyDistances.containsKey(hour)) {
                    hourlyDistances[hour] = 0.0
                }
            }

            val entries = ArrayList<Entry>()
            for (hour in 0..23) {
                val distance = hourlyDistances.getOrDefault(hour, 0.0)
                entries.add(Entry(hour.toFloat(), distance.toFloat()))
            }

            Log.d("DailyStatisticsFragment", "Line chart updated with ${entries.size} entries")

            val dataSet = LineDataSet(entries, "Quá trình chạy hằng giờ").apply {
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
        }
    }

    private fun showDatePickerBottomSheet() {
        val bottomSheet = DatePickerBottomSheetFragment(currentSelectedDate) { selectedDate ->
            binding.filterText.text = DateUtils.getFormattedDate(selectedDate)
            currentSelectedDate = selectedDate
            viewModel.setSelectedDate(selectedDate)
            viewModel.getRecords(selectedDate)
        }

        bottomSheet.show(parentFragmentManager, bottomSheet.tag)
    }
}