package com.university.marathononline.ui.view.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.ui.viewModel.ContestViewModel
import com.university.marathononline.ui.adapter.ContestAdapter
import com.university.marathononline.databinding.FragmentContestBinding
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.ui.view.activity.SearchActivity
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ContestFragment : BaseFragment<ContestViewModel, FragmentContestBinding>() {

    private lateinit var adapter: ContestAdapter

    override fun getViewModel() = ContestViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentContestBinding.inflate(inflater, container, false)

    override fun getFragmentRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(ContestApiService::class.java, token)
        return listOf(ContestRepository(api))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.contests.layoutManager = LinearLayoutManager(requireContext())

        viewModel.getActiveContests()
        setAdapter()
        setSearchButton()

        observe()
    }

    private fun setAdapter() {
        adapter = ContestAdapter(emptyList())
        binding.contests.adapter = adapter
    }

    private fun setSearchButton() {
        binding.btnSearch.setOnClickListener{
            val intent = Intent(binding.root.context, SearchActivity::class.java)
            binding.root.context.startActivity(intent)
        }
    }

    private fun observe() {
        viewModel.contests.observe(viewLifecycleOwner) {
            Log.d("ContestFragment", it.toString())
            when(it){
                is Resource.Success -> {
                    if (it.value.contests.isEmpty()) {
                        Toast.makeText(requireContext(), "No active contests found.", Toast.LENGTH_SHORT).show()
                    } else {
                        adapter.updateData(it.value.contests)
                    }
                }
                is Resource.Failure -> {
                    handleApiError(it)
                    it.fetchErrorMessage()
                }
                else -> Unit
            }
        }
    }
}