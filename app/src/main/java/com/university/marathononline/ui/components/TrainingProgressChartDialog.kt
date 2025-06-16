package com.university.marathononline.ui.components

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.university.marathononline.R
import com.university.marathononline.data.models.TrainingDay
import com.university.marathononline.databinding.DialogTrainingProgressChartBinding
import com.university.marathononline.utils.DateUtils
import androidx.core.content.ContextCompat
import kotlin.math.max

class TrainingProgressChartDialog(
    context: Context,
    private val trainingDays: List<TrainingDay>
) : Dialog(context) {

    private lateinit var binding: DialogTrainingProgressChartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogTrainingProgressChartBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        setupDialog()

        setupChart()

        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.95).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupDialog() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

    private fun setupChart() {
        if (trainingDays.isEmpty()) {
            showNoDataState()
            return
        }

        binding.progressChart.visibility = ViewGroup.VISIBLE
        binding.noDataContainer.visibility = ViewGroup.GONE
        binding.legendContainer.visibility = ViewGroup.VISIBLE

        val actualEntries = mutableListOf<Entry>()
        val goalEntries = mutableListOf<Entry>()
        val dateLabels = mutableListOf<String>()
        var maxDistance = 0f
        var totalActual = 0f
        var totalGoal = 0f

        trainingDays.sortedBy { DateUtils.convertStringToLocalDateTime(it.dateTime) }
            .forEachIndexed { index, trainingDay ->
                val actualDistance = trainingDay.record?.distance?.toFloat() ?: 0f
                val goalDistance = trainingDay.session.distance.toFloat()

                totalActual += actualDistance
                totalGoal += goalDistance

                actualEntries.add(Entry(index.toFloat(), actualDistance))
                goalEntries.add(Entry(index.toFloat(), goalDistance))

                maxDistance = maxOf(maxDistance, actualDistance, goalDistance)

                val label = "T${trainingDay.week}N${trainingDay.dayOfWeek}"
                dateLabels.add(label)
                Log.d("TrainingProgressChart", "Label $index: $label")
            }

        updateSummaryStats(totalActual, totalGoal)

        val chart = binding.progressChart
        setupLineChart(chart, actualEntries, goalEntries, dateLabels, maxDistance)
    }

    private fun showNoDataState() {
        binding.progressChart.visibility = ViewGroup.GONE
        binding.noDataContainer.visibility = ViewGroup.VISIBLE
        binding.legendContainer.visibility = ViewGroup.GONE

        binding.totalActualDistance.text = "0 km"
        binding.totalGoalDistance.text = "0 km"

        Log.d("TrainingProgressChart", "No training days available")
    }

    private fun updateSummaryStats(totalActual: Float, totalGoal: Float) {
        binding.totalActualDistance.text = String.format("%.2f km", totalActual)
        binding.totalGoalDistance.text = String.format("%.2f km", totalGoal)
    }

    private fun setupLineChart(
        chart: LineChart,
        actualEntries: List<Entry>,
        goalEntries: List<Entry>,
        dateLabels: List<String>,
        maxDistance: Float
    ) {
        val actualDataSet = LineDataSet(actualEntries, "Thực tế").apply {
            color = ContextCompat.getColor(context, R.color.success_color)
            setCircleColor(ContextCompat.getColor(context, R.color.success_color))
            lineWidth = 3f
            circleRadius = 5f
            setDrawCircleHole(false)
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(context, R.color.success_color)
            fillAlpha = 50
            valueTextColor = ContextCompat.getColor(context, R.color.success_color)
            valueTextSize = 10f
            setDrawValues(true)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value > 0) String.format("%.2fkm", value) else ""
                }
            }
        }

        val goalDataSet = LineDataSet(goalEntries, "Mục tiêu").apply {
            color = ContextCompat.getColor(context, R.color.warning_orange)
            setCircleColor(ContextCompat.getColor(context, R.color.warning_orange))
            lineWidth = 3f
            circleRadius = 5f
            setDrawCircleHole(false)
            enableDashedLine(10f, 5f, 0f)
            valueTextColor = ContextCompat.getColor(context, R.color.warning_orange)
            valueTextSize = 10f
            setDrawValues(true)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value > 0) String.format("%.2fkm", value) else ""
                }
            }
        }

        val lineData = LineData(actualDataSet, goalDataSet)
        chart.data = lineData

        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(dateLabels)
            granularity = 1f
            isGranularityEnabled = true
            setLabelCount(minOf(dateLabels.size, 8), false)
            textColor = ContextCompat.getColor(context, R.color.text_dark)
            textSize = 10f
            setDrawGridLines(false)
            setAvoidFirstLastClipping(true)
            labelRotationAngle = 0f
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(true)
        }

        val kmFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}"
            }
        }

        chart.axisLeft.apply {
            textColor = ContextCompat.getColor(context, R.color.text_dark)
            textSize = 10f
            setDrawGridLines(true)
            gridColor = ContextCompat.getColor(context, R.color.divider_color)
            axisMinimum = 0f
            axisMaximum = (maxDistance * 1.2f).coerceAtLeast(5f)
            valueFormatter = kmFormatter
            granularity = 1f
        }
        chart.axisRight.isEnabled = false

        chart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setBackgroundColor(Color.TRANSPARENT)
            legend.isEnabled = false
            animateY(1000)
            setExtraOffsets(10f, 20f, 10f, 10f)
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            invalidate()
        }
    }
}