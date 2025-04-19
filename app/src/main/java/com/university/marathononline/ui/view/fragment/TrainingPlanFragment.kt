package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.databinding.FragmentTrainingPlanBinding
import com.university.marathononline.ui.viewModel.TrainingPlanViewModel

class TrainingPlanFragment : BaseFragment<TrainingPlanViewModel, FragmentTrainingPlanBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getViewModel() = TrainingPlanViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTrainingPlanBinding.inflate(inflater, container, false)

    override fun getFragmentRepositories(): List<BaseRepository> {
        return listOf()
    }

}