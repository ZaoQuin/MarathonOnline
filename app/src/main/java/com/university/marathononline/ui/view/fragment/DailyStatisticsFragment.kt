package com.university.marathononline.ui.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.university.marathononline.R
import com.university.marathononline.data.models.User
import com.university.marathononline.databinding.FragmentDailyStatisticsBinding
import com.university.marathononline.ui.viewModel.DailyStatisticsViewModel
import com.university.marathononline.ui.components.DatePickerBottomSheetFragment
import com.university.marathononline.utils.DateUtils
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyStatisticsFragment : Fragment() {

    private lateinit var binding: FragmentDailyStatisticsBinding
    private val viewModel: DailyStatisticsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDailyStatisticsBinding.inflate(inflater, container, false)

        initUI()

        return binding.root
    }

    private fun initUI() {
        binding.filterText.text = DateUtils.getCurrentDate()
        binding.filterButton.setOnClickListener { showDatePickerBottomSheet() }

        setUpLineChart()
    }

    private fun setUpLineChart() {
        binding.apply {
            // Configure X and Y axes for the Line Chart
            val xAxis = lineChart.xAxis
            val leftAxis = lineChart.axisLeft
            val rightAxis = lineChart.axisRight

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setLabelCount(3, true)  // Three time blocks: Morning, Afternoon, Evening
                textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
                gridColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
                axisLineColor = ContextCompat.getColor(requireContext(), R.color.dark_main_color)
            }

            leftAxis.apply {
                textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
                gridColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
                axisLineColor = ContextCompat.getColor(requireContext(), R.color.dark_main_color)
            }

            rightAxis.isEnabled = false

            // Prepare data for line chart with morning, afternoon, evening time blocks
            val entries = ArrayList<Entry>()
            // Example: Adding dummy data for each time block
            entries.add(Entry(0f, 2f)) // Morning
            entries.add(Entry(1f, 3f)) // Afternoon
            entries.add(Entry(2f, 1f)) // Evening

            val dataSet = LineDataSet(entries, "Lượt chạy")
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

            // Set the data for the line chart
            val lineData = LineData(dataSet)
            lineChart.data = lineData

            // Update the chart's appearance
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
            viewModel.filterDataByWeek(day)
        }

        bottomSheet.show(parentFragmentManager, bottomSheet.tag)
    }
}