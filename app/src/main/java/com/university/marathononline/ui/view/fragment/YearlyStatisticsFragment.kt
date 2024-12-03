package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.university.marathononline.R
import com.university.marathononline.databinding.FragmentYearlyStatisticsBinding
import com.university.marathononline.ui.viewModel.YearlyStatisticsViewModel
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.race.RaceApiService
import com.university.marathononline.data.models.Race
import com.university.marathononline.data.repository.RaceRepository
import com.university.marathononline.utils.KEY_RACES
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.Calendar
import java.util.Date

class YearlyStatisticsFragment : BaseFragment<YearlyStatisticsViewModel, FragmentYearlyStatisticsBinding>() {

    override fun getViewModel(): Class<YearlyStatisticsViewModel> = YearlyStatisticsViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentYearlyStatisticsBinding {
        return FragmentYearlyStatisticsBinding.inflate(inflater, container, false)
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

    private fun observeViewModel() {
        viewModel.races.observe(viewLifecycleOwner){
            val current = DateUtils.convertDateToLocalDate(Date())
            viewModel.filterDataByYear(current.year)
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
                Log.d("YearlyStatisticsFragment", it1.toString())
                setUpLineChart(it1)
            }
        }
    }

    private fun initUI() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        binding.filterText.text = DateUtils.getFormattedYear(currentYear)

        binding.filterButton.setOnClickListener {
            showYearPickerBottomSheet()
        }

    }

    private fun setUpLineChart(race: Map<String, String>) {
        binding.apply {
            val lineChart = binding.lineChart

            val xAxis = lineChart.xAxis
            val leftAxis = lineChart.axisLeft
            val rightAxis = lineChart.axisRight

            xAxis.apply {
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setLabelCount(12, true)
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

            // Gắn các giá trị vào các tháng
            for (month in 1..12) {
                // Kiểm tra nếu có giá trị cho tháng này trong `race`
                val distance = race["2024-${month.toString().padStart(2, '0')}"]?.toFloat() ?: 0f
                entries.add(Entry(month.toFloat(), distance))
            }

            // Tạo dataset
            val dataSet = LineDataSet(entries, "Quá trình chạy hàng tháng")
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
        val yearPicker = YearPickerBottomSheetFragment { selectedYear ->
            binding.filterText.text = DateUtils.getFormattedYear(selectedYear)
            viewModel.filterDataByYear(selectedYear)
        }
        yearPicker.show(parentFragmentManager, "YearPicker")
    }
}
