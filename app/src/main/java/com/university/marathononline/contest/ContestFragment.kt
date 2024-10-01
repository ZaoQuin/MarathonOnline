package com.university.marathononline.contest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.contest.adapter.ContestAdapter
import com.university.marathononline.databinding.FragmentContestBinding
import com.university.marathononline.entity.Contest

class ContestFragment : Fragment() {

    private lateinit var binding: FragmentContestBinding
    private val viewModel: ContestViewModel by activityViewModels()
    private lateinit var adapter: ContestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.contests.layoutManager = LinearLayoutManager(requireContext())
        adapter = ContestAdapter(emptyList())
        binding.contests.adapter = adapter

        observe()
    }

    private fun observe() {
        viewModel.contests.observe(viewLifecycleOwner) { contests: List<Contest>->
            adapter.updateData(contests)
        }
    }
}