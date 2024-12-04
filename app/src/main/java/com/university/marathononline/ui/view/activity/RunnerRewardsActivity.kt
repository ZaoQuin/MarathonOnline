package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.models.Reward
import com.university.marathononline.databinding.ActivityRunnerRewardsBinding
import com.university.marathononline.ui.adapter.RunnerRewardAdapter
import com.university.marathononline.ui.viewModel.RunnerRewardsViewModel
import com.university.marathononline.utils.KEY_REWARDS
import com.university.marathononline.utils.finishAndGoBack

class RunnerRewardsActivity  : BaseActivity<RunnerRewardsViewModel, ActivityRunnerRewardsBinding>() {

    private lateinit var adapter: RunnerRewardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras(intent)

        setUpAdapter()
        initializeUI()
        setUpObserve()
    }

    private fun setUpAdapter() {
        binding.rvRewards.layoutManager = LinearLayoutManager(this)
        adapter = RunnerRewardAdapter(emptyList())
        binding.rvRewards.adapter = adapter
    }

    private fun setUpObserve() {
        viewModel.rewards.observe(this) {
            viewModel.rewards.value?.let { it1 -> adapter.updateData(it1) }
        }
    }

    private fun initializeUI() {
        binding.buttonBack.setOnClickListener{
            finishAndGoBack()
        }
    }

    private fun handleIntentExtras(intent: Intent) {
        intent.apply {
            viewModel.apply {
                (getSerializableExtra(KEY_REWARDS) as? List<Reward>)?.let { setRewards(it) }
            }
        }
    }

    override fun getViewModel(): Class<RunnerRewardsViewModel> = RunnerRewardsViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater): ActivityRunnerRewardsBinding {
        return ActivityRunnerRewardsBinding.inflate(inflater)
    }

    override fun getActivityRepositories(): List<BaseRepository> {
        return listOf()
    }
}