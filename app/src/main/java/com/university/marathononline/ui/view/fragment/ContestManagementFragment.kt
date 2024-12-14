package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.Toast.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.databinding.FragmentContestManagementBinding
import com.university.marathononline.ui.adapter.EditContestAdapter
import com.university.marathononline.ui.viewModel.ContestManagementViewModel
import com.university.marathononline.utils.SORT_BY_ASC
import com.university.marathononline.utils.SORT_BY_DES
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ContestManagementFragment :
    BaseFragment<ContestManagementViewModel, FragmentContestManagementBinding>() {

    private lateinit var contestAdapter: EditContestAdapter

    override fun getViewModel(): Class<ContestManagementViewModel> =
        ContestManagementViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentContestManagementBinding =
        FragmentContestManagementBinding.inflate(inflater, container, false)

    override fun getFragmentRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiContest = retrofitInstance.buildApi(ContestApiService::class.java, token)
        return listOf(ContestRepository(apiContest))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getMyContest()

        setupRecyclerView()
        setupSearch()
        setupFiltersAndSorting()
        observeContestData()

        showSkeletonLoading()
    }

    private fun showSkeletonLoading() {
        binding.apply {
            shimmerLayout.startShimmer()
            shimmerLayout.visibility = View.VISIBLE
            recyclerViewContests.visibility = View.GONE
        }
    }

    private fun hideSkeletonLoading() {
        binding.apply {
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            recyclerViewContests.visibility = View.VISIBLE
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.getMyContest()
    }

    private fun setupRecyclerView() {
        contestAdapter = EditContestAdapter(emptyList())
        binding.recyclerViewContests.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewContests.adapter = contestAdapter
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchKey(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupFiltersAndSorting() {
        val statusSpinner = binding.statusSpinner
        val statusOptions =
            arrayOf("Tất cả",
                EContestStatus.PENDING.value,
                EContestStatus.ACTIVE.value,
                EContestStatus.FINISHED.value,
                EContestStatus.CANCELLED.value,
                EContestStatus.NOT_APPROVAL.value,
                EContestStatus.COMPLETED.value)
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusSpinner.adapter = statusAdapter

        statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                val status = when (position) {
                    0 -> null
                    1 -> EContestStatus.PENDING
                    2 -> EContestStatus.ACTIVE
                    3 -> EContestStatus.FINISHED
                    4 -> EContestStatus.CANCELLED
                    5 -> EContestStatus.NOT_APPROVAL
                    6 -> EContestStatus.COMPLETED
                    else -> null
                }

                viewModel.setFilterStatus(status)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        val sortSpinner = binding.sortSpinner
        val sortOptions = arrayOf("Mới nhất", "Cũ nhất")
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = sortAdapter

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                val sortedContests = when (position) {
                    0 -> SORT_BY_DES
                    1 -> SORT_BY_ASC
                    else -> null
                }

                viewModel.setSortType(sortedContests)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }

    private fun observeContestData() {
        viewModel.getContestByJwtResponse.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success -> {
                    viewModel.setContest(it.value)
                    viewModel.setResult(it.value)
                    hideSkeletonLoading()
                }
                is Resource.Failure -> {
                    handleApiError(it)
                }
                else -> Unit
            }
        }

        viewModel.results.observe(viewLifecycleOwner) {
            contestAdapter.updateData(it)
        }

        viewModel.keySearch.observe(viewLifecycleOwner) {
            viewModel.applySearchAndFiltersAndSort()
        }

        viewModel.filterStatus.observe(viewLifecycleOwner) {
            viewModel.applySearchAndFiltersAndSort()
        }

        viewModel.sortType.observe(viewLifecycleOwner) {
            viewModel.applySearchAndFiltersAndSort()
        }
    }
}
