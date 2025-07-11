package com.university.marathononline.ui.view.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.university.marathononline.R
import com.university.marathononline.base.BaseFragment
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
import com.university.marathononline.databinding.FragmentTrainingPlanBinding
import com.university.marathononline.ui.components.CreateTrainingPlanDialog
import com.university.marathononline.ui.components.TrainingFeedbackDialog
import com.university.marathononline.ui.components.TrainingProgressChartDialog
import com.university.marathononline.ui.view.activity.TrainingPlanHistoryActivity
import com.university.marathononline.ui.viewModel.TrainingPlanViewModel
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.calPace
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.formatPace
import com.university.marathononline.utils.startNewActivity
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class TrainingPlanFragment : BaseFragment<TrainingPlanViewModel, FragmentTrainingPlanBinding>() {

    private var startDate: String = ""
    private var endDate: String = ""
    private var currentDateString: String = DateUtils.getCurrentDateString()

    override fun onResume() {
        super.onResume()
        fetchFreshTrainingPlan()
    }

    private fun fetchFreshTrainingPlan() {
        currentDateString = DateUtils.getCurrentDateString()
        showLoadingState(true)
        viewModel.getCurrentTrainingPlan()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpButton()
        setUpDateNavigation()
        observeViewModel()
    }

    private fun showFeedbackDialog(trainingDay: TrainingDay) {
        if (trainingDay.trainingFeedback != null) {
            showViewFeedbackDialog(trainingDay)
        } else {
            showCreateFeedbackDialog(trainingDay)
        }
    }

    private fun showLoadingState(isLoading: Boolean) {
        if (isLoading) {
            binding.itemDetails.apply {
                loadingState.visibility = View.VISIBLE
                activePlanSection.visibility = View.GONE
                noActivePlan.root.visibility = View.GONE
            }
            binding.fabCreatePlan.hide()
        } else {
            binding.itemDetails.loadingState.visibility = View.GONE
            binding.fabCreatePlan.show()
        }
    }

    private fun setUpButton() {
        binding.fabCreatePlan.setOnClickListener {
            showCreatePlanDialog()
        }

        binding.itemDetails.noActivePlan.createNewPlanButton.setOnClickListener {
            showCreatePlanDialog()
        }

        binding.historyButton.setOnClickListener {
            startNewActivity(TrainingPlanHistoryActivity::class.java)
        }

        binding.itemDetails.planSummary.chartButton.setOnClickListener {
            val trainingDays = viewModel.currentTrainingDays.value ?: emptyList()
            if (trainingDays.isEmpty()) {
                Toast.makeText(requireContext(), "Không có dữ liệu để hiển thị biểu đồ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val dialog = TrainingProgressChartDialog(requireContext(), trainingDays)
            dialog.show()
        }
    }

    private fun calculateConsistencyScore(trainingDays: List<TrainingDay>): Double {
        val completedDays = trainingDays.count {
            it.status == ETrainingDayStatus.COMPLETED ||
                    it.status == ETrainingDayStatus.PARTIALLY_COMPLETED
        }
        val skippedDays = trainingDays.count { it.status == ETrainingDayStatus.SKIPPED }
        val missedDays = trainingDays.count { it.status == ETrainingDayStatus.MISSED }

        val totalDays = trainingDays.size
        if (totalDays == 0) return 0.0

        val completionScore = (completedDays.toDouble() / totalDays) * 5.0
        val penaltyScore = ((skippedDays + missedDays).toDouble() / totalDays) * 2.0

        return (10.0 + completionScore - penaltyScore).coerceIn(0.0, 10.0)
    }

    private fun showCreatePlanDialog() {
        val dialog = CreateTrainingPlanDialog(requireContext()) { trainingPlanRequest ->
            viewModel.createTrainingPlan(trainingPlanRequest)
        }
        dialog.show()
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

    private fun observeViewModel() {
        viewModel.getCurrentTrainingPlan.observe(viewLifecycleOwner) {
            showLoadingState(false)
            when (it) {
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

        viewModel.createTrainingPlan.observe(viewLifecycleOwner) {
            showLoadingState(false)
            when (it) {
                is Resource.Loading -> {
                    showLoadingState(true)
                }
                is Resource.Success -> {
                    viewModel.setCurrentTrainingPlan(it.value)
                    setUpCurrentTrainingPlan(it.value)
                    Toast.makeText(
                        requireContext(),
                        "Kế hoạch tập luyện đã được tạo thành công!",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.itemDetails.noActivePlan.root.visibility = View.GONE
                    binding.itemDetails.activePlanSection.visibility = View.VISIBLE
                }
                is Resource.Failure -> {
                    binding.itemDetails.noActivePlan.root.visibility = View.VISIBLE
                    binding.itemDetails.activePlanSection.visibility = View.GONE
                    handleApiError(it)
                    println("Add Training Plan" + it.fetchErrorMessage())
                }
                else -> Unit
            }
        }

        viewModel.resetTrainingDay.observe(viewLifecycleOwner) {
            showLoadingState(it == Resource.Loading)
            when (it) {
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    Toast.makeText(
                        requireContext(),
                        it.value.str,
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.getCurrentTrainingPlan()
                }
                is Resource.Failure -> {
                    handleApiError(it)
                }
                else -> Unit
            }
        }

        viewModel.submitFeedback.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Phản hồi đã được gửi thành công!",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.getCurrentTrainingPlan()
                }
                is Resource.Failure -> {
                    handleApiError(it)
                    println(it.fetchErrorMessage())
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

        this.startDate = trainingPlan.startDate
        this.endDate = trainingPlan.endDate

        updateTrainingSessionDisplay()
    }

    private fun showTrainingInputDialog(trainingPlan: TrainingPlan) {
        val message = """
            🎯 Mục tiêu quãng đường: ${trainingPlan.input.goal} km
            🏃‍♂️ Số tuần: ${trainingPlan.input.trainingWeeks}
            📈 Mức độ luyện tập: ${trainingPlan.input.level}
            
            📊 Lịch sử:
            • Quãng đường dài nhất: ${trainingPlan.input.maxDistance} km
            • Pace trung bình: ${trainingPlan.input.averagePace} phút/km
        """.trimIndent()


        AlertDialog.Builder(requireContext())
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
                    sessionPace.text =  formatPace(filteredDay.session.pace)
                    sessionDistance.text = formatDistance(filteredDay.session.distance)
                    sessionType.text = filteredDay.session.type.value

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
            displayFeedbackInSession(filteredDay)
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

    private fun displayFeedbackInSession(trainingDay: TrainingDay) {
        binding.itemDetails.itemTrainingDay.trainingSessionCard.apply {
            if (trainingDay.trainingFeedback != null) {
                feedbackSection.visibility = View.VISIBLE
                feedbackDifficulty.text = trainingDay.trainingFeedback!!.difficultyRating.value
                feedbackFeeling.text = trainingDay.trainingFeedback!!.feelingRating.value

                if (!trainingDay.trainingFeedback!!.notes.isNullOrEmpty()) {
                    feedbackNotes.visibility = View.VISIBLE
                    feedbackNotes.text = trainingDay.trainingFeedback!!.notes
                } else {
                    feedbackNotes.visibility = View.GONE
                }

                feedbackSection.setOnClickListener {
                    showViewFeedbackDialog(trainingDay)
                }

                val typedValue = TypedValue()
                requireContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
            } else {
                feedbackSection.visibility = View.GONE
            }
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
                    setupFeedbackButton(trainingDay)
                }
                ETrainingDayStatus.PARTIALLY_COMPLETED -> {
                    trainingStatus.apply {
                        setBackgroundResource(R.drawable.status_completed_bg)
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                    }
                    setupFeedbackButton(trainingDay)
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

    private fun setupFeedbackButton(trainingDay: TrainingDay) {
        binding.itemDetails.itemTrainingDay.trainingSessionCard.btnFeedback?.apply {
            if (trainingDay.trainingFeedback == null &&
                (trainingDay.status == ETrainingDayStatus.COMPLETED ||
                        trainingDay.status == ETrainingDayStatus.PARTIALLY_COMPLETED) &&
                DateUtils.isToday(trainingDay.dateTime)
            ) {
                visibility = View.VISIBLE
                setOnClickListener {
                    showCreateFeedbackDialog(trainingDay)
                }
            } else {
                visibility = View.GONE
            }
        }
    }

    private fun showCreateFeedbackDialog(trainingDay: TrainingDay) {
        val dialog = TrainingFeedbackDialog(
            context = requireContext(),
            existingFeedback = null
        ) { feedback ->
            viewModel.submitTrainingFeedback(trainingDay.id, feedback)
        }
        dialog.show()
    }

    private fun showViewFeedbackDialog(trainingDay: TrainingDay) {
        val dialog = TrainingFeedbackDialog(
            context = requireContext(),
            existingFeedback = trainingDay.trainingFeedback,
            onFeedbackSubmitted = null
        )
        dialog.show()
    }

    private fun updateSessionCardAppearance(trainingDay: TrainingDay) {
        val cardView = binding.itemDetails.itemTrainingDay.trainingSessionCard.root as androidx.cardview.widget.CardView

        when (trainingDay.status) {
            ETrainingDayStatus.COMPLETED -> {
                cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.completed_card_bg))
                cardView.alpha = 1.0f
            }
            ETrainingDayStatus.PARTIALLY_COMPLETED -> {
                cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.partial_complete_color))
                cardView.alpha = 0.9f
            }
            ETrainingDayStatus.ACTIVE -> {
                cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                cardView.alpha = 1.0f
            }
            ETrainingDayStatus.SKIPPED -> {
                cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.missed_card_bg))
                cardView.alpha = 0.8f
            }
            ETrainingDayStatus.MISSED -> {
                cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.missed_card_bg))
                cardView.alpha = 0.8f
            }
        }
    }

    private fun updateSessionProgress(trainingDay: TrainingDay) {
        if (trainingDay.record == null) {
            resetProgressUI()
            setupFeedbackButton(trainingDay)
            binding.itemDetails.itemTrainingDay.trainingSessionCard.progressDetails.root.visibility = View.GONE
            return
        }
        bindProgressData(trainingDay)
        setupFeedbackButton(trainingDay)
    }

    private fun resetProgressUI() {
        binding.itemDetails.itemTrainingDay.trainingSessionCard.progressDetails.apply {
            completedDistance.text = "0.0"
            goalDistance.text = "0.0"
            distanceProgress.progress = 0
            stepsCount.text = "0"
            timeTaken.text = "00:00:00"
            avgPace.text = "0.0 phút/km"
            heartRate.text = "0.0 bpm"
        }
    }

    private fun bindProgressData(trainingDay: TrainingDay){
        val record = trainingDay.record
        var totalDistance = record.distance
        var totalSteps = record.steps
        var totalTime = DateUtils.getDurationBetween(
            record.startTime,
            record.endTime
        ).seconds
        var avgHeartRate = record.heartRate

        val avgPaces = calPace(record.avgSpeed)

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
            root.visibility = android.view.View.VISIBLE
            completedDistance.text = kotlin.String.format("%.1f", totalDistance)
            goalDistance.text = kotlin.String.format("%.1f", goal)
            distanceProgress.progress = progressPercentage

            stepsCount.text = totalSteps.toString()
            timeTaken.text = formattedTime
            avgPace.text = kotlin.String.format("%.1f phút/km", avgPaces)
            heartRate.text = kotlin.String.format("%.1f bpm", avgHeartRate)
            timeTaken.text = com.university.marathononline.utils.DateUtils.convertLongToHHMMSS(totalTime)
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

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTrainingPlanBinding.inflate(inflater, container, false)

    override fun getFragmentRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiTrainingPlan = retrofitInstance.buildApi(TrainingPlanApiService::class.java, token)
        val apiTrainingDay = retrofitInstance.buildApi(TrainingDayApiService::class.java, token)
        val apiTrainingFeedback = retrofitInstance.buildApi(TrainingFeedbackApiService::class.java, token)
        return listOf(
            TrainingPlanRepository(apiTrainingPlan),
            TrainingDayRepository(apiTrainingDay),
            TrainingFeedbackRepository(apiTrainingFeedback)
        )
    }
}