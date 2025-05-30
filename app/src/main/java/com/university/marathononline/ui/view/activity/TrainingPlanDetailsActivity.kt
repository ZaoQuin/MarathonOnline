package com.university.marathononline.ui.view.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.university.marathononline.R
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.trainingPlan.TrainingPlanApiService
import com.university.marathononline.data.models.ETrainingSessionType
import com.university.marathononline.data.models.TrainingDay
import com.university.marathononline.data.models.TrainingPlan
import com.university.marathononline.data.repository.TrainingPlanRepository
import com.university.marathononline.databinding.ActivityTrainingPlanDetailsBinding
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

        val isPrevEnabled = !currentDate.isEqual(planStart) && !currentDate.isBefore(planStart)
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

        // Next button
        val isNextEnabled = !currentDate.isEqual(planEnd) && !currentDate.isAfter(planEnd)
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
            🏃‍♂️ Số buổi mỗi tuần: ${trainingPlan.input.daysPerWeek}
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
                    sessionType.text = filteredDay.session.type.toString()
                    sessionNotes.text = filteredDay.session.notes
                    sessionPace.text = filteredDay.session.pace.toString()
                    sessionDistance.text = filteredDay.session.distance.toString()
                    trainingStatus.text = filteredDay.status.toString()

                    if (filteredDay.session.type == ETrainingSessionType.REST) {
                        sessionDetails.visibility = View.GONE
                        trainingSessionCard.progressDetails.root.visibility = View.GONE
                    } else {
                        sessionDetails.visibility = View.VISIBLE
                        trainingSessionCard.progressDetails.root.visibility = View.VISIBLE
                        updateSessionProgress(filteredDay)
                    }
                }
            }
        } else {
            println("Không tìm thấy bài tập phù hợp hoặc ngoài khoảng thời gian của kế hoạch")
            binding.itemDetails.itemTrainingDay.apply {
                trainingSessionCard.root.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
            }
        }
    }

    private fun updateSessionProgress(trainingDay: TrainingDay) {
        // Calculate total values from all records
        var totalDistance = 0.0
        var totalSteps = 0
        var totalTime = 0L
        var avgHeartRate = 0.0

        trainingDay.records.forEach { record ->
            totalDistance += record.distance
            totalSteps += record.steps
            totalTime += record.timeTaken
            avgHeartRate += record.heartRate
        }

        // Calculate averages
        if (trainingDay.records.isNotEmpty()) {
            avgHeartRate /= trainingDay.records.size
        }

        // Calculate average pace (minutes per km)
        val avgPaces = if (totalDistance > 0) {
            val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(totalTime)
            totalMinutes.toDouble() / totalDistance
        } else {
            0.0
        }

        // Format time (hh:mm:ss)
        val hours = TimeUnit.MILLISECONDS.toHours(totalTime)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime) % 60
        val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        // Set the goal distance from the session
        val goal = trainingDay.session.distance

        // Calculate progress percentage
        val progressPercentage = if (goal > 0) {
            ((totalDistance / goal) * 100).toInt().coerceAtMost(100)
        } else {
            0
        }

        // Update UI
        binding.itemDetails.itemTrainingDay.trainingSessionCard.progressDetails.apply{
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
            // Check if current date is within plan range
            val currentDate = DateUtils.parseStringToLocalDate(currentDateString) ?: return null

            // Check date is within plan range
            if (!DateUtils.isDateInRange(currentDateString, startDate, endDate)) {
                return null
            }

            // Find training day with matching date
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
        val api = retrofitInstance.buildApi(TrainingPlanApiService::class.java, token)
        return listOf(TrainingPlanRepository(api))
    }
}