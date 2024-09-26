package com.university.marathononline.profile.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
}