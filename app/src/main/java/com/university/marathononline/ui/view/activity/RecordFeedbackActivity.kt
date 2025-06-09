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
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.feedback.FeedbackApiService
import com.university.marathononline.data.api.record.RecordApiService
import com.university.marathononline.data.models.*
import com.university.marathononline.data.repository.FeedbackRepository
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.databinding.ActivityRecordFeedbackBinding
import com.university.marathononline.firebase.MyFirebaseMessagingService
import com.university.marathononline.ui.adapter.FeedbackAdapter
import com.university.marathononline.ui.viewModel.FeedBackViewModel
import com.university.marathononline.utils.KEY_NOTIFICATION_DATA
import com.university.marathononline.utils.KEY_RECORD_ID
import com.university.marathononline.utils.finishAndGoBack
import com.university.marathononline.utils.visible
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class RecordFeedbackActivity :
    BaseActivity<FeedBackViewModel, ActivityRecordFeedbackBinding>() {

    private lateinit var adapter: FeedbackAdapter
    private lateinit var feedbackReceiver: BroadcastReceiver
    private lateinit var localFeedbackReceiver: BroadcastReceiver
    private var recordId: Long = -1L

    companion object {
        private const val TAG = "FeedbackActivity"
        const val KEY_RECORD = "key_record"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupIntent()
        setupAdapter()
        setupFeedbackReceivers()
        setupSendButton()
        observeViewModel()

        // Load feedbacks cho record hiện tại
        if (recordId != -1L) {
            showLoadingState(true)
            viewModel.loadFeedbacksByRecord(recordId)
        }
    }

    private fun setupIntent() {
        recordId = intent.getLongExtra(KEY_RECORD_ID, -1L)

        if (recordId == -1L) {
            Log.e(TAG, "Record ID not provided")
            finish()
            return
        }

        viewModel.getRecord(recordId)
    }

    private fun populateRecordDetails(record: Record) {
        // Basic record info
        binding.tvRecordId.text = "Record #${record.id}"
        binding.tvUserName.text = "${record.user.fullName}"
        binding.tvUserEmail.text = record.user.email

        // Record statistics
        binding.tvDistance.text = String.format("%.2f km", record.distance * 1000)
        binding.tvSteps.text = String.format("%,d", record.steps)
        binding.tvDuration.text = formatDuration(record.timeTaken)
        binding.tvAvgSpeed.text = String.format("%.1f km/h", record.avgSpeed)
        binding.tvHeartRate.text = String.format("%.0f bpm", record.heartRate)

        // Source info
        binding.tvSource.text = record.source.name
        updateSourceIcon(record.source)

        // Time info
        binding.tvStartTime.text = formatTime(record.startTime)
        binding.tvEndTime.text = formatTime(record.endTime)

        // Approval info
        populateApprovalDetails(record.approval)
    }

    private fun populateApprovalDetails(approval: RecordApproval?) {
        if (approval == null) {
            binding.cardApproval.visible(false)
            return
        }

        binding.cardApproval.visible(true)

        // Approval status chip
        updateApprovalStatusChip(approval.approvalStatus)

        // Fraud risk progress
        val riskPercentage = (approval.fraudRisk).roundToInt()
        binding.progressFraudRisk.progress = riskPercentage
        binding.tvFraudRisk.text = "$riskPercentage%"

        // Update fraud risk color based on level
        updateFraudRiskColor(approval.fraudRisk)

        // Fraud type
        if (approval.fraudType.isNotBlank()) {
            binding.layoutFraudType.visible(true)
            binding.chipFraudType.text = approval.fraudType
            updateFraudTypeChip(approval.fraudType)
        } else {
            binding.layoutFraudType.visible(false)
        }

        // Review note
        if (approval.reviewNote.isNotBlank()) {
            binding.tvReviewNote.visible(true)
            binding.tvReviewNote.text = "Ghi chú: ${approval.reviewNote}"
        } else {
            binding.tvReviewNote.visible(false)
        }
    }

    private fun updateApprovalStatusChip(status: ERecordApprovalStatus) {
        when (status) {
            ERecordApprovalStatus.PENDING -> {
                binding.chipApprovalStatus.text = "ĐANG CHỜ"
                binding.chipApprovalStatus.chipBackgroundColor =
                    ContextCompat.getColorStateList(this, R.color.light_main_color)
                binding.chipApprovalStatus.chipIcon =
                    ContextCompat.getDrawable(this, R.drawable.ic_time)
            }
            ERecordApprovalStatus.APPROVED -> {
                binding.chipApprovalStatus.text = "ĐÃ DUYỆT"
                binding.chipApprovalStatus.chipBackgroundColor =
                    ContextCompat.getColorStateList(this, R.color.main_color)
                binding.chipApprovalStatus.chipIcon =
                    ContextCompat.getDrawable(this, R.drawable.ic_completed)
            }
            ERecordApprovalStatus.REJECTED -> {
                binding.chipApprovalStatus.text = "TỪ CHỐI"
                binding.chipApprovalStatus.chipBackgroundColor =
                    ContextCompat.getColorStateList(this, R.color.red)
                binding.chipApprovalStatus.chipIcon =
                    ContextCompat.getDrawable(this, R.drawable.ic_cancel)
            }
        }
    }

    private fun updateFraudRiskColor(fraudRisk: Double) {
        val color = when {
            fraudRisk < 30 -> ContextCompat.getColor(this, R.color.main_color)
            fraudRisk < 70 -> ContextCompat.getColor(this, R.color.warning_color)
            else -> ContextCompat.getColor(this, R.color.error_color)
        }

        binding.tvFraudRisk.setTextColor(color)
        binding.progressFraudRisk.progressTintList = ContextCompat.getColorStateList(this,
            when {
                fraudRisk < 30 -> R.color.main_color
                fraudRisk < 70 -> R.color.warning_color
                else -> R.color.error_color
            }
        )
    }

    private fun updateFraudTypeChip(fraudType: String) {
        val (displayText, colorRes) = when (fraudType) {
            "SPEED_ANOMALY" -> "Tốc độ bất thường" to R.color.warning_color
            "DISTANCE_MISMATCH" -> "Khoảng cách không khớp" to R.color.warning_color
            "TIME_INCONSISTENCY" -> "Thời gian không nhất quán" to R.color.warning_color
            "LOCATION_JUMP" -> "Nhảy vị trí" to R.color.warning_color
            else -> fraudType to R.color.warning_color
        }

        binding.chipFraudType.text = displayText
        binding.chipFraudType.chipBackgroundColor =
            ContextCompat.getColorStateList(this, colorRes)
    }

    private fun updateSourceIcon(source: ERecordSource) {
        val iconRes = when (source) {
            ERecordSource.DEVICE -> R.drawable.ic_run
            ERecordSource.THIRD -> R.drawable.ic_run
            ERecordSource.MERGED -> R.drawable.ic_run
        }
        binding.ivSourceIcon.setImageResource(iconRes)
    }

    private fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    private fun formatTime(timeString: String): String {
        return try {
            val inputFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val outputFormat = DateTimeFormatter.ofPattern("HH:mm dd/MM")
            val dateTime = LocalDateTime.parse(timeString, inputFormat)
            dateTime.format(outputFormat)
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting time: ${e.message}")
            timeString.take(16) // Fallback to first 16 characters
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

        val filter = IntentFilter(MyFirebaseMessagingService.ACTION_NEW_FEEDBACK)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(localFeedbackReceiver, filter)

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
        viewModel.createFeedback(recordId, message)
    }

    private fun handleNewFeedback(intent: Intent?) {
        Log.d(TAG, "handleNewFeedback called")

        if (intent == null) {
            Log.w(TAG, "Intent is null in handleNewFeedback")
            return
        }

        // Log all extras for debugging
        val extras = intent.extras
        if (extras != null) {
            Log.d(TAG, "Intent extras:")
            for (key in extras.keySet()) {
                val value = extras.get(key)
                Log.d(TAG, "  $key: $value (${value?.javaClass?.simpleName})")
            }
        }

        intent.getSerializableExtra(KEY_NOTIFICATION_DATA)?.let { notificationData ->
            Log.d(TAG, "Raw notification data: $notificationData")
            Log.d(TAG, "Notification data class: ${notificationData.javaClass.simpleName}")

            if (notificationData is Notification) {
                val feedbackId = intent.getLongExtra("feedbackId", -1L)
                val feedbackRecordId = intent.getLongExtra("recordId", -1L)
                val feedbackType = intent.getStringExtra("feedbackType")

                Log.d(TAG, "Processing feedback notification - FeedbackID: $feedbackId, RecordID: $feedbackRecordId, Type: $feedbackType")

                if (feedbackRecordId != recordId) {
                    Log.d(TAG, "Feedback does not belong to current record (recordId: $feedbackRecordId vs $recordId)")
                    return
                }

                viewModel.getById(feedbackId)
            } else {
                Log.w(TAG, "Notification data is not of type Notification: ${notificationData.javaClass}")
            }
        } ?: run {
            Log.w(TAG, "No notification data found in intent")
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
                is Resource.Loading -> {
                    showLoadingState(true)
                }

                is Resource.Success -> {
                    Log.d(TAG, "Received ${it.value.size} feedbacks from ViewModel")
                    showLoadingState(false)

                    val processedFeedbacks = it.value.mapNotNull { feedback ->
                        if (isValidFeedback(feedback)) {
                            ensureFeedbackHasTimestamp(feedback)
                        } else {
                            Log.w(TAG, "Filtering out invalid feedback: ${feedback.id}")
                            null
                        }
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

        viewModel.createFeedbackResult.observe(this) { it ->
            when (it) {
                is Resource.Success -> {
                    Log.d(TAG, "Feedback created successfully")
                    binding.btnSend.isEnabled = true
                    binding.progressBar.visible(false)
                    binding.etMessage.text!!.clear()

                    viewModel.loadFeedbacksByRecord(recordId)
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
                    viewModel.loadFeedbacksByRecord(recordId)
                    Toast.makeText(this, "Đã xóa feedback", Toast.LENGTH_SHORT).show()
                }
                is Resource.Failure -> handleApiError(it)
                else -> Unit
            }
        }

        viewModel.getRecord.observe(this) {
            when (it) {
                is Resource.Success -> {
                    populateRecordDetails(it.value)
                }

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
                            "ADMIN_FEEDBACK" -> {
                                Toast.makeText(this, "Admin đã phản hồi", Toast.LENGTH_SHORT).show()
                            }
                            "RUNNER_FEEDBACK" -> {
                                Toast.makeText(this, "Có phản hồi mới", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.d(TAG, "Feedback already exists in list, skipping")
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

    private fun showErrorState(errorMessage: String) {
        binding.tvEmpty.text = "Lỗi: $errorMessage"
        updateEmptyState(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(feedbackReceiver)
            LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(localFeedbackReceiver)
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
        return listOf(FeedbackRepository(apiFeedback), RecordRepository(apiRecord))
    }
}