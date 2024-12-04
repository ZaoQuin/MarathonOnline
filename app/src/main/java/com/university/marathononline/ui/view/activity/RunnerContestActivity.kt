package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.models.Contest
import com.university.marathononline.databinding.ActivityRunnerContestBinding
import com.university.marathononline.ui.adapter.ContestRunnerAdapter
import com.university.marathononline.ui.viewModel.RunnerContestsViewModel
import com.university.marathononline.utils.KEY_CONTESTS
import com.university.marathononline.utils.finishAndGoBack

class RunnerContestActivity : BaseActivity<RunnerContestsViewModel, ActivityRunnerContestBinding>() {

    private lateinit var adapter: ContestRunnerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras(intent)

        setUpAdapter()
        initializeUI()
        setUpObserve()
    }

    private fun setUpAdapter() {
        binding.rvContests.layoutManager = LinearLayoutManager(this)
        adapter = ContestRunnerAdapter(emptyList(), userPreferences.email.toString())
        binding.rvContests.adapter = adapter
    }

    private fun setUpObserve() {
        viewModel.contests.observe(this) {
            viewModel.contests.value?.let { it1 -> adapter.updateData(it1) }
        }
    }

    private fun initializeUI() {
        binding.rvContests.layoutManager = LinearLayoutManager(this)
        binding.rvContests.adapter = adapter

        binding.buttonBack.setOnClickListener{
            finishAndGoBack()
        }
    }

    private fun handleIntentExtras(intent: Intent) {
        intent.apply {
            viewModel.apply {
                (getSerializableExtra(KEY_CONTESTS) as? List<Contest>)?.let { setContests(it) }
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