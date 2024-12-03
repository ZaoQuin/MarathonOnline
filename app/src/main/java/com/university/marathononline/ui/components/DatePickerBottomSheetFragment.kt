package com.university.marathononline.ui.components

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.university.marathononline.databinding.FragmentDatePickerBottomSheetBinding
import java.util.Date

class DatePickerBottomSheetFragment(
    private val onDateSelected: (Date) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentDatePickerBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDatePickerBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendarView = binding.calendarView

        val currentDate = Calendar.getInstance().time
        calendarView.date = currentDate.time

        val calendar = Calendar.getInstance()

        calendarView.setDate(calendar.timeInMillis, true, true)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.time

            onDateSelected.invoke(selectedDate)
        }
    }
}

