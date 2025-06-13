package com.university.marathononline.ui.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.university.marathononline.R
import com.university.marathononline.databinding.FragmentMonthPickerBottomSheetBinding
import java.util.Calendar

class MonthPickerBottomSheetFragment(private val currentMonth: Int, private val currentYear: Int,
                                     private val onMonthYearSelected: (Int, Int) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentMonthPickerBottomSheetBinding
    private lateinit var monthSpinner: Spinner
    private lateinit var yearSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMonthPickerBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        monthSpinner = binding.monthSpinner
        yearSpinner = binding.yearSpinner

        setupSpinners()

        binding.btnSelect.setOnClickListener {
            val selectedMonth = monthSpinner.selectedItemPosition
            val selectedYear = yearSpinner.selectedItem.toString().toInt()

            onMonthYearSelected(selectedMonth, selectedYear)

            dismiss()
        }
    }

    private fun setupSpinners() {
        val months = resources.getStringArray(R.array.months)

        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = monthAdapter

        val currentCalendarYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (2023..currentCalendarYear).toList()
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = yearAdapter

        // Sử dụng currentMonth và currentYear được truyền vào thay vì Calendar.getInstance()
        monthSpinner.setSelection(currentMonth)
        yearSpinner.setSelection(years.indexOf(currentYear))
    }
}