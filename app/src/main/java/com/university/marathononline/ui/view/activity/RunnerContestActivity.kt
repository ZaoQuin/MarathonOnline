package com.university.marathononline.ui.view.activity

import android.content.Intent
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
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.databinding.ActivityRunnerContestBinding
import com.university.marathononline.ui.adapter.ContestRunnerAdapter
import com.university.marathononline.ui.viewModel.RunnerContestsViewModel
import com.university.marathononline.utils.ContestUserStatusManager
import com.university.marathononline.utils.KEY_CONTESTS
import com.university.marathononline.utils.finishAndGoBack
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RunnerContestActivity : BaseActivity<RunnerContestsViewModel, ActivityRunnerContestBinding>() {

    private lateinit var adapter: ContestRunnerAdapter
    private var allContests: List<Contest> = emptyList()
    private var userEmail: String = ""

    // Filter options
    private val contestStatusOptions = listOf(
        "Tất cả trạng thái",
        "Đang chờ",
        "Đang diễn ra",
        "Đã hoàn thành",
        "Đã kết thúc"
    )

    private val userStatusOptions = listOf(
        "Tất cả tình trạng",
        "Chưa đăng ký",
        "Chưa thanh toán",
        "Đang tham gia",
        "Đã hoàn thành",
        "Bị chặn",
        "Đã kết thúc"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras(intent)

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
        viewModel.contests.observe(this) { contests ->
            contests?.let {
                allContests = it
                applyFilters()
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
        // Setup Contest Status Filter
        val contestStatusAdapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, contestStatusOptions)
        (binding.tilContestStatus.editText as? AutoCompleteTextView)?.apply {
            setAdapter(contestStatusAdapter)
            setText(contestStatusOptions[0], false)
            setOnItemClickListener { _, _, _, _ ->
                applyFilters()
            }
        }

        // Setup User Status Filter
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

        // Filter by contest status
        if (contestStatusFilter != null && contestStatusFilter != contestStatusOptions[0]) {
            filteredContests = filteredContests.filter { contest ->
                when (contestStatusFilter) {
                    "Đang chờ" -> contest.status == EContestStatus.PENDING
                    "Đang diễn ra" -> contest.status == EContestStatus.ACTIVE
                    "Đã hoàn thành" -> contest.status == EContestStatus.COMPLETED
                    "Đã kết thúc" -> contest.status == EContestStatus.FINISHED
                    else -> true
                }
            }
        }

        // Filter by user status
        if (userStatusFilter != null && userStatusFilter != userStatusOptions[0]) {
            filteredContests = filteredContests.filter { contest ->
                val userRegistration = contest.registrations?.find { it.runner.email == userEmail }
                val statusManager = ContestUserStatusManager(contest, userRegistration)
                val userStatus = statusManager.getUserContestStatus()

                when (userStatusFilter) {
                    "Chưa đăng ký" -> userStatus == ContestUserStatusManager.UserContestStatus.NOT_REGISTERED
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

    private fun handleIntentExtras(intent: Intent) {
        intent.apply {
            viewModel.apply {
                (getSerializableExtra(KEY_CONTESTS) as? List<Contest>)?.let {
                    setUpAdapter(it)
                    setContests(it)
                }
            }
        }
    }

    override fun getViewModel(): Class<RunnerContestsViewModel> = RunnerContestsViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater): ActivityRunnerContestBinding {
        return ActivityRunnerContestBinding.inflate(inflater)
    }

    override fun getActivityRepositories(): List<BaseRepository> {
        return listOf()
    }
}