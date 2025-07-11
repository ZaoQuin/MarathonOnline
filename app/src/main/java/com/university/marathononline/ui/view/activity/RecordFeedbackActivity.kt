package com.university.marathononline.ui.view.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.feedback.FeedbackApiService
import com.university.marathononline.data.api.record.RecordApiService
import com.university.marathononline.data.api.registration.RegistrationApiService
import com.university.marathononline.data.models.*
import com.university.marathononline.data.repository.FeedbackRepository
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.data.repository.RegistrationRepository
import com.university.marathononline.databinding.ActivityRecordFeedbackBinding
import com.university.marathononline.service.firebase.MyFirebaseMessagingService
import com.university.marathononline.ui.adapter.FeedbackAdapter
import com.university.marathononline.ui.viewModel.FeedBackViewModel
import com.university.marathononline.utils.ACTION_NEW_FEEDBACK
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_FEEDBACK_ID
import com.university.marathononline.utils.KEY_FEEDBACK_TYPE
import com.university.marathononline.utils.KEY_NOTIFICATION_DATA
import com.university.marathononline.utils.KEY_RECORD_ID
import com.university.marathononline.utils.KEY_REGISTRATION_ID
import com.university.marathononline.utils.finishAndGoBack
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.formatSpeed
import com.university.marathononline.utils.visible
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import kotlin.math.roundToInt

class RecordFeedbackActivity : BaseActivity<FeedBackViewModel, ActivityRecordFeedbackBinding>() {

    private lateinit var adapter: FeedbackAdapter
    private lateinit var feedbackReceiver: BroadcastReceiver
    private lateinit var localFeedbackReceiver: BroadcastReceiver
    private var recordId: Long = -1L
    private var registrationId: Long = -1L
    private var mode: FeedbackMode = FeedbackMode.RECORD

    enum class FeedbackMode {
        RECORD, REGISTRATION
    }

    companion object {
        private const val TAG = "FeedbackActivity"
        const val KEY_RECORD = "key_record"
        const val KEY_REGISTRATION = "key_registration"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupIntent()
        setupAdapter()
        setupFeedbackReceivers()
        setupSendButton()
        observeViewModel()

        when (mode) {
            FeedbackMode.RECORD -> {
                if (recordId != -1L) {
                    showLoadingState(true)
                    viewModel.loadFeedbacksByRecord(recordId)
                    viewModel.getRecord(recordId)
                }
            }
            FeedbackMode.REGISTRATION -> {
                if (registrationId != -1L) {
                    showLoadingState(true)
                    viewModel.loadFeedbacksByRegistration(registrationId)
                    viewModel.getRegistration(registrationId)
                }
            }
        }
    }

    private fun setupIntent() {
        recordId = intent.getLongExtra(KEY_RECORD_ID, -1L)
        registrationId = intent.getLongExtra(KEY_REGISTRATION_ID, -1L)

        mode = when {
            recordId != -1L -> FeedbackMode.RECORD
            registrationId != -1L -> FeedbackMode.REGISTRATION
            else -> {
                Log.e(TAG, "Neither Record ID nor Registration ID provided")
                finish()
                return
            }
        }

        when (mode) {
            FeedbackMode.RECORD -> {
                binding.recordDetailsLayout.visible(true)
                binding.cardApproval.visible(true)
                binding.registrationDetailsLayout.visible(false)
            }
            FeedbackMode.REGISTRATION -> {
                binding.recordDetailsLayout.visible(false)
                binding.cardApproval.visible(false)
                binding.registrationDetailsLayout.visible(true)
            }
        }
    }

    private fun populateRecordDetails(record: Record) {
        binding.tvRecordUserName.text = "${record.user.fullName}"
        binding.tvRecordUserEmail.text = record.user.email
        binding.tvDistance.text = formatDistance(record.distance)
        binding.tvSteps.text = String.format("%,d", record.steps)
        binding.tvDuration.text =String.format("%,d giây", record.timeTaken)
        binding.tvAvgSpeed.text = formatSpeed(record.avgSpeed)
        binding.tvHeartRate.text = String.format("%.0f bpm", record.heartRate)
        binding.tvSource.text = record.source.name
        binding.tvStartTime.text = DateUtils.formatLocalDateTimeStrToDateTimeString(record.startTime)
        binding.tvEndTime.text = DateUtils.formatLocalDateTimeStrToDateTimeString(record.endTime)
        populateApprovalDetails(record.approval)
    }

