package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.api.record.RecordApiService
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.databinding.ActivityRunnerContestBinding
import com.university.marathononline.ui.adapter.ContestRunnerAdapter
import com.university.marathononline.ui.viewModel.ProfileViewModel
import com.university.marathononline.utils.ContestUserStatusManager
import com.university.marathononline.utils.finishAndGoBack
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RunnerContestActivity : BaseActivity<ProfileViewModel, ActivityRunnerContestBinding>() {

    private lateinit var adapter: ContestRunnerAdapter
    private var allContests: List<Contest> = emptyList()
    private var userEmail: String = ""

    private val contestStatusOptions = listOf(
        "Tất cả trạng thái",
        "Đang diễn ra",
        "Đã hoàn thành",
        "Đã kết thúc"
    )

    private val userStatusOptions = listOf(
        "Tất cả tình trạng",
        "Chưa thanh toán",
        "Đang tham gia",
        "Đã hoàn thành",
        "Bị chặn",
        "Đã kết thúc"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getMyContest()

        initializeUI()
        setupFilters()
        setUpObserve()
    }

    private fun setUpAdapter(contests: List<Contest>) {
        lifecycleScope.launch {
            userEmail = userPreferences.email.first() ?: ""
            adapter = ContestRunnerAdapter(contests, userEmail)
            binding.rvContests.adapter = adapter
        }
    }

    private fun setUpObserve() {
        viewModel.getMyContestResponse.observe(this) {
            when(it){
                is Resource.Success -> {
                    setUpAdapter(it.value.contests)
                    allContests = it.value.contests
                    applyFilters()
                }
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }
    }

    private fun initializeUI() {
        binding.rvContests.layoutManager = LinearLayoutManager(this)

        binding.buttonBack.setOnClickListener {
            finishAndGoBack()
        }

        // Toggle filter visibility
        binding.buttonFilter.setOnClickListener {
            val isVisible = binding.filterSection.visibility == View.VISIBLE
            binding.filterSection.visibility = if (isVisible) View.GONE else View.VISIBLE
            binding.buttonFilter.setImageResource(
                if (isVisible) R.drawable.ic_filter else R.drawable.ic_filter_active
            )
        }

        // Clear filters
        binding.buttonClearFilters.setOnClickListener {
            clearFilters()
        }
    }

    private fun setupFilters() {
        val contestStatusAdapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, contestStatusOptions)
        (binding.tilContestStatus.editText as? AutoCompleteTextView)?.apply {
            setAdapter(contestStatusAdapter)
            setText(contestStatusOptions[0], false)
            setOnItemClickListener { _, _, _, _ ->
                applyFilters()
            }
        }

        val userStatusAdapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, userStatusOptions)
        (binding.tilUserStatus.editText as? AutoCompleteTextView)?.apply {
            setAdapter(userStatusAdapter)
            setText(userStatusOptions[0], false)
            setOnItemClickListener { _, _, _, _ ->
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val contestStatusFilter = (binding.tilContestStatus.editText as? AutoCompleteTextView)?.text?.toString()
        val userStatusFilter = (binding.tilUserStatus.editText as? AutoCompleteTextView)?.text?.toString()

        var filteredContests = allContests

        if (contestStatusFilter != null && contestStatusFilter != contestStatusOptions[0]) {
            filteredContests = filteredContests.filter { contest ->
                when (contestStatusFilter) {
                    "Đang diễn ra" -> contest.status == EContestStatus.ACTIVE
                    "Đã hoàn thành" -> contest.status == EContestStatus.COMPLETED
                    "Đã kết thúc" -> contest.status == EContestStatus.FINISHED
                    else -> true
                }
            }
        }

        if (userStatusFilter != null && userStatusFilter != userStatusOptions[0]) {
            filteredContests = filteredContests.filter { contest ->
                val userRegistration = contest.registrations?.find { it.runner.email == userEmail }
                val statusManager = ContestUserStatusManager(contest, userRegistration)
                val userStatus = statusManager.getUserContestStatus()

                when (userStatusFilter) {
                    "Chưa thanh toán" -> userStatus == ContestUserStatusManager.UserContestStatus.REGISTERED_UNPAID ||
                            userStatus == ContestUserStatusManager.UserContestStatus.PAYMENT_FAILED ||
                            userStatus == ContestUserStatusManager.UserContestStatus.PAYMENT_PENDING
                    "Đang tham gia" -> userStatus == ContestUserStatusManager.UserContestStatus.REGISTERED_ACTIVE
                    "Đã hoàn thành" -> userStatus == ContestUserStatusManager.UserContestStatus.REGISTRATION_COMPLETED
                    "Bị chặn" -> userStatus == ContestUserStatusManager.UserContestStatus.REGISTERED_BLOCKED
                    "Đã kết thúc" -> userStatus == ContestUserStatusManager.UserContestStatus.CONTEST_EXPIRED
                    else -> true
                }
            }
        }

        adapter.updateData(filteredContests)
        updateFilterResultText(filteredContests.size, allContests.size)
    }

    private fun clearFilters() {
        (binding.tilContestStatus.editText as? AutoCompleteTextView)?.setText(contestStatusOptions[0], false)
        (binding.tilUserStatus.editText as? AutoCompleteTextView)?.setText(userStatusOptions[0], false)
        applyFilters()
    }

    private fun updateFilterResultText(filteredCount: Int, totalCount: Int) {
        binding.tvFilterResult.text = "Hiển thị $filteredCount/$totalCount cuộc thi"
        binding.tvFilterResult.visibility = if (filteredCount != totalCount) View.VISIBLE else View.GONE
    }

    override fun getViewModel(): Class<ProfileViewModel> = ProfileViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater): ActivityRunnerContestBinding {
        return ActivityRunnerContestBinding.inflate(inflater)
    }

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiAuth = retrofitInstance.buildApi(AuthApiService::class.java, token)
        val apiRecord = retrofitInstance.buildApi(RecordApiService::class.java, token)
        val apiContest = retrofitInstance.buildApi(ContestApiService::class.java, token)
        return listOf(
            AuthRepository(apiAuth, userPreferences),
            RecordRepository(apiRecord), ContestRepository(apiContest)
        )
    }
}