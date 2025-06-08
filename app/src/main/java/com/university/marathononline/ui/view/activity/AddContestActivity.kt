package com.university.marathononline.ui.view.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.R.string.error_field_required
import com.university.marathononline.R.string.exist_email
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.contest.ContestApiService
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.data.models.Reward
import com.university.marathononline.data.models.Rule
import com.university.marathononline.data.repository.ContestRepository
import com.university.marathononline.data.request.CreateContestRequest
import com.university.marathononline.databinding.ActivityAddContestBinding
import com.university.marathononline.ui.adapter.EditRewardAdapter
import com.university.marathononline.ui.adapter.EditRuleAdapter
import com.university.marathononline.ui.components.AddRewardDialog
import com.university.marathononline.ui.components.AddRuleDialog
import com.university.marathononline.ui.viewModel.AddContestViewModel
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.KEY_UPDATE_CONTEST
import com.university.marathononline.utils.finishAndGoBack
import com.university.marathononline.utils.getMessage
import com.university.marathononline.utils.isABeforeB
import com.university.marathononline.utils.isEmpty
import com.university.marathononline.utils.isValueNull
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Calendar

class AddContestActivity : BaseActivity<AddContestViewModel, ActivityAddContestBinding>() {

    private var contestToEdit: Contest? = null

    private val ruleAdapter = EditRuleAdapter(
        rules = mutableListOf(),
        onEditClick = { rule -> handleEditRule(rule) },
        onDeleteClick = { rule -> handleDeleteRule(rule) }
    )

    private val rewardAdapter = EditRewardAdapter(
        rewards = mutableListOf(),
        onEditClick = { reward -> handleEditReward(reward) },
        onDeleteClick = { reward -> handleDeleteReward(reward) }
    )

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

        contestToEdit = intent.getSerializableExtra(KEY_CONTEST) as? Contest

        if (contestToEdit != null) {
            populateFieldsForEdit(contestToEdit!!)
        }

        setupInitUI()

        viewModel.checkNameResponse.observe(this){
            when(it){
                is Resource.Success -> {
                    if (!it.value.exists) {
                        binding.etContestNameErrorText.text = null
                        addContestHandle()
                    } else {
                        binding.etContestNameErrorText.text = "Tên cuộc thi đã tồn tại"
                    }
                }
                is Resource.Failure -> {
                    handleApiError(it)
                    it.fetchErrorMessage()
                }
                else -> Unit
            }
        }

        viewModel.addContestResponse.observe(this){
            Log.e("Add Contest", it.toString())
            when(it){
                is Resource.Success -> {
                    Toast.makeText(this, "Đã thêm", Toast.LENGTH_SHORT).show()
                    finishAndGoBack()
                }
                is Resource.Failure -> {
                    handleApiError(it)
                    it.fetchErrorMessage()
                }
                else -> Unit
            }
        }

        viewModel.updateContestResponse.observe(this){
            Log.e("Update Contest", it.toString())
            when(it){
                is Resource.Success -> {
                    Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show()
                    val resultIntent = Intent().apply {
                        putExtra(KEY_UPDATE_CONTEST, it.value)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
                is Resource.Failure -> {
                    handleApiError(it)
                    it.fetchErrorMessage()
                }
                else -> Unit
            }
        }
    }

    private fun setupInitUI() {
        binding.buttonBack.setOnClickListener {
            finishAndGoBack()
        }

        binding.recyclerRules.layoutManager = LinearLayoutManager(this)
        binding.recyclerRules.adapter = ruleAdapter

        binding.recyclerRewards.layoutManager = LinearLayoutManager(this)
        binding.recyclerRewards.adapter = rewardAdapter

        binding.btnStartDate.setOnClickListener {
            showDatePicker { year, month, dayOfMonth ->
                val localDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0, 0)
                binding.btnStartDate.text =
                    DateUtils.convertToVietnameseDate(localDateTime.toString())
                viewModel.selectedStartDate(localDateTime)
            }
        }

        binding.btnEndDate.setOnClickListener {
            showDatePicker { year, month, dayOfMonth ->
                val localDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0, 0)
                binding.btnEndDate.text =
                    DateUtils.convertToVietnameseDate(localDateTime.toString())
                viewModel.selectedEndDate(localDateTime)
            }
        }

        binding.btnRegistrationDeadline.setOnClickListener {
            showDatePicker { year, month, dayOfMonth ->
                val localDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0, 0)
                binding.btnRegistrationDeadline.text =
                    DateUtils.convertToVietnameseDate(localDateTime.toString())
                viewModel.selectedRegistrationDeadlineDate(localDateTime)
            }
        }

        binding.btnAddRule.setOnClickListener {
            val addRuleDialog = AddRuleDialog(
                this,
                onRuleAdded = { rule ->
                    ruleAdapter.updateData(ruleAdapter.getCurrentData() + rule)
                },
                onRuleUpdated = { updatedRule ->
                    val updatedRules = ruleAdapter.getCurrentData().map {
                        if (it.id == updatedRule.id) updatedRule else it
                    }
                    ruleAdapter.updateData(updatedRules)
                }
            )
            addRuleDialog.show()
        }

        binding.btnAddReward.setOnClickListener {
            val addRewardDialog = AddRewardDialog(this,
                onRewardAdded = { reward ->
                    val updatedRewards = rewardAdapter.getCurrentData() + reward
                    rewardAdapter.updateData(updatedRewards)
                },
                onRewardUpdated = { updatedReward ->
                    val updatedRewards = rewardAdapter.getCurrentData().map {
                        if (it.id == updatedReward.id) updatedReward else it
                    }
                    rewardAdapter.updateData(updatedRewards)
                }
            )
            addRewardDialog.show()
        }

        binding.btnSaveContest.setOnClickListener {
            if(!validateFields())
                return@setOnClickListener
            val name = binding.etContestName.text.toString()

            if (contestToEdit != null) {
                if (contestToEdit!!.name != name) {
                    viewModel.checkNameContest(name)
                } else {
                    addContestHandle()
                }
            } else {
                viewModel.checkNameContest(name)
            }
        }
    }

    private fun addContestHandle() {
        if(contestToEdit == null) {
            val contestDetails = CreateContestRequest(
                name = binding.etContestName.text.toString(),
                description = binding.etContestDescription.text.toString(),
                distance = binding.etContestDistance.text.toString().toDoubleOrNull(),
                startDate = viewModel.start.value.toString(),
                endDate = viewModel.end.value.toString(),
                registrationDeadline = viewModel.deadline.value.toString(),
                fee = BigDecimal(binding.etContestFee.text.toString()),
                maxMembers = binding.etMaxMembers.text.toString().toIntOrNull(),
                status = EContestStatus.PENDING,
                rules = ruleAdapter.getCurrentData(),
                rewards = rewardAdapter.getCurrentData()
            )
            viewModel.addContest(contestDetails)
        } else {
            contestToEdit!!.name = binding.etContestName.text.toString()
            contestToEdit!!.description = binding.etContestDescription.text.toString()
            contestToEdit!!.distance = binding.etContestDistance.text.toString().toDoubleOrNull()!!
            contestToEdit!!.startDate = viewModel.start.value.toString()
            contestToEdit!!.endDate = viewModel.end.value.toString()
            contestToEdit!!.registrationDeadline = viewModel.deadline.value.toString()
            contestToEdit!!.fee = BigDecimal(binding.etContestFee.text.toString())
            contestToEdit!!.maxMembers = binding.etMaxMembers.text.toString().toIntOrNull()!!
            contestToEdit!!.status = EContestStatus.PENDING
            contestToEdit!!.rules = ruleAdapter.getCurrentData()
            contestToEdit!!.rewards = rewardAdapter.getCurrentData()
            viewModel.updateContest(contestToEdit!!)
        }
    }

    private fun validateFields(): Boolean {
        val errorMessage = getMessage(error_field_required)
        binding.apply {
            val fields = listOf(
                etContestName to etContestNameErrorText,
                etContestDescription to etContestDescriptionErrorText,
                etContestDistance to etContestDistanceErrorText,
                etContestFee to etContestFeeErrorText,
                etMaxMembers to etMaxMembersErrorText
            )
            val isStartDateNull = viewModel.start.value == null
            val isEndDateNull = viewModel.end.value == null
            val isDeadlineDateNull = viewModel.deadline.value == null
            Log.e("AddContestActivity", "Start: ${viewModel.start.value}, End: ${viewModel.end.value}, Deadline: ${viewModel.deadline.value}")
            Log.e("AddContestActivity", "Start: ${isStartDateNull}, End: ${isEndDateNull}, Deadline: ${isDeadlineDateNull}")
            return fields.all { (field, errorText) -> !field.isEmpty(errorText, errorMessage) }
                    && !isValueNull(isStartDateNull, startDateErrorText, "Vui lòng chọn ngày bắt đầu")
                    && !isValueNull(isEndDateNull, endDateErrorText, "Vui lòng chọn ngày kết thúc")
                    && !isValueNull(isDeadlineDateNull, deadlineDateErrorText, "Vui lòng chọn ngày hạn đăng ký")
                    && isABeforeB(viewModel.start.value!!, viewModel.end.value!!, startDateErrorText, "Ngày bắt đầu phải nhỏ hơn ngày kết thúc")
                    && isABeforeB(viewModel.deadline.value!!, viewModel.end.value!!, deadlineDateErrorText, "Ngày hạn đăng ký phải nhỏ hơn ngày kết thúc")
        }
    }

    private fun populateFieldsForEdit(contest: Contest) {
        binding.etContestName.setText(contest.name)
        binding.etContestDescription.setText(contest.description)
        binding.etContestDistance.setText(contest.distance.toString())
        binding.etContestFee.setText(contest.fee.toString())
        binding.etMaxMembers.setText(contest.maxMembers.toString())

        binding.btnStartDate.text = DateUtils.convertToVietnameseDate(contest.startDate!!)
        binding.btnEndDate.text = DateUtils.convertToVietnameseDate(contest.endDate.toString())
        binding.btnRegistrationDeadline.text = DateUtils.convertToVietnameseDate(contest.registrationDeadline.toString())

        viewModel.selectedStartDate(DateUtils.convertStringToLocalDateTime(contest.startDate!!))
        viewModel.selectedEndDate(DateUtils.convertStringToLocalDateTime(contest.endDate!!))
        viewModel.selectedRegistrationDeadlineDate(DateUtils.convertStringToLocalDateTime(contest.registrationDeadline!!))


        ruleAdapter.updateData(contest.rules!!)
        rewardAdapter.updateData(contest.rewards!!)
    }

    private fun handleEditRule(rule: Rule) {
        val addRuleDialog = AddRuleDialog(this,
            onRuleAdded = { rule ->
                ruleAdapter.updateData(ruleAdapter.getCurrentData() + rule)
            },
            onRuleUpdated = { updatedRule ->
                Log.e("RuleUpdate", "onRuleUpdated called")
                val updatedRules = ruleAdapter.getCurrentData().map {
                    if (it.id == updatedRule.id) {
                        Log.e("RuleUpdate", "Found matching rule")
                        updatedRule
                    } else {
                        it
                    }
                }
                ruleAdapter.updateData(updatedRules)
            }
        )
        addRuleDialog.setRule(rule)
        addRuleDialog.show()
    }

    private fun handleDeleteRule(rule: Rule) {
        val updatedRules = ruleAdapter.getCurrentData().toMutableList()
        updatedRules.remove(rule)
        ruleAdapter.updateData(updatedRules)
    }

    private fun handleEditReward(reward: Reward) {
        val addRewardDialog = AddRewardDialog(this,
            onRewardAdded = { reward ->
                rewardAdapter.updateData(rewardAdapter.getCurrentData() + reward)
            },
            onRewardUpdated = { updatedReward ->
                val updatedRewards = rewardAdapter.getCurrentData().map {
                    if (it.id == updatedReward.id) updatedReward else it
                }
                rewardAdapter.updateData(updatedRewards)
            }
        )
        addRewardDialog.setReward(reward)
        addRewardDialog.show()
    }


    private fun handleDeleteReward(reward: Reward) {
        val updatedRewards = rewardAdapter.getCurrentData().toMutableList()
        updatedRewards.remove(reward)
        rewardAdapter.updateData(updatedRewards)
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