    private fun populateRegistrationDetails(registration: Registration) {
        binding.tvRegistrationUserName.text = registration.runner?.fullName ?: "Không xác định"
        binding.tvRegistrationUserEmail.text = registration.runner?.email ?: "N/A"
        binding.tvRegistrationRank.text = registration.registrationRank.toString()
        binding.tvRegistrationDate.text = DateUtils.formatDateString(registration.registrationDate)
        binding.tvCompletedDate.text = registration.completedDate?.let { DateUtils.formatDateString(it) } ?: "N/A"
        binding.chipRegistrationStatus.text = registration.status.value
        updateRegistrationStatusChip(registration.status)
    }

    private fun updateRegistrationStatusChip(status: ERegistrationStatus) {
        binding.chipRegistrationStatus.apply {
            when (status) {
                ERegistrationStatus.PENDING -> {
                    setChipIconResource(R.drawable.ic_time)
                    chipBackgroundColor = getColorStateList(R.color.light_main_color)
                }
                ERegistrationStatus.ACTIVE -> {
                    setChipIconResource(R.drawable.ic_runner)
                    chipBackgroundColor = getColorStateList(R.color.light_main_color)
                }
                ERegistrationStatus.COMPLETED -> {
                    setChipIconResource(R.drawable.ic_completed)
                    chipBackgroundColor = getColorStateList(R.color.main_color)
                }
                ERegistrationStatus.BLOCK -> {
                    setChipIconResource(R.drawable.ic_cancel)
                    chipBackgroundColor = getColorStateList(R.color.red)
                }
            }
        }
    }

    private fun populateApprovalDetails(approval: RecordApproval?) {
        if (approval == null) {
            binding.cardApproval.visible(false)
            return
        }
        binding.cardApproval.visible(true)
        val riskPercentage = (approval.fraudRisk).roundToInt()
        binding.progressFraudRisk.progress = riskPercentage
        binding.tvFraudRisk.text = "$riskPercentage%"
        if (approval.fraudType.isNotBlank()) {
            binding.layoutFraudType.visible(true)
            binding.chipFraudType.text = approval.fraudType
        } else {
            binding.layoutFraudType.visible(false)
        }
        if (approval.reviewNote.isNotBlank()) {
            binding.tvReviewNote.visible(true)
            binding.tvReviewNote.text = "Ghi chú: ${approval.reviewNote}"
        } else {
            binding.tvReviewNote.visible(false)
        }
    }

    private fun setupAdapter() {
        binding.rvFeedbacks.apply {
            layoutManager = LinearLayoutManager(this@RecordFeedbackActivity).apply {
                reverseLayout = false
                stackFromEnd = true
            }
            itemAnimator = DefaultItemAnimator().apply {
                addDuration = 300
                removeDuration = 300
                moveDuration = 300
                changeDuration = 300
            }
        }
        adapter = FeedbackAdapter(emptyList()) { feedback ->
            showFeedbackOptions(feedback)
        }
        binding.rvFeedbacks.adapter = adapter
    }

    private fun setupFeedbackReceivers() {
        feedbackReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                handleNewFeedback(intent)
            }
        }
        localFeedbackReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                handleNewFeedback(intent)
            }
        }
        val filter = IntentFilter(ACTION_NEW_FEEDBACK)
        LocalBroadcastManager.getInstance(this).registerReceiver(localFeedbackReceiver, filter)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(feedbackReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(feedbackReceiver, filter)
        }
    }

    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendFeedback(message)
            } else {
                Toast.makeText(this, "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnBack.setOnClickListener {
            finishAndGoBack()
        }
    }

    private fun sendFeedback(message: String) {
        binding.btnSend.isEnabled = false
        binding.progressBar.visible(true)
        when (mode) {
            FeedbackMode.RECORD -> viewModel.createFeedback(recordId, message)
            FeedbackMode.REGISTRATION -> viewModel.createRegistrationFeedback(registrationId, message)
        }
    }

    private fun handleNewFeedback(intent: Intent?) {
        intent!!.getSerializableExtra(KEY_NOTIFICATION_DATA)?.let { notificationData ->
            if (notificationData is Notification) {
                val feedbackId = intent.getLongExtra(KEY_FEEDBACK_ID, -1L)
                val feedbackRecordId = intent.getLongExtra(KEY_RECORD_ID, -1L)
                val feedbackRegistrationId = intent.getLongExtra(KEY_REGISTRATION_ID, -1L)
                val feedbackType = intent.getStringExtra(KEY_FEEDBACK_TYPE)
                Log.d(TAG, "Processing feedback notification - FeedbackID: $feedbackId, RecordID: $feedbackRecordId, RegistrationID: $feedbackRegistrationId, Type: $feedbackType")
                when (mode) {
                    FeedbackMode.RECORD -> {
                        if (feedbackRecordId != recordId) {
                            Log.d(TAG, "Feedback does not belong to current record")
                            return
                        }
                    }
                    FeedbackMode.REGISTRATION -> {
                        if (feedbackRegistrationId != registrationId) {
                            Log.d(TAG, "Feedback does not belong to current registration")
                            return
                        }
                    }
                }
                viewModel.getById(feedbackId)
            }
        }
    }

    private fun isValidFeedback(feedback: Feedback): Boolean {
        val isValid = feedback.id > 0 &&
                !feedback.message.isNullOrBlank() &&
                feedback.sender != null &&
                feedback.sender.id > 0
        if (!isValid) {
            Log.w(TAG, "Invalid feedback - ID: ${feedback.id}, Message: '${feedback.message}', Sender: ${feedback.sender}")
        }
        return isValid
    }

    private fun ensureFeedbackHasTimestamp(feedback: Feedback): Feedback {
        return if (feedback.sentAt.isNullOrBlank()) {
            Log.w(TAG, "Feedback ${feedback.id} has null/empty sentAt, setting current timestamp")
            feedback.copy(sentAt = LocalDateTime.now().toString())
        } else {
            feedback
        }
    }

    private fun showFeedbackOptions(feedback: Feedback) {
        val currentEmail = runBlocking {
            try {
                userPreferences.email.first()
            } catch (e: Exception) {
                ""
            }
        }
        if (feedback.sender.email == currentEmail) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Tùy chọn")
                .setItems(arrayOf("Xóa feedback")) { _, which ->
                    when (which) {
                        0 -> deleteFeedback(feedback)
                    }
                }
                .show()
        }
    }

    private fun deleteFeedback(feedback: Feedback) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xóa feedback")
            .setMessage("Bạn có chắc chắn muốn xóa feedback này?")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteFeedback(feedback.id)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.feedbacks.observe(this) {
            when (it) {
                is Resource.Loading -> showLoadingState(true)
                is Resource.Success -> {
                    Log.d(TAG, "Received ${it.value.size} feedbacks from ViewModel")
                    showLoadingState(false)
                    val processedFeedbacks = it.value.mapNotNull { feedback ->
                        if (isValidFeedback(feedback)) ensureFeedbackHasTimestamp(feedback) else null
                    }
                    Log.d(TAG, "Processed ${processedFeedbacks.size} valid feedbacks")
                    adapter.updateData(processedFeedbacks)
                    updateFeedbackCount(processedFeedbacks.size)
                    updateEmptyState(processedFeedbacks.isEmpty())
                    if (processedFeedbacks.isNotEmpty()) {
                        binding.rvFeedbacks.scrollToPosition(processedFeedbacks.size - 1)
                    }
                }
                is Resource.Failure -> handleApiError(it)
            }
        }
        viewModel.createFeedbackResult.observe(this) {
            when (it) {
                is Resource.Success -> {
                    Log.d(TAG, "Feedback created successfully")
                    binding.btnSend.isEnabled = true
                    binding.progressBar.visible(false)
                    binding.etMessage.text!!.clear()
                    when (mode) {
                        FeedbackMode.RECORD -> viewModel.loadFeedbacksByRecord(recordId)
                        FeedbackMode.REGISTRATION -> viewModel.loadFeedbacksByRegistration(registrationId)
                    }
                    Toast.makeText(this, "Đã gửi feedback", Toast.LENGTH_SHORT).show()
                }
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }
        viewModel.deleteFeedbackResult.observe(this) {
            when (it) {
                is Resource.Success -> {
                    Log.d(TAG, "Feedback deleted successfully")
                    when (mode) {
                        FeedbackMode.RECORD -> viewModel.loadFeedbacksByRecord(recordId)
                        FeedbackMode.REGISTRATION -> viewModel.loadFeedbacksByRegistration(registrationId)
                    }
                    Toast.makeText(this, "Đã xóa feedback", Toast.LENGTH_SHORT).show()
                }
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }
        viewModel.getRecord.observe(this) {
            when (it) {
                is Resource.Success -> populateRecordDetails(it.value)
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }
        viewModel.getRegistration.observe(this) {
            when (it) {
                is Resource.Success -> populateRegistrationDetails(it.value)
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }
        viewModel.getById.observe(this) {
            when (it) {
                is Resource.Success -> {
                    Log.d(TAG, "Received single feedback: ${it.value.id}")
                    val feedback = it.value
                    if (!isValidFeedback(feedback)) {
                        Log.w(TAG, "Invalid feedback received: ${feedback.id}")
                        return@observe
                    }
                    val processedFeedback = ensureFeedbackHasTimestamp(feedback)
                    val currentFeedbacks = adapter.getCurrentData().toMutableList()
                    val existingIndex = currentFeedbacks.indexOfFirst { it.id == processedFeedback.id }
                    if (existingIndex == -1) {
                        Log.d(TAG, "Adding new feedback to list")
                        currentFeedbacks.add(processedFeedback)
                        adapter.updateData(currentFeedbacks)
                        updateFeedbackCount(currentFeedbacks.size)
                        updateEmptyState(currentFeedbacks.isEmpty())
                        binding.rvFeedbacks.smoothScrollToPosition(currentFeedbacks.size - 1)
                        try {
                            val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom)
                            binding.rvFeedbacks.findViewHolderForAdapterPosition(currentFeedbacks.size - 1)
                                ?.itemView?.startAnimation(slideIn)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error applying animation: ${e.message}")
                        }
                        val feedbackType = intent?.getStringExtra("feedbackType")
                        when (feedbackType) {
                            "ADMIN_FEEDBACK" -> Toast.makeText(this, "Admin đã phản hồi", Toast.LENGTH_SHORT).show()
                            "RUNNER_FEEDBACK" -> Toast.makeText(this, "Có phản hồi mới", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }
    }

    private fun showLoadingState(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingLayout.visible(true)
            binding.rvFeedbacks.visible(false)
            binding.emptyLayout.visible(false)
        } else {
            binding.loadingLayout.visible(false)
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyLayout.visible(true)
            binding.rvFeedbacks.visible(false)
        } else {
            binding.emptyLayout.visible(false)
            binding.rvFeedbacks.visible(true)
        }
    }

    private fun updateFeedbackCount(count: Int) {
        binding.tvFeedbackCount.text = when (count) {
            0 -> "Chưa có phản hồi"
            1 -> "1 phản hồi"
            else -> "$count phản hồi"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(feedbackReceiver)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(localFeedbackReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receivers: ${e.message}")
        }
    }

    override fun getViewModel() = FeedBackViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityRecordFeedbackBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiFeedback = retrofitInstance.buildApi(FeedbackApiService::class.java, token)
        val apiRecord = retrofitInstance.buildApi(RecordApiService::class.java, token)
        val apiRegistration = retrofitInstance.buildApi(RegistrationApiService::class.java, token)
        return listOf(
            FeedbackRepository(apiFeedback),
            RecordRepository(apiRecord),
            RegistrationRepository(apiRegistration)
        )
    }
}