package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.university.marathononline.R
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.race.RaceApiService
import com.university.marathononline.data.models.Race
import com.university.marathononline.data.repository.RaceRepository
import com.university.marathononline.databinding.FragmentMonthlyStatisticsBinding
import com.university.marathononline.ui.components.MonthPickerBottomSheetFragment
import com.university.marathononline.ui.viewModel.MonthlyStatisticsViewModel
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_RACES
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.Calendar
import java.util.Date

class MonthlyStatisticsFragment : BaseFragment<MonthlyStatisticsViewModel, FragmentMonthlyStatisticsBinding>() {

    override fun getViewModel(): Class<MonthlyStatisticsViewModel> = MonthlyStatisticsViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMonthlyStatisticsBinding {
        return FragmentMonthlyStatisticsBinding.inflate(inflater, container, false)
    }

    override fun getFragmentRepositories():  List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(RaceApiService::class.java, token)
        return listOf(RaceRepository(api))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (arguments?.getSerializable(KEY_RACES) as? List<Race>)?.let { viewModel.setRaces(it) }

        initUI()
        observeViewModel()
    }

    private fun initUI() {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        binding.filterText.text = DateUtils.getFormattedMonthYear(currentMonth, currentYear)

        binding.filterButton.setOnClickListener { showMonthPickerBottomSheet() }
    }

    private fun setUpLineChart(race: Map<String, String>) {
        val lineChart = binding.lineChart

        val xAxis = lineChart.xAxis
        val leftAxis = lineChart.axisLeft
        val rightAxis = lineChart.axisRight

        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setLabelCount(10, true)
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

        if (race.isEmpty()) {
            val currentMonth = LocalDate.now().monthValue
            val numberOfDaysInMonth = LocalDate.of(2024, currentMonth, 1).lengthOfMonth() // Giả sử năm là 2024

            for (day in 1..numberOfDaysInMonth) {
                entries.add(Entry(day.toFloat(), 0f))
            }
        } else {
            for ((day, value) in race) {
                val dateParts = day.split("-")
                val month = dateParts[1].toInt()
                val dayInt = dateParts[2].toInt()
                val distance = value.toFloat()

                val currentMonth = LocalDate.now().monthValue
                if (month == currentMonth) {
                    entries.add(Entry(dayInt.toFloat(), distance))
                }
            }

            val currentMonth = LocalDate.now().monthValue
            val numberOfDaysInMonth = LocalDate.of(2024, currentMonth, 1).lengthOfMonth()

            for (day in 1..numberOfDaysInMonth) {
                if (entries.none { it.x == day.toFloat() }) {
                    entries.add(Entry(day.toFloat(), 0f))
                }
            }
        }

        val dataSet = LineDataSet(entries, "Quá trình chạy hằng ngày")
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

    private fun observeViewModel() {
        viewModel.races.observe(viewLifecycleOwner){
            val current = DateUtils.convertDateToLocalDate(Date())
            viewModel.filterDataByMonth(current.monthValue, current.year)
        }

        viewModel.distance.observe(viewLifecycleOwner){
            binding.tvDistance.text = viewModel.distance.value.toString()
        }

        viewModel.timeTaken.observe(viewLifecycleOwner){
            binding.tvTime.text = viewModel.timeTaken.value.toString()
        }

        viewModel.avgSpeed.observe(viewLifecycleOwner){
            binding.tvSpeed.text = viewModel.avgSpeed.value.toString()
        }

        viewModel.steps.observe(viewLifecycleOwner){
            binding.tvSteps.text = viewModel.steps.value.toString()
        }

        viewModel.dataLineChart.observe(viewLifecycleOwner){
            viewModel.dataLineChart.value?.let { it1 ->
                setUpLineChart(it1)
            }
        }
    }

    private fun showMonthPickerBottomSheet() {
        val bottomSheet = MonthPickerBottomSheetFragment { month, year ->
            binding.filterText.text = DateUtils.getFormattedMonthYear(month, year)
            viewModel.filterDataByMonth(month, year)
        }
        bottomSheet.show(parentFragmentManager, bottomSheet.tag)
    }
}
