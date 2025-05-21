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
        val api = retrofitInstance.buildApi(RecordApiService::class.java, token)
        return listOf(RecordRepository(api))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (arguments?.getSerializable(KEY_USER) as? User)?.let { viewModel.setUser(it)
            (arguments?.getSerializable(KEY_RECORDS) as? List<Record>)?.let { viewModel.setRecords(it) }
        }
        initUI()
        observeViewModel()
    }

    private fun initUI() {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        binding.filterText.text = DateUtils.getFormattedMonthYear(currentMonth, currentYear)

        binding.filterButton.setOnClickListener { showMonthPickerBottomSheet() }


        Glide.with(this)
            .asGif()
            .load(R.drawable.ic_calories)
            .into(binding.calogiesIcon)
    }

    private fun setUpLineChart(record: Map<String, String>) {
        val lineChart = binding.lineChart
        val year = viewModel.selectedYear.value?: LocalDate.now().year
        val month = viewModel.selectedMonth.value?: LocalDate.now().monthValue
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
            val key = "2024-$formattedMonth-$formattedDay"

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

        Log.d("MonthlyStatisticsFragment", "Entries count: ${entries.size}")
    }

    private fun observeViewModel() {
        viewModel.records.observe(viewLifecycleOwner){
            val current = DateUtils.convertDateToLocalDate(Date())
            viewModel.filterDataByMonth(current.monthValue, current.year)
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
                setUpLineChart(it1)
            }
        }
    }

    private fun showMonthPickerBottomSheet() {
        val bottomSheet = MonthPickerBottomSheetFragment { month, year ->
            binding.filterText.text = DateUtils.getFormattedMonthYear(month, year)
            viewModel.setSelectedTime(month + 1, year)
            viewModel.filterDataByMonth(month + 1, year)
        }
        bottomSheet.show(parentFragmentManager, bottomSheet.tag)
    }
}
