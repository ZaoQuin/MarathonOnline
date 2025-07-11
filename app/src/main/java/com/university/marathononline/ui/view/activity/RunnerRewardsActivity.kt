package com.university.marathononline.ui.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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
import com.university.marathononline.utils.finishAndGoBack
import com.university.marathononline.utils.visible
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RunnerRewardsActivity : BaseActivity<RunnerRewardsViewModel, ActivityRunnerRewardsBinding>() {

    private lateinit var adapter: RunnerRewardAdapter
    private var contestId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showLoading(true)

        lifecycleScope.launch {
            val emailValue = userPreferences.email.first()
            viewModel.setEmail(emailValue!!)
        }

        viewModel.loadAllUserRewards()
        setUpAdapter()
        initializeUI()
        setUpObserve()
    }

    private fun showLoading(show: Boolean){
        binding.apply{
            if(show){
                binding.shimmerViewContainer.startShimmer()
                binding.shimmerViewContainer.visible(true)
                binding.rvRewards.visible(false)
            } else {

                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visible(false)
                binding.rvRewards.visible(true)
            }
        }
    }

    private fun setUpAdapter() {
        binding.rvRewards.layoutManager = LinearLayoutManager(this)
        adapter = RunnerRewardAdapter(mutableListOf()) { reward, contest ->
            onRewardItemClick(reward, contest)
        }
        binding.rvRewards.adapter = adapter
    }

    private fun setUpObserve() {
        viewModel.rewardOfContests.observe(this) { rewardList ->
            val sortedRewards = sortRewardsByImportance(rewardList)
            adapter.updateData(sortedRewards.toMutableList())

            showLoading(false)
        }

        viewModel.contests.observe(this) { contests ->
            viewModel.setRewardOfContest(contests)
        }

        viewModel.getContest.observe(this) {
            when(it) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    viewModel.setRewardOfContest(listOf(it.value))
                    showLoading(false)
                }
                is Resource.Failure -> {
                    handleApiError(it)
                    showLoading(false)
                }
                else -> Unit
            }
        }

        viewModel.getContests.observe(this) {
            when(it) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    viewModel.setRewardOfContest(it.value.contests)
                    showLoading(false)
                }
                is Resource.Failure -> {
                    handleApiError(it)
                    showLoading(false)
                }
                else -> Unit
            }
        }
    }

    private fun initializeUI() {
        binding.buttonBack.setOnClickListener {
            finishAndGoBack()
        }

        val titleText = if (contestId != null) {
            "Phần thưởng cuộc thi"
        } else {
            "Các giải thưởng của tôi"
        }
        binding.titleText?.text = titleText
    }

    private fun sortRewardsByImportance(rewards: List<Pair<Contest, Reward>>): List<Pair<Contest, Reward>> {
        return rewards.sortedWith(compareBy<Pair<Contest, Reward>> { pair ->
            when (pair.second.rewardRank) {
                0 -> Int.MAX_VALUE
                else -> pair.second.rewardRank
            }
        }.thenBy { pair ->
            pair.first.endDate
        })
    }

    private fun onRewardItemClick(reward: Reward, contest: Contest) {
        // Handle reward item click
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.shimmerViewContainer.stopShimmer()
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