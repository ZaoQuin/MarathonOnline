package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.trainingPlan.TrainingPlanApiService
import com.university.marathononline.data.repository.TrainingPlanRepository
import com.university.marathononline.databinding.ActivityArchivedPlanBinding
import com.university.marathononline.ui.adapter.SingleTrainingPlanAdapter
import com.university.marathononline.ui.components.FilterTrainingPlanDialog
import com.university.marathononline.ui.viewModel.TrainingPlanViewModel
import com.university.marathononline.utils.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class ArchivedPlanActivity : BaseActivity<TrainingPlanViewModel, ActivityArchivedPlanBinding>() {
    private lateinit var adapter: SingleTrainingPlanAdapter
    private var currentPage = 0
    private val ITEMS_PER_PAGE = 10
    private var isLastPage = false
    private var isLoading = false

    // Filter variables
    private var startDate: LocalDateTime? = null
    private var endDate: LocalDateTime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
        setupListeners()
        loadInitialData()
    }

    private fun setupViews(){
        binding.btnBack.setOnClickListener { onBackPressed() }

        setupRecyclerView()
    }

    private fun setupRecyclerView(){
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerArchivedPlans.layoutManager = layoutManager
        adapter = SingleTrainingPlanAdapter(emptyList())
        binding.recyclerArchivedPlans.adapter = adapter

        // Implement infinite scrolling with pagination
        binding.recyclerArchivedPlans.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Check if we need to load more data
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= ITEMS_PER_PAGE) {
                        loadMoreTrainingPlans()
                    }
                }
            }
        })
    }

    private fun setupListeners() {
        // No additional listeners to set up right now
    }

    private fun loadInitialData() {
        currentPage = 0
        isLastPage = false
        showLoading(true)

        // Use DateUtils to format LocalDateTime to ISO-8601 for API
        val startDateStr = DateUtils.formatToApiDateTimeString(startDate)
        val endDateStr = DateUtils.formatToApiDateTimeString(endDate)

        viewModel.getArchivedTrainingPlans(currentPage, ITEMS_PER_PAGE, startDateStr, endDateStr)
        observeViewModel()
    }

    private fun loadMoreTrainingPlans(){
        if (isLoading || isLastPage) return

        isLoading = true
        showLoadingMore(true)
        currentPage++

        // Use DateUtils to format LocalDateTime to ISO-8601 for API
        val startDateStr = DateUtils.formatToApiDateTimeString(startDate)
        val endDateStr = DateUtils.formatToApiDateTimeString(endDate)

        viewModel.getArchivedTrainingPlans(currentPage, ITEMS_PER_PAGE, startDateStr, endDateStr)
    }

    private fun observeViewModel() {
        viewModel.getTrainingPlans.observe(this) { resource ->
            when(resource) {
                is Resource.Loading -> {
                    if (currentPage == 0) {
                        showLoading(true)
                    } else {
                        showLoadingMore(true)
                    }
                }
                is Resource.Success -> {
                    val pageResult = resource.value
                    isLastPage = pageResult.last
                    isLoading = false

                    if (currentPage == 0) {
                        showLoading(false)
                        adapter.updateData(pageResult.content)
                        showEmptyState(pageResult.content.isEmpty())
                    } else {
                        showLoadingMore(false)
                        adapter.addData(pageResult.content)
                    }
                }
                is Resource.Failure -> {
                    isLoading = false
                    showLoading(false)
                    showLoadingMore(false)
                    // Handle error
                    Toast.makeText(this, "Failed to load training plans", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showFilterDialog() {
        FilterTrainingPlanDialog(
            context = this,
            initialStartDate = startDate,
            initialEndDate = endDate,
            onApplyFilter = { newStartDate, newEndDate ->
                // Update filter values and reload data
                startDate = newStartDate
                endDate = newEndDate
                loadInitialData()
            }
        ).show()
    }

    private fun showLoading(show: Boolean) {
        binding.loadingState.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerArchivedPlans.visibility = if (show) View.GONE else View.VISIBLE
        binding.emptyState.visibility = View.GONE
    }

    private fun showLoadingMore(show: Boolean) {
        binding.loadingMoreContainer.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        if (show) {
            binding.recyclerArchivedPlans.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerArchivedPlans.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
        }
    }

    override fun getViewModel() = TrainingPlanViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityArchivedPlanBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(TrainingPlanApiService::class.java, token)
        return listOf(TrainingPlanRepository(api))
    }
}