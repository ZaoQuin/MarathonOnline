package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.university.marathononline.databinding.FragmentYearPickerBottomSheetBinding
import java.util.Calendar

class YearPickerBottomSheetFragment(private val onYearSelected: (Int) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentYearPickerBottomSheetBinding
    private lateinit var yearSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentYearPickerBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yearSpinner = binding.yearSpinner

        setupYearSpinner()

        binding.btnSelect.setOnClickListener {
            val selectedYear = yearSpinner.selectedItem.toString().toInt()
            onYearSelected(selectedYear)
            dismiss()
        }
    }

    private fun setupYearSpinner() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (2023..currentYear).toList() // Lấy năm từ 2023 đến hiện tại

        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = yearAdapter

        yearSpinner.setSelection(years.indexOf(currentYear)) // Đặt chọn năm hiện tại
    }
}
