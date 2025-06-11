package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.training.TrainingDayApiService
import com.university.marathononline.data.api.training.TrainingFeedbackApiService
import com.university.marathononline.data.api.training.TrainingPlanApiService
import com.university.marathononline.data.models.ETrainingDayStatus
import com.university.marathononline.data.models.ETrainingSessionType
import com.university.marathononline.data.models.TrainingDay
import com.university.marathononline.data.models.TrainingPlan
import com.university.marathononline.data.repository.TrainingDayRepository
import com.university.marathononline.data.repository.TrainingFeedbackRepository
import com.university.marathononline.data.repository.TrainingPlanRepository
import com.university.marathononline.databinding.ActivityTrainingPlanDetailsBinding
import com.university.marathononline.ui.components.TrainingProgressChartDialog
import com.university.marathononline.ui.viewModel.TrainingPlanViewModel
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_TRAINING_PLAN_ID
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class TrainingPlanDetailsActivity : BaseActivity<TrainingPlanViewModel, ActivityTrainingPlanDetailsBinding>()  {

    private var startDate: String = ""
    private var endDate: String = ""
    private var currentDateString: String = DateUtils.getCurrentDateString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras(intent)
        showLoadingState(true)
        setUpButton()
        setUpDateNavigation()
        observeViewModel()
    }

    private fun handleIntentExtras(intent: Intent) {
        val planId = intent.getLongExtra(KEY_TRAINING_PLAN_ID, -1L)
        if (planId != -1L) {
            viewModel.getById(planId)
        }
    }

    private fun showLoadingState(isLoading: Boolean) {
        if (isLoading) {
            binding.itemDetails.apply {
                loadingState.visibility = View.VISIBLE
                activePlanSection.visibility = View.GONE
                noActivePlan.root.visibility = View.GONE
            }
        } else {
            // Hide loading skeleton, show appropriate content
            binding.itemDetails.loadingState.visibility = View.GONE
        }
    }

    private fun setUpButton(){
        binding.itemDetails.noActivePlan.createNewPlanButton.visibility = View.GONE

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.itemDetails.planSummary.chartButton.setOnClickListener {
            val trainingDays = viewModel.currentTrainingDays.value ?: emptyList()
            if (trainingDays.isEmpty()) {
                Toast.makeText(this, "Không có dữ liệu để hiển thị biểu đồ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val dialog = TrainingProgressChartDialog(this, trainingDays)
            dialog.show()
        }
    }

    private fun setUpDateNavigation() {
        binding.itemDetails.itemTrainingDay.apply {
            nextDay.setOnClickListener {
                navigateToNextDay()
                updateNavigationButtons()
            }
            previousDay.setOnClickListener {
                navigateToPreviousDay()
                updateNavigationButtons()
            }
        }
    }

    private fun navigateToNextDay() {
        try {
            val nextDateString = DateUtils.addDaysToDate(currentDateString, 1)
            val nextLocalDate = DateUtils.parseStringToLocalDate(nextDateString) ?: return
            val planEnd = DateUtils.parseLocalDateTimeStr(endDate)

            if (nextLocalDate.isAfter(planEnd)) return

            currentDateString = nextDateString
            updateTrainingSessionDisplay()
        } catch (_: Exception) {}
    }

    private fun navigateToPreviousDay() {
        try {
            val prevDateString = DateUtils.addDaysToDate(currentDateString, -1)
            val prevLocalDate = DateUtils.parseStringToLocalDate(prevDateString) ?: return
            val planStart = DateUtils.parseLocalDateTimeStr(startDate)

            if (prevLocalDate.isBefore(planStart)) return

            currentDateString = prevDateString
            updateTrainingSessionDisplay()
        } catch (_: Exception) {}
    }

    private fun updateNavigationButtons() {
        val currentDate = DateUtils.parseStringToLocalDate(currentDateString) ?: return
        val planStart = DateUtils.parseLocalDateTimeStr(startDate)
        val planEnd = DateUtils.parseLocalDateTimeStr(endDate)
        val trainingDays = viewModel.currentTrainingDays.value ?: emptyList()

        if (trainingDays.size == 1) {
            binding.itemDetails.itemTrainingDay.previousDay.apply {
                isEnabled = false
                backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.disabled_gray)
                )
                imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.white)
                )
                alpha = 0.5f
            }
            binding.itemDetails.itemTrainingDay.nextDay.apply {
                isEnabled = false
                backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.disabled_gray)
                )
                imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.white)
                )
                alpha = 0.5f
            }
            return
        }

        val trainingDayDates = trainingDays.mapNotNull { day ->
            DateUtils.convertStringToLocalDateTime(day.dateTime)?.toLocalDate()
        }.sorted()

        val earliestDate = trainingDayDates.firstOrNull()
        val latestDate = trainingDayDates.lastOrNull()

        val isPrevEnabled = earliestDate != null && currentDate.isAfter(earliestDate) && !currentDate.isBefore(planStart)
        binding.itemDetails.itemTrainingDay.previousDay.apply {
            isEnabled = isPrevEnabled
            backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, if (isPrevEnabled) R.color.main_color else R.color.disabled_gray)
            )
            imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, if (isPrevEnabled) R.color.white else R.color.white)
            )
            alpha = if (isPrevEnabled) 1f else 0.5f
        }

        val isNextEnabled = latestDate != null && currentDate.isBefore(latestDate) && !currentDate.isAfter(planEnd)
        binding.itemDetails.itemTrainingDay.nextDay.apply {
            isEnabled = isNextEnabled
            backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, if (isNextEnabled) R.color.main_color else R.color.disabled_gray)
            )
            imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, if (isNextEnabled) R.color.white else R.color.white)
            )
            alpha = if (isNextEnabled) 1f else 0.5f
        }
    }

    private fun observeViewModel(){
        viewModel.getById.observe(this) {
            showLoadingState(false)
            when(it){
                is Resource.Loading -> {
                    showLoadingState(true)
                }
                is Resource.Success -> {
                    viewModel.setCurrentTrainingPlan(it.value)
                    setUpCurrentTrainingPlan(it.value)
                    binding.itemDetails.activePlanSection.visibility = View.VISIBLE
                }
                is Resource.Failure -> {
                    binding.itemDetails.noActivePlan.root.visibility = View.VISIBLE
                    binding.itemDetails.activePlanSection.visibility = View.GONE
                    handleApiError(it)
                }
                else -> Unit
            }
        }
    }

    private fun setUpCurrentTrainingPlan(trainingPlan: TrainingPlan) {
        binding.itemDetails.planSummary.apply {
            planName.text = trainingPlan.name
            planStatus.text = trainingPlan.status.toString()
            planStartDate.text = DateUtils.parseLocalDateTimeStr(trainingPlan.startDate).toString()
            planEndDate.text = DateUtils.parseLocalDateTimeStr(trainingPlan.endDate).toString()
            completedSessions.text = trainingPlan.completedDays.toString()
            totalDistance.text = trainingPlan.totalDistance.toString()
            remainingSessions.text = trainingPlan.remainingDays.toString()
            planProgress.progress = trainingPlan.progress.toInt()
            progressPercentage.text = "${trainingPlan.progress.toInt()}%"
            infoButton.setOnClickListener {
                showTrainingInputDialog(trainingPlan)
            }
        }

        // Save training plan data for filtering
        this.startDate = trainingPlan.startDate
        this.endDate = trainingPlan.endDate

        // Update training day display for current date
        updateTrainingSessionDisplay()
    }

    private fun showTrainingInputDialog(trainingPlan: TrainingPlan){
        val message = """
            🎯 Mục tiêu quãng đường: ${trainingPlan.input.goal} km
            🏃‍♂️ Số tuần: ${trainingPlan.input.trainingWeeks}
            📈 Mức độ luyện tập: ${trainingPlan.input.level}
            
            📊 Lịch sử:
            • Quãng đường dài nhất: ${trainingPlan.input.maxDistance} km
            • Pace trung bình: ${trainingPlan.input.averagePace} phút/km
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Thông tin kế hoạch")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun updateTrainingSessionDisplay() {
        val filteredDay = filterTrainingDayByDate()

        if (filteredDay != null) {
            println("Đã tìm thấy ngày luyện tập: ${filteredDay.dateTime}, loại: ${filteredDay.session.type}")

            binding.itemDetails.itemTrainingDay.dayOfSession.text = DateUtils.formatTrainingDayString(filteredDay)

            println("Hiển thị bài tập: ${filteredDay.session.name}")
            binding.itemDetails.itemTrainingDay.apply {
                emptyState.visibility = View.GONE
                trainingSessionCard.apply {
                    root.visibility = View.VISIBLE
                    sessionName.text = filteredDay.session.name
                    sessionNotes.text = filteredDay.session.notes
                    sessionPace.text = filteredDay.session.pace.toString()
                    sessionDistance.text = filteredDay.session.distance.toString()

                    if (filteredDay.session.type == ETrainingSessionType.REST) {
                        sessionDetails.visibility = View.GONE
                        trainingSessionCard.progressDetails.root.visibility = View.GONE
                    } else {
                        updateSessionProgress(filteredDay)
                        sessionDetails.visibility = View.VISIBLE
                        trainingSessionCard.progressDetails.root.visibility = View.VISIBLE
                    }
                }
            }

            updateTrainingStatusDisplay(filteredDay)
            updateNavigationButtons()
        } else {
            println("Không có bài tập được lên lịch cho ngày này")
            binding.itemDetails.itemTrainingDay.apply {
                trainingSessionCard.root.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
            }
            updateNavigationButtons()
        }
    }

    private fun updateTrainingStatusDisplay(trainingDay: TrainingDay) {
        binding.itemDetails.itemTrainingDay.trainingSessionCard.apply {
            val statusText = when (trainingDay.status) {
                ETrainingDayStatus.COMPLETED -> "${trainingDay.status} (${String.format("%.0f%%", trainingDay.completionPercentage)})"
                ETrainingDayStatus.PARTIALLY_COMPLETED -> "${trainingDay.status} (${String.format("%.0f%%", trainingDay.completionPercentage)})"
                else -> trainingDay.status.toString()
            }
            trainingStatus.text = statusText

            when (trainingDay.status) {
                ETrainingDayStatus.COMPLETED -> {
                    trainingStatus.apply {
                        setBackgroundResource(R.drawable.status_completed_bg)
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                    }
                }
                ETrainingDayStatus.PARTIALLY_COMPLETED -> {
                    trainingStatus.apply {
                        setBackgroundResource(R.drawable.status_completed_bg)
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                    }
                }
                ETrainingDayStatus.ACTIVE -> {
                    trainingStatus.apply {
                        setBackgroundResource(R.drawable.status_active_bg)
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                    }
                }
                ETrainingDayStatus.SKIPPED -> {
                    trainingStatus.apply {
                        setBackgroundResource(R.drawable.status_missed_bg)
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                    }
                }
                ETrainingDayStatus.MISSED -> {
                    trainingStatus.apply {
                        setBackgroundResource(R.drawable.status_missed_bg)
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                    }
                }
            }

            updateSessionCardAppearance(trainingDay)
        }
    }

    private fun updateSessionCardAppearance(trainingDay: TrainingDay) {
        val cardView = binding.itemDetails.itemTrainingDay.trainingSessionCard.root as androidx.cardview.widget.CardView

        when (trainingDay.status) {
            ETrainingDayStatus.COMPLETED -> {
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.completed_card_bg))
                cardView.alpha = 1.0f
            }
            ETrainingDayStatus.PARTIALLY_COMPLETED -> {
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.partial_complete_color))
                cardView.alpha = 0.9f
            }
            ETrainingDayStatus.ACTIVE -> {
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
                cardView.alpha = 1.0f
            }
            ETrainingDayStatus.SKIPPED -> {
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.missed_card_bg))
                cardView.alpha = 0.8f
            }
            ETrainingDayStatus.MISSED -> {
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.missed_card_bg))
                cardView.alpha = 0.8f
            }
        }
    }

    private fun updateSessionProgress(trainingDay: TrainingDay) {
        if (trainingDay.record == null) {
            binding.itemDetails.itemTrainingDay.trainingSessionCard.progressDetails.root.visibility = View.GONE
            return
        }

        val record = trainingDay.record
        var totalDistance = record.distance
        var totalSteps = record.steps
        var totalTime = DateUtils.getDurationBetween(
            record.startTime,
            record.endTime
        ).seconds
        var avgHeartRate = record.heartRate

        val avgPaces = if (totalDistance > 0) {
            val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(totalTime)
            totalMinutes.toDouble() / totalDistance
        } else {
            0.0
        }

        val hours = TimeUnit.MILLISECONDS.toHours(totalTime)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime) % 60
        val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        val goal = trainingDay.session.distance

        val progressPercentage = if (goal > 0) {
            ((totalDistance / goal) * 100).toInt().coerceAtMost(100)
        } else {
            0
        }

        binding.itemDetails.itemTrainingDay.trainingSessionCard.progressDetails.apply {
            root.visibility = View.VISIBLE
            completedDistance.text = String.format("%.1f", totalDistance)
            goalDistance.text = String.format("%.1f", goal)
            distanceProgress.progress = progressPercentage

            stepsCount.text = totalSteps.toString()
            timeTaken.text = formattedTime
            avgPace.text = String.format("%.1f phút/km", avgPaces)
            heartRate.text = String.format("%.1f bpm", avgHeartRate)
        }
    }

    private fun filterTrainingDayByDate(): TrainingDay? {
        try {
            val currentDate = DateUtils.parseStringToLocalDate(currentDateString) ?: return null

            if (!DateUtils.isDateInRange(currentDateString, startDate, endDate)) {
                return null
            }

            return viewModel.currentTrainingDays.value?.find { day ->
                val sessionDate = DateUtils.convertStringToLocalDateTime(day.dateTime).toLocalDate()
                sessionDate == currentDate
            }
        } catch (e: Exception) {
            println("Lỗi khi lọc ngày tập: ${e.message}")
            return null
        }
    }

    override fun getViewModel() = TrainingPlanViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityTrainingPlanDetailsBinding.inflate(inflater)

    override fun getActivityRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiTrainingPlan = retrofitInstance.buildApi(TrainingPlanApiService::class.java, token)
        val apiTrainingDay = retrofitInstance.buildApi(TrainingDayApiService::class.java, token)
        val apiTrainingFeedback = retrofitInstance.buildApi(TrainingFeedbackApiService::class.java, token)
        return listOf(TrainingPlanRepository(apiTrainingPlan), TrainingDayRepository(apiTrainingDay),
            TrainingFeedbackRepository(apiTrainingFeedback)
        )
    }
}