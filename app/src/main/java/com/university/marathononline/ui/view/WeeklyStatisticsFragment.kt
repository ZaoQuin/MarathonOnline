package com.university.marathononline.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.university.marathononline.R
import com.university.marathononline.databinding.FragmentWeeklyStatisticsBinding
import com.university.marathononline.ui.viewModel.WeeklyStatisticsViewModel
import com.university.marathononline.ui.components.DatePickerBottomSheetFragment
import com.university.marathononline.utils.DateUtils

class WeeklyStatisticsFragment : Fragment() {

    private lateinit var binding: FragmentWeeklyStatisticsBinding
    private val viewModel: WeeklyStatisticsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeeklyStatisticsBinding.inflate(inflater, container, false)

        initUI()

        return binding.root
    }

    private fun initUI() {
        binding.filterText.text = DateUtils.getCurrentDate()
        binding.filterButton.setOnClickListener { showDatePickerBottomSheet() }

        setUpBarChart()
    }

    lateinit var barChart: BarChart
    lateinit var barData: BarData
    lateinit var barDataSet: BarDataSet
    lateinit var barEntriesList: ArrayList<BarEntry>

    private fun setUpBarChart() {
        barChart = binding.barChart

        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e != null) {
                    val value = e.y
                    val xValue = e.x

                    Toast.makeText(requireContext(), "Giá trị: $value, Vị trí: $xValue", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected() {
                // Không có hành động khi không click vào cột nào
            }
        })

        barEntriesList = ArrayList()


        barEntriesList.add(BarEntry(1f,1f))
        barEntriesList.add(BarEntry(2f,2f))
        barEntriesList.add(BarEntry(3f,3f))
        barEntriesList.add(BarEntry(4f,4f))
        barEntriesList.add(BarEntry(5f,5f))
        barEntriesList.add(BarEntry(6f,6f))
        barEntriesList.add(BarEntry(7f,7f))

        barDataSet = BarDataSet(barEntriesList, "Quãng đường")
        barData = BarData(barDataSet)

        barChart.data = barData
        barChart.invalidate()

        barDataSet.setDrawValues(true)
        barDataSet.valueTextColor = R.color.dark_main_color
        barDataSet.setColors(resources.getColor(R.color.light_main_color))
        barDataSet.valueTextSize = 16f
        barChart.description.isEnabled = false
    }

    private fun showDatePickerBottomSheet() {
        val bottomSheet = DatePickerBottomSheetFragment {selectedDate ->
            binding.filterText.text = DateUtils.getFormattedDate(selectedDate)
            viewModel.filterDataByWeek(selectedDate)
        }

        bottomSheet.show(parentFragmentManager, bottomSheet.tag)
    }
}