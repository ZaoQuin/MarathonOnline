package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.models.Registration
import com.university.marathononline.databinding.FragmentLeaderBoardBinding
import com.university.marathononline.ui.adapter.LeaderBoardAdapter
import com.university.marathononline.ui.viewModel.LeaderBoardViewModel
import com.university.marathononline.utils.KEY_REGISTRATIONS
import com.university.marathononline.utils.formatDistance

class LeaderBoardFragment : BaseFragment<LeaderBoardViewModel, FragmentLeaderBoardBinding>() {

    private lateinit var adapter: LeaderBoardAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (arguments?.getSerializable(KEY_REGISTRATIONS) as? List<Registration>)?.let { viewModel.setRegistrations(it) }
        binding.leaderBoards.layoutManager = LinearLayoutManager(requireContext())
        observe()
    }

    private fun observe() {
        viewModel.registrations.observe(viewLifecycleOwner) {
            viewModel.rankUsers()
        }

        viewModel.rankUsers.observe(viewLifecycleOwner) {
            adapter = LeaderBoardAdapter(it)
            binding.leaderBoards.adapter = adapter
        }

        viewModel.top1.observe(viewLifecycleOwner) {
            if(it!=null)
                binding.apply {
                    top1Name.text = it.runner.fullName
                    top1Distance.text = it.records?.sumOf { it1 -> it1.distance }
                        ?.let { it2 -> formatDistance(it2) }
                }

        }

        viewModel.top2.observe(viewLifecycleOwner) {
            if(it!=null)
                binding.apply {
                    top2Name.text = it.runner.fullName
                    top2Distance.text = it.records?.sumOf { it1 -> it1.distance }
                        ?.let { it2 -> formatDistance(it2) }
                }
        }

        viewModel.top3.observe(viewLifecycleOwner) {
            if(it!=null)
                binding.apply {
                    top3Name.text = it.runner.fullName
                    top3Distance.text = it.records?.sumOf { it1 -> it1.distance }
                        ?.let { it2 -> formatDistance(it2) }
                }
        }
    }

    fun updateRegistrations(newRegistrations: List<Registration>?) {
        if (newRegistrations != null) {
            viewModel.setRegistrations(newRegistrations)
            viewModel.rankUsers()

            adapter.updateData(viewModel.rankUsers.value ?: emptyList())

            Log.d("LeaderBoardFragment", "Updated with ${newRegistrations.size} registrations")
        }
    }

    override fun getViewModel(): Class<LeaderBoardViewModel> {
        return LeaderBoardViewModel::class.java
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLeaderBoardBinding {
        return FragmentLeaderBoardBinding.inflate(inflater, container, false)
    }

    override fun getFragmentRepositories(): List<BaseRepository> {
        return listOf()
    }
}
