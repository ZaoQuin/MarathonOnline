package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.databinding.FragmentLeaderBoardBinding
import com.university.marathononline.ui.viewModel.LeaderBoardViewModel
import com.university.marathononline.ui.adapter.LeaderBoardAdapter

class LeaderBoardFragment : Fragment() {

    private lateinit var binding: FragmentLeaderBoardBinding
    private val viewModel: LeaderBoardViewModel by viewModels()
    private lateinit var adapter: LeaderBoardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLeaderBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.leaderBoards.layoutManager = LinearLayoutManager(requireContext())

        observe()
    }

    private fun observe() {
        viewModel.eventHistories.observe(viewLifecycleOwner, Observer { eventHistories ->
            viewModel.users.observe(viewLifecycleOwner, Observer {
                adapter = LeaderBoardAdapter(eventHistories)
                binding.leaderBoards.adapter = adapter
            })
        })
    }
}