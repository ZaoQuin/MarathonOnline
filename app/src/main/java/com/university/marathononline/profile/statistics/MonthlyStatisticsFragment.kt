package com.university.marathononline.profile.statistics

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
import com.university.marathononline.databinding.FragmentMonthlyStatisticsBinding

class MonthlyStatisticsFragment : Fragment() {

    private lateinit var binding: FragmentMonthlyStatisticsBinding
    private val viewModel: MonthlyStatisticsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMonthlyStatisticsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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



        for (i in 1..30) {
            barEntriesList.add(BarEntry(i.toFloat(), (i * 2).toFloat()))
        }

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
}