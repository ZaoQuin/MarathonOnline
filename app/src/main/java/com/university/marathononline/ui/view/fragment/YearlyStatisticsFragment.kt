package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.university.marathononline.R
import com.university.marathononline.databinding.FragmentYearlyStatisticsBinding
import com.university.marathononline.ui.viewModel.MonthlyStatisticsViewModel
import com.university.marathononline.utils.DateUtils
import java.util.Calendar

class YearlyStatisticsFragment : Fragment() {

    private lateinit var binding: FragmentYearlyStatisticsBinding
    private val viewModel: MonthlyStatisticsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentYearlyStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
    }

    private fun initUI() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        binding.filterText.text = DateUtils.getFormattedYear(currentYear)

        binding.filterButton.setOnClickListener {
            showYearPickerBottomSheet()
        }
    }

    private fun setUpLineChart() {
        binding.apply {
        // Configure the X and Y axes for the LineChart
            val xAxis = lineChart.xAxis
            val leftAxis = lineChart.axisLeft
            val rightAxis = lineChart.axisRight

            // Configure X-axis (months of the year)
            xAxis.apply {
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setLabelCount(12, true)  // Labeling each month
                textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
                gridColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
                axisLineColor = ContextCompat.getColor(requireContext(), R.color.dark_main_color)
                granularity = 1f
                isGranularityEnabled = true
            }

            // Configure Y-axis
            leftAxis.apply {
                textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
                gridColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
                axisLineColor = ContextCompat.getColor(requireContext(), R.color.dark_main_color)
            }

            rightAxis.isEnabled = false  // Disable right Y-axis

            // Prepare the data entries for the months
            val entries = ArrayList<Entry>()
            for (i in 1..12) {
                val month = i  // Month number (1 to 12)
                val value = getDataForMonth(month) // Get the data for each month (implement the logic here)

                // Add data for the month
                entries.add(Entry(month.toFloat(), value.toFloat()))
            }

            // Create the LineDataSet for the LineChart
            val dataSet = LineDataSet(entries, "Quá trình chạy hàng tháng")
            dataSet.apply {
                color = ContextCompat.getColor(requireContext(), R.color.main_color) // Line color
                lineWidth = 2f
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.light_main_color)) // Circle color
                circleRadius = 5f
                setDrawFilled(true)  // Enable filling under the line
                fillColor = ContextCompat.getColor(requireContext(), R.color.light_main_color) // Fill color
                fillAlpha = 80  // Transparency for fill
                mode = LineDataSet.Mode.CUBIC_BEZIER  // Smooth cubic bezier curve
            }

            // Set data for the LineChart
            val lineData = LineData(dataSet)
            lineChart.data = lineData

            // Update the chart appearance
            lineChart.apply {
                setDrawGridBackground(false)  // Disable background grid
                description.isEnabled = false  // Disable description
                legend.apply {
                    isEnabled = true
                    textColor = ContextCompat.getColor(requireContext(), R.color.text_color) // Legend text color
                }
                setTouchEnabled(true)  // Enable interaction
                animateXY(1500, 1500)  // Animation for chart rendering
                setPinchZoom(true)  // Enable pinch zoom
                setScaleEnabled(true)  // Enable scaling
            }
        }
    }

    private fun getDataForMonth(month: Int): Int {
        return month * 3
    }

    // Open the Year Picker BottomSheet
    private fun showYearPickerBottomSheet() {
        val yearPicker = YearPickerBottomSheetFragment { selectedYear ->
            binding.filterText.text = DateUtils.getFormattedYear(selectedYear)
            updateChartForYear(selectedYear)
        }
        yearPicker.show(parentFragmentManager, "YearPicker")
    }

    // Update the chart data based on the selected year
    private fun updateChartForYear(year: Int) {
        // Logic to update the chart data for the selected year
        // You can fetch or generate data for the selected year here
        val entries = ArrayList<Entry>()
        for (i in 1..12) {
            val month = i  // Month number (1 to 12)
            val value = getDataForMonth(month) // Replace this with actual data for the selected year

            // Add data for the month
            entries.add(Entry(month.toFloat(), value.toFloat()))
        }

        // Update the dataset with the new data
        val dataSet = LineDataSet(entries, "Quá trình chạy hàng tháng - $year")
        dataSet.apply {
            color = ContextCompat.getColor(requireContext(), R.color.main_color) // Line color
            lineWidth = 2f
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.light_main_color)) // Circle color
            circleRadius = 5f
            setDrawFilled(true)  // Enable filling under the line
            fillColor = ContextCompat.getColor(requireContext(), R.color.light_main_color) // Fill color
            fillAlpha = 80  // Transparency for fill
            mode = LineDataSet.Mode.CUBIC_BEZIER  // Smooth cubic bezier curve
        }

        // Set new data for the LineChart
        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate() // Refresh the chart
    }
}
