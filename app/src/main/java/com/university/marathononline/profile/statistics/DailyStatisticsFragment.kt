package com.university.marathononline.profile.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.university.marathononline.databinding.FragmentDailyStatisticsBinding
import com.university.marathononline.profile.components.DatePickerBottomSheetFragment
import com.university.marathononline.utils.DateUtils

class DailyStatisticsFragment : Fragment() {

    private lateinit var binding: FragmentDailyStatisticsBinding
    private val viewModel: DailyStatisticsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDailyStatisticsBinding.inflate(inflater, container, false)

        initUI()

        return binding.root
    }

    private fun initUI() {
        binding.filterText.text = DateUtils.getCurrentDate()
        binding.filterButton.setOnClickListener { showDatePickerBottomSheet() }
    }

    private fun showDatePickerBottomSheet() {
        val bottomSheet = DatePickerBottomSheetFragment {selectedDate ->
            binding.filterText.text = DateUtils.getFormattedDate(selectedDate)
            viewModel.filterDataByDate(selectedDate)
        }

        bottomSheet.show(parentFragmentManager, bottomSheet.tag)
    }
}