package com.university.marathononline.ui.view.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.databinding.ActivityAddContestBinding
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.ui.adapter.RewardAdapter
import com.university.marathononline.ui.adapter.RuleAdapter
import com.university.marathononline.ui.components.AddRewardDialog
import com.university.marathononline.ui.components.AddRuleDialog
import com.university.marathononline.ui.viewModel.AddContestViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import java.util.Calendar

class AddContestActivity : BaseActivity<AddContestViewModel, ActivityAddContestBinding>() {

    private val ruleAdapter = RuleAdapter(mutableListOf())
    private val rewardAdapter = RewardAdapter(mutableListOf())

    override fun getViewModel(): Class<AddContestViewModel> {
        return AddContestViewModel::class.java
    }

    override fun getActivityBinding(inflater: LayoutInflater): ActivityAddContestBinding {
        return ActivityAddContestBinding.inflate(inflater)
    }

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiContest = retrofitInstance.buildApi(ContestApiService::class.java, token)
        return listOf(
            ContestRepository(apiContest)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up RecyclerView for rules and rewards
        binding.recyclerRules.layoutManager = LinearLayoutManager(this)
        binding.recyclerRules.adapter = ruleAdapter

        binding.recyclerRewards.layoutManager = LinearLayoutManager(this)
        binding.recyclerRewards.adapter = rewardAdapter

        // DatePicker for Start Date
        binding.btnStartDate.setOnClickListener {
            showDatePicker { year, month, dayOfMonth ->
                binding.btnStartDate.text = "$dayOfMonth/${month + 1}/$year"
            }
        }

        // DatePicker for End Date
        binding.btnEndDate.setOnClickListener {
            showDatePicker { year, month, dayOfMonth ->
                binding.btnEndDate.text = "$dayOfMonth/${month + 1}/$year"
            }
        }

        // DatePicker for Registration Deadline
        binding.btnRegistrationDeadline.setOnClickListener {
            showDatePicker { year, month, dayOfMonth ->
                binding.btnRegistrationDeadline.text = "$dayOfMonth/${month + 1}/$year"
            }
        }

        // Add Rule button
        binding.btnAddRule.setOnClickListener {
            val addRuleDialog = AddRuleDialog(this) { rule ->
//                ruleAdapter.addRule(rule)
            }
            addRuleDialog.show()
        }

        // Add Reward button
        binding.btnAddReward.setOnClickListener {
            val addRewardDialog = AddRewardDialog(this) { reward ->
//                rewardAdapter.addReward(reward)
            }
            addRewardDialog.show()
        }

        // Save Contest button
        binding.btnSaveContest.setOnClickListener {
            // Collect all contest details
            val contestDetails = Contest(
                name = binding.etContestName.text.toString(),
                description = binding.etContestDescription.text.toString(),
                distance = binding.etContestDistance.text.toString().toDoubleOrNull(),
                startDate = binding.btnStartDate.text.toString(),
                endDate = binding.btnEndDate.text.toString(),
                registrationDeadline = binding.btnRegistrationDeadline.text.toString(),
                fee = BigDecimal(binding.etContestFee.text.toString()),
                maxMembers = binding.etMaxMembers.text.toString().toIntOrNull(),
                status = EContestStatus.PENDING, // Default status
//                rules = ruleAdapter.getRules(),
//                rewards = rewardAdapter.getRewards()
                rules = emptyList(),
                rewards = emptyList()
            )

            // Call ViewModel to save the contest
//            viewModel.addContest(contestDetails)
        }
    }

    private fun showDatePicker(onDateSetListener: (Int, Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this, { _, selectedYear, selectedMonth, selectedDay ->
                onDateSetListener(selectedYear, selectedMonth, selectedDay)
            }, year, month, day
        )
        datePickerDialog.show()
    }
}