package com.university.marathononline.profile.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.university.marathononline.databinding.FragmentPickerBottomSheetBinding

class MonthPickerBottomSheet (
    private val onMonthSelected: (Int, Int) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentPickerBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPickerBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}