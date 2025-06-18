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
import com.university.marathononline.data.api.training.TrainingDayApiService
import com.university.marathononline.data.api.training.TrainingFeedbackApiService
import com.university.marathononline.data.api.training.TrainingPlanApiService
import com.university.marathononline.data.repository.TrainingDayRepository
import com.university.marathononline.data.repository.TrainingFeedbackRepository
import com.university.marathononline.data.repository.TrainingPlanRepository
import com.university.marathononline.databinding.ActivityTrainingPlanHistoryBinding
import com.university.marathononline.ui.adapter.SingleTrainingPlanAdapter
import com.university.marathononline.ui.components.FilterTrainingPlanDialog
import com.university.marathononline.ui.viewModel.TrainingPlanViewModel
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.startNewActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class TrainingPlanHistoryActivity: BaseActivity<TrainingPlanViewModel, ActivityTrainingPlanHistoryBinding>() {

    private lateinit var adapter: SingleTrainingPlanAdapter
    private var currentPage = 0
    private val ITEMS_PER_PAGE = 10
    private var isLastPage = false
    private var isLoading = false

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

        binding.filterButton.setOnClickListener {
            showFilterDialog()
        }

        binding.archivedButton.setOnClickListener {
            startNewActivity(ArchivedPlanActivity::class.java)
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView(){
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerCompletedPlans.layoutManager = layoutManager
        adapter = SingleTrainingPlanAdapter(emptyList())
        binding.recyclerCompletedPlans.adapter = adapter

        // Implement infinite scrolling with pagination
        binding.recyclerCompletedPlans.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

        viewModel.getCompletedTrainingPlans(currentPage, ITEMS_PER_PAGE, startDateStr, endDateStr)
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

        viewModel.getCompletedTrainingPlans(currentPage, ITEMS_PER_PAGE, startDateStr, endDateStr)
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
        binding.recyclerCompletedPlans.visibility = if (show) View.GONE else View.VISIBLE
        binding.emptyCompletedState.visibility = View.GONE
    }

    private fun showLoadingMore(show: Boolean) {
        binding.loadingMoreContainer.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        if (show) {
            binding.recyclerCompletedPlans.visibility = View.GONE
            binding.emptyCompletedState.visibility = View.VISIBLE
        } else {
            binding.recyclerCompletedPlans.visibility = View.VISIBLE
            binding.emptyCompletedState.visibility = View.GONE
        }
    }

    override fun getViewModel() = TrainingPlanViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityTrainingPlanHistoryBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiTrainingPlan = retrofitInstance.buildApi(TrainingPlanApiService::class.java, token)
        val apiTrainingDay = retrofitInstance.buildApi(TrainingDayApiService::class.java, token)
        val apiTrainingFeedback = retrofitInstance.buildApi(TrainingFeedbackApiService::class.java, token)
        return listOf(TrainingPlanRepository(apiTrainingPlan), TrainingDayRepository(apiTrainingDay),
            TrainingFeedbackRepository(apiTrainingFeedback)
        )
    }
}