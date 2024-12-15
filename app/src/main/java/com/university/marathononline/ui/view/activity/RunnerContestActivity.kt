package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.models.Contest
import com.university.marathononline.databinding.ActivityRunnerContestBinding
import com.university.marathononline.ui.adapter.ContestRunnerAdapter
import com.university.marathononline.ui.viewModel.RunnerContestsViewModel
import com.university.marathononline.utils.KEY_CONTESTS
import com.university.marathononline.utils.finishAndGoBack
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RunnerContestActivity : BaseActivity<RunnerContestsViewModel, ActivityRunnerContestBinding>() {

    private lateinit var adapter: ContestRunnerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras(intent)

        initializeUI()
        setUpObserve()
    }

    private fun setUpAdapter(contests: List<Contest>) {
        lifecycleScope.launch {
            val emailValue = userPreferences.email.first() ?: "" // Ensure email is fetched safely
            adapter = ContestRunnerAdapter(contests, emailValue)
            binding.rvContests.adapter = adapter
        }
    }

    private fun setUpObserve() {
        viewModel.contests.observe(this) { contests ->
            contests?.let { adapter.updateData(it) }
        }
    }

    private fun initializeUI() {
        binding.rvContests.layoutManager = LinearLayoutManager(this)

        binding.buttonBack.setOnClickListener {
            finishAndGoBack()
        }
    }

    private fun handleIntentExtras(intent: Intent) {
        intent.apply {
            viewModel.apply {
                (getSerializableExtra(KEY_CONTESTS) as? List<Contest>)?.let {
                    setContests(it)
                    setUpAdapter(it)
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
