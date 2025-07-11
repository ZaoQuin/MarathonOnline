package com.university.marathononline.ui.components

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.university.marathononline.databinding.DialogFilterTrainingPlanBinding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class FilterTrainingPlanDialog(
    context: Context,
    private val initialStartDate: LocalDateTime? = null,
    private val initialEndDate: LocalDateTime? = null,
    private val onApplyFilter: (startDate: LocalDateTime?, endDate: LocalDateTime?) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogFilterTrainingPlanBinding
    private var startDate: LocalDateTime? = initialStartDate
    private var endDate: LocalDateTime? = initialEndDate

    // Formatter chỉ dùng để hiển thị ngày cho người dùng
    private val displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogFilterTrainingPlanBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // Set the dialog width to match parent with margins
        val width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
        window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT) // -2 is WRAP_CONTENT

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Set initial values if they exist
        startDate?.let {
            binding.tvStartDate.text = it.format(displayDateFormatter)
        }

        endDate?.let {
            binding.tvEndDate.text = it.format(displayDateFormatter)
        }
    }

    private fun setupListeners() {
        // Start date picker
        binding.layoutStartDate.setOnClickListener {
            val currentDate = startDate?.toLocalDate() ?: LocalDate.now()

            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    startDate = LocalDateTime.of(selectedDate, LocalTime.MIDNIGHT)
                    binding.tvStartDate.text = selectedDate.format(displayDateFormatter)
                },
                currentDate.year,
                currentDate.monthValue - 1, // DatePickerDialog uses 0-11 for months
                currentDate.dayOfMonth
            ).apply {
                show()
            }
        }

        // End date picker
        binding.layoutEndDate.setOnClickListener {
            val currentDate = endDate?.toLocalDate() ?: LocalDate.now()

            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    // Set end date to end of day (23:59:59)
                    endDate = LocalDateTime.of(selectedDate, LocalTime.of(23, 59, 59))
                    binding.tvEndDate.text = selectedDate.format(displayDateFormatter)
                },
                currentDate.year,
                currentDate.monthValue - 1,
                currentDate.dayOfMonth
            ).apply {
                show()
            }
        }

        // Reset button
        binding.btnReset.setOnClickListener {
            startDate = null
            endDate = null
            binding.tvStartDate.text = ""
            binding.tvStartDate.hint = "Chọn ngày bắt đầu"
            binding.tvEndDate.text = ""
            binding.tvEndDate.hint = "Chọn ngày kết thúc"
        }

        // Apply button
        binding.btnApply.setOnClickListener {
            // Validate date range
            if (startDate != null && endDate != null) {
                if (startDate!!.isAfter(endDate)) {
                    Toast.makeText(context, "Ngày bắt đầu không thể sau ngày kết thúc", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Call the callback with selected dates
            onApplyFilter(startDate, endDate)
            dismiss()
        }
    }
}