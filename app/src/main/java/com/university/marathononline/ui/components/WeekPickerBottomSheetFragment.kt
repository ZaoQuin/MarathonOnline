package com.university.marathononline.ui.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.university.marathononline.R
import com.university.marathononline.databinding.FragmentWeekPickerBottomSheetBinding
import java.text.SimpleDateFormat
import java.util.*

class WeekPickerBottomSheetFragment(
    private val currentWeek: String? = null,
    private val onWeekSelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentWeekPickerBottomSheetBinding
    private lateinit var weekSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeekPickerBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        weekSpinner = binding.weekSpinner

        setupSpinner()

        binding.btnSelect.setOnClickListener {
            val selectedWeek = weekSpinner.selectedItem.toString()
            onWeekSelected(selectedWeek)
            dismiss()
        }
    }

    private fun setupSpinner() {
        val weeks = generateWeeks()

        val weekAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, weeks)
        weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        weekSpinner.adapter = weekAdapter

        val currentSelection = if (currentWeek != null) {
            weeks.indexOf(currentWeek).takeIf { it >= 0 } ?: 0
        } else {
            0
        }
        weekSpinner.setSelection(currentSelection)
    }

    private fun generateWeeks(): List<String> {
        val calendar = Calendar.getInstance()
        val weeks = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        for (i in 0 until 52) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val startDate = calendar.time

            calendar.add(Calendar.DAY_OF_YEAR, 6)
            val endDate = calendar.time

            val weekLabel = "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
            weeks.add(weekLabel)

            calendar.add(Calendar.DAY_OF_YEAR, -13)
        }

        return weeks
    }
}