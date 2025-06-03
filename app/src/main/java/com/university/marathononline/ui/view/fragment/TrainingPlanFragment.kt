package com.university.marathononline.ui.view.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
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
import com.university.marathononline.ui.view.activity.TrainingPlanHistoryActivity
import com.university.marathononline.ui.viewModel.TrainingPlanViewModel
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.startNewActivity
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class TrainingPlanFragment: BaseFragment<TrainingPlanViewModel, FragmentTrainingPlanBinding>() {

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

    private fun setUpButton(){
        binding.fabCreatePlan.setOnClickListener {
            showCreatePlanDialog()
        }

        binding.itemDetails.noActivePlan.createNewPlanButton.setOnClickListener {
            showCreatePlanDialog()
        }

        binding.historyButton.setOnClickListener{
            startNewActivity(TrainingPlanHistoryActivity::class.java)
        }
    }

    private fun showCreatePlanDialog(){
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

        // If there is only one training day, disable both navigation buttons
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

        // If there are multiple training days, find the earliest and latest training day dates
        val trainingDayDates = trainingDays.mapNotNull { day ->
            DateUtils.convertStringToLocalDateTime(day.dateTime)?.toLocalDate()
        }.sorted()

        val earliestDate = trainingDayDates.firstOrNull()
        val latestDate = trainingDayDates.lastOrNull()

        // Previous button: enabled if current date is after the earliest training day
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

        // Next button: enabled if current date is before the latest training day
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
        viewModel.getCurrentTrainingPlan.observe(viewLifecycleOwner) {
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

        viewModel.createTrainingPlan.observe(viewLifecycleOwner) {
            showLoadingState(false)
            when(it){
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
                }
                else -> Unit
            }
        }

        viewModel.resetTrainingDay.observe(viewLifecycleOwner){
            showLoadingState(it == Resource.Loading)
            when(it){
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    Toast.makeText(
                        requireContext(),
                        it.value.message,
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
            when(it) {
                is Resource.Loading -> {
                    // Show loading if needed
                }
                is Resource.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Phản hồi đã được gửi thành công!",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Refresh the training plan to show updated feedback
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

    private fun showTrainingInputDialog(trainingPlan: TrainingPlan){
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
            displayFeedbackInSession(filteredDay) // Add this line
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

                // Make feedback section clickable to view details
                feedbackSection.setOnClickListener {
                    showViewFeedbackDialog(trainingDay)
                }

                // Add ripple effect for better UX
                val typedValue = TypedValue()
                requireContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
            } else {
                feedbackSection.visibility = View.GONE
            }
        }
    }

    private fun updateTrainingStatusDisplay(trainingDay: TrainingDay) {
        binding.itemDetails.itemTrainingDay.trainingSessionCard.apply {
            // Format status text with completion percentage for COMPLETED and PARTIALLY_COMPLETED
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

            if (trainingDay.trainingFeedback == null) {
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
            // Handle feedback submission
            viewModel.submitTrainingFeedback(trainingDay.id, feedback)
        }
        dialog.show()
    }

    private fun showViewFeedbackDialog(trainingDay: TrainingDay) {
        val dialog = TrainingFeedbackDialog(
            context = requireContext(),
            existingFeedback = trainingDay.trainingFeedback,
            onFeedbackSubmitted = null // No callback needed for view-only mode
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


    private fun showResetConfirmationDialog(currentDay: TrainingDay) {
        val message = """
            Bạn có chắc chắn muốn reset tiến trình tập luyện này không?
            
            📅 Ngày: ${DateUtils.formatTrainingDayString(currentDay)}
            🏃‍♂️ Bài tập: ${currentDay.session.name}
            
            ⚠️ Tất cả dữ liệu tiến trình sẽ bị xóa và không thể khôi phục!
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận Reset")
            .setMessage(message)
            .setIcon(R.drawable.ic_info)
            .setPositiveButton("Reset") { _, _ ->
                viewModel.resetTrainingDay()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateSessionProgress(trainingDay: TrainingDay) {
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

        if (trainingDay.records.isNotEmpty()) {
            avgHeartRate /= trainingDay.records.size
        }

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
        return listOf(TrainingPlanRepository(apiTrainingPlan), TrainingDayRepository(apiTrainingDay),
            TrainingFeedbackRepository(apiTrainingFeedback)
        )
    }
}