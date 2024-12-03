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
import com.university.marathononline.databinding.FragmentDailyStatisticsBinding
import com.university.marathononline.ui.viewModel.DailyStatisticsViewModel
import com.university.marathononline.ui.components.DatePickerBottomSheetFragment
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.data.repository.RaceRepository
import com.university.marathononline.utils.KEY_RACES
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Date

class DailyStatisticsFragment : BaseFragment<DailyStatisticsViewModel, FragmentDailyStatisticsBinding>() {

    override fun getViewModel(): Class<DailyStatisticsViewModel> = DailyStatisticsViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentDailyStatisticsBinding {
        return FragmentDailyStatisticsBinding.inflate(inflater, container, false)
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
            viewModel.filterDataByDay(Date())
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
                Log.d("DailyStatisticsFragment", it1.toString())
            }
        }
    }

    private fun initUI() {
        binding.filterText.text = DateUtils.getCurrentDate()
        binding.filterButton.setOnClickListener { showDatePickerBottomSheet() }
    }

    private fun setUpLineChart(races: List<Race>) {
        binding.apply {
            val xAxis = lineChart.xAxis
            val leftAxis = lineChart.axisLeft
            val rightAxis = lineChart.axisRight

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setLabelCount(3, true)
                textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
                gridColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
                axisLineColor = ContextCompat.getColor(requireContext(), R.color.dark_main_color)
            }

            leftAxis.apply {
                textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
                gridColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
                axisLineColor = ContextCompat.getColor(requireContext(), R.color.dark_main_color)
                axisMinimum = 0f
            }

            rightAxis.isEnabled = false

            val hourlyDistances = mutableMapOf<Int, Double>()

            races.forEach { race ->
                val raceTime = DateUtils.convertStringToLocalDateTime(race.timestamp)
                val hour = raceTime.hour

                hourlyDistances[hour] = hourlyDistances.getOrDefault(hour, 0.0) + race.distance
            }

            val entries = ArrayList<Entry>()
            for (hour in 0..23) {
                val distance = hourlyDistances.getOrDefault(hour, 0.0)
                entries.add(Entry(hour.toFloat(), distance.toFloat()))
            }

            val dataSet = LineDataSet(entries, "Quá trình chạy hằng giờ")
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
            }
        }
    }



    private fun showDatePickerBottomSheet() {
        val bottomSheet = DatePickerBottomSheetFragment() { day ->
            binding.filterText.text = DateUtils.getFormattedDate(day)
            viewModel.filterDataByDay(day)
        }

        bottomSheet.show(parentFragmentManager, bottomSheet.tag)
    }
}
