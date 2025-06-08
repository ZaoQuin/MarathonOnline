package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.Reward
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.databinding.ActivityRunnerRewardsBinding
import com.university.marathononline.ui.adapter.RunnerRewardAdapter
import com.university.marathononline.ui.viewModel.RunnerRewardsViewModel
import com.university.marathononline.utils.KEY_CONTEST_ID
import com.university.marathononline.utils.KEY_EMAIL
import com.university.marathononline.utils.finishAndGoBack
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RunnerRewardsActivity : BaseActivity<RunnerRewardsViewModel, ActivityRunnerRewardsBinding>() {

    private lateinit var adapter: RunnerRewardAdapter
    private var contestId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras(intent)

        setUpAdapter()
        initializeUI()
        setUpObserve()

        // Load data based on contestId if provided
        contestId?.let {
            viewModel.loadRewardsForContest(it)
        } ?: run {
            viewModel.loadAllUserRewards()
        }
    }

    private fun setUpAdapter() {
        binding.rvRewards.layoutManager = LinearLayoutManager(this)
        adapter = RunnerRewardAdapter(mutableListOf()) { reward, contest ->
            // Handle reward item click
            onRewardItemClick(reward, contest)
        }
        binding.rvRewards.adapter = adapter
    }

    private fun setUpObserve() {
        // Fixed: Remove duplicate observer
        viewModel.rewardOfContests.observe(this) { rewardList ->
            val sortedRewards = sortRewardsByImportance(rewardList)
            adapter.updateData(sortedRewards.toMutableList())
        }

        viewModel.contests.observe(this) { contests ->
            viewModel.setRewardOfContest(contests)
        }

//        viewModel.isLoading.observe(this) { isLoading ->
//            // Handle loading state
//            binding.progressBar?.let {
//                it.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
//            }
//        }

        viewModel.getContest.observe(this) {
            when(it) {
                is Resource.Success -> {
                    viewModel.setRewardOfContest(listOf(it.value))
                }

                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }

        viewModel.getContests.observe(this) {
            when(it) {
                is Resource.Success -> {
                    viewModel.setRewardOfContest(it.value.contests)
                }

                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }
    }

    private fun initializeUI() {
        binding.buttonBack.setOnClickListener {
            finishAndGoBack()
        }

        // Update title based on context
        val titleText = if (contestId != null) {
            "Phần thưởng cuộc thi"
        } else {
            "Các giải thưởng của tôi"
        }
        binding.titleText?.text = titleText
    }

    private fun handleIntentExtras(intent: Intent) {
        intent.apply {
            // Get contest ID for specific contest rewards
            contestId = getLongExtra(KEY_CONTEST_ID, -1L).takeIf { it != -1L }

            // Get email if needed
            lifecycleScope.launch {
                val email = userPreferences.email.first()
                email?.let { viewModel.setEmail(it) }
            }
        }
    }

    private fun sortRewardsByImportance(rewards: List<Pair<Contest, Reward>>): List<Pair<Contest, Reward>> {
        return rewards.sortedWith(compareBy<Pair<Contest, Reward>> { pair ->
            // Sort by rank (lower rank = higher importance)
            when (pair.second.rewardRank) {
                0 -> Int.MAX_VALUE // Completion rewards go last
                else -> pair.second.rewardRank
            }
        }.thenBy { pair ->
            // Then by contest end date (newer first)
            pair.first.endDate
        })
    }

    private fun onRewardItemClick(reward: Reward, contest: Contest) {
        // Navigate to contest details or reward details
        // You can add specific logic here
    }

    override fun getViewModel(): Class<RunnerRewardsViewModel> = RunnerRewardsViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater): ActivityRunnerRewardsBinding {
        return ActivityRunnerRewardsBinding.inflate(inflater)
    }

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val api = retrofitInstance.buildApi(ContestApiService::class.java, token)
        return listOf(
            ContestRepository(api)
        )
    }
}