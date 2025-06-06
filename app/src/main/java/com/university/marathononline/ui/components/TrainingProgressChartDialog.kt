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

        // Inflate the layout with View Binding
        binding = DialogTrainingProgressChartBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // Setup dialog properties
        setupDialog()

        // Setup the chart
        setupChart()

        // Setup close button
        binding.closeButton.setOnClickListener {
            dismiss()
        }

        // Adjust dialog width to 90% of screen width
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
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
            binding.progressChart.visibility = ViewGroup.GONE
            // Optionally, show a TextView with "No data available"
            Log.d("TrainingProgressChart", "No training days available")
            return
        }

        // Prepare chart data
        val actualEntries = mutableListOf<Entry>()
        val goalEntries = mutableListOf<Entry>()
        val dateLabels = mutableListOf<String>()
        var maxDistance = 0f

        // Sort training days by date and process
        trainingDays.sortedBy { DateUtils.convertStringToLocalDateTime(it.dateTime) }
            .forEachIndexed { index, trainingDay ->
                // Calculate total actual distance from records (0 if no records)
                val actualDistance = trainingDay.record.distance.toFloat()
                // Goal distance from the session
                val goalDistance = trainingDay.session.distance.toFloat()

                // Add entries for both actual and goal distances
                actualEntries.add(Entry(index.toFloat(), actualDistance))
                goalEntries.add(Entry(index.toFloat(), goalDistance))

                // Track maximum distance for Y-axis scaling
                maxDistance = maxOf(maxDistance, actualDistance, goalDistance)

                // Create label: "Ngày X, Tuần Y"
                val label = "Ngày ${trainingDay.dayOfWeek}, Tuần ${trainingDay.week}"
                dateLabels.add(label)
                Log.d("TrainingProgressChart", "Label $index: $label")
            }

        // Log to verify labels
        Log.d("TrainingProgressChart", "Date labels: $dateLabels")

        // Setup the line chart
        val chart = binding.progressChart
        setupLineChart(chart, actualEntries, goalEntries, dateLabels, maxDistance)
    }

    private fun setupLineChart(
        chart: LineChart,
        actualEntries: List<Entry>,
        goalEntries: List<Entry>,
        dateLabels: List<String>,
        maxDistance: Float
    ) {
        // Actual distance dataset
        val actualDataSet = LineDataSet(actualEntries, "Quãng đường thực tế").apply {
            color = ContextCompat.getColor(context, R.color.main_color)
            setCircleColor(ContextCompat.getColor(context, R.color.main_color))
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextColor = ContextCompat.getColor(context, R.color.main_color) // Use distinct color
            valueTextSize = 10f
            setDrawValues(true) // Enable value labels
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()} km" // Format as integer km
                }
            }
        }

        // Goal distance dataset
        val goalDataSet = LineDataSet(goalEntries, "Quãng đường mục tiêu").apply {
            color = ContextCompat.getColor(context, R.color.white)
            setCircleColor(ContextCompat.getColor(context, R.color.white))
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextColor = ContextCompat.getColor(context, R.color.white) // Use distinct color
            valueTextSize = 10f
            setDrawValues(true) // Enable value labels
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()} km" // Format as integer km
                }
            }
        }

        // Combine datasets
        val lineData = LineData(actualDataSet, goalDataSet)
        chart.data = lineData

        // Customize x-axis with "Ngày X, Tuần Y" labels
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(dateLabels)
            granularity = 1f
            isGranularityEnabled = true
            setLabelCount(dateLabels.size, true)
            textColor = ContextCompat.getColor(context, R.color.white)
            textSize = 12f
            setDrawGridLines(false)
            setAvoidFirstLastClipping(true)
            labelRotationAngle = 45f
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(true)
        }

        // Custom Y-axis formatter to display distances in km
        val kmFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()} km"
            }
        }

        // Customize y-axis
        chart.axisLeft.apply {
            textColor = ContextCompat.getColor(context, R.color.white)
            textSize = 12f
            setDrawGridLines(true)
            axisMinimum = 0f
            axisMaximum = (maxDistance * 1.2f).coerceAtLeast(5f)
            valueFormatter = kmFormatter
            granularity = 1f
        }
        chart.axisRight.isEnabled = false

        // Chart appearance
        chart.apply {
            description.text = "Tiến độ tập luyện (km)"
            description.textColor = ContextCompat.getColor(context, R.color.white)
            description.textSize = 12f
            setDrawGridBackground(false)
            setBackgroundColor(ContextCompat.getColor(context, R.color.gray))
            legend.textColor = ContextCompat.getColor(context, R.color.white)
            legend.textSize = 12f
            animateY(1000)
            setExtraBottomOffset(30f)
            invalidate()
        }
    }
}