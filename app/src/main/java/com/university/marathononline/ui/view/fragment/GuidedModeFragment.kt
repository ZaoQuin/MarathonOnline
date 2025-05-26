package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.university.marathononline.R
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.api.record.RecordApiService
import com.university.marathononline.data.api.registration.RegistrationApiService
import com.university.marathononline.data.api.trainingDay.TrainingDayApiService
import com.university.marathononline.data.models.ETraingDayStatus
import com.university.marathononline.data.models.ETrainingSessionType
import com.university.marathononline.data.models.TrainingDay
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.data.repository.RegistrationRepository
import com.university.marathononline.data.repository.TrainingDayRepository
import com.university.marathononline.databinding.FragmentGuidedModeBinding
import com.university.marathononline.ui.viewModel.RecordViewModel
import com.university.marathononline.utils.visible
import handleApiError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class GuidedModeFragment: BaseFragment<RecordViewModel, FragmentGuidedModeBinding>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        initializeGuidedMode()
    }

    private fun setupObservers() {
        viewModel.getCurrentTrainingDay.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    displayGuidedModeData(it.value)
                    showGuidedModeUI(true)
                }
                is Resource.Failure -> {
                    handleApiError(it)
                    hideGuidedModeUI()
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun initializeGuidedMode() {
        if (viewModel.trainingDay.value == null) {
            viewModel.getCurrentTrainingDay()
        } else {
            displayGuidedModeData(viewModel.trainingDay.value!!)
            showGuidedModeUI(true)
        }
    }

    private fun displayGuidedModeData(trainingDay: TrainingDay) {
        if (trainingDay.session.type == ETrainingSessionType.REST) {
            showGuidedModeUI(false)
            binding.apply {
                // Display basic training data
                targetMetrics.visibility = View.GONE
                trainingDayTitle.text = trainingDay.session.name
                trainingDayDescription.text = trainingDay.session.notes
                updateUIForStatus(trainingDay.status)
            }
            Toast.makeText(
                requireContext(),
                "Hôm nay là ngày nghỉ ngơi",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        binding.apply {
            // Display basic training data
            targetPace.text = "${trainingDay.session.pace} min/km"
            targetDistance.text = "${trainingDay.session.distance} km"
            trainingDayTitle.text = trainingDay.session.name
            trainingDayDescription.text = trainingDay.session.notes

            // Update UI based on status
            updateUIForStatus(trainingDay.status)
        }
    }

    private fun updateUIForStatus(status: ETraingDayStatus) {
        binding.apply {
            when (status) {
                ETraingDayStatus.ACTIVE -> {
                    // Active state - normal blue/green colors
                    guidedModeContainer.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.dark_main_color)
                    )
                    statusIndicator.apply {
                        visible(true)
                        text = "● ${status.value}"
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color))
                    }
                    targetPaceCard.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.main_color)
                    )
                    targetDistanceCard.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.main_color)
                    )
                    headerIcon.setImageResource(R.drawable.ic_distance)
                    headerIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
                }

                ETraingDayStatus.COMPLETED -> {
                    // Completed state - success green colors
                    guidedModeContainer.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.main_color)
                    )
                    statusIndicator.apply {
                        visible(true)
                        text = "✓ ${status.value}"
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    }
                    targetPaceCard.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.dark_main_color)
                    )
                    targetDistanceCard.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.dark_main_color)
                    )
                    headerIcon.setImageResource(R.drawable.ic_completed)
                    headerIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))

                    // Show completion message
                    trainingDayDescription.text = "Chúc mừng! Bạn đã hoàn thành buổi tập này."
                }

                ETraingDayStatus.MISSED  -> {
                    // Missed state - warning/error colors
                    guidedModeContainer.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.light_red)
                    )
                    statusIndicator.apply {
                        visible(true)
                        text = "⚠ ${status.value}"
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    }
                    targetPaceCard.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.light_red)
                    )
                    targetDistanceCard.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.light_red)
                    )
                    headerIcon.setImageResource(R.drawable.ic_info)
                    headerIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))

                    // Show missed message
                    trainingDayDescription.text = "Buổi tập này đã bị bỏ lỡ. Hãy cố gắng hơn trong các buổi tập tiếp theo!"
                }
            }
        }
    }

    private fun showGuidedModeUI(show: Boolean) {
        binding.guidedModeContainer.visible(show)
    }

    private fun hideGuidedModeUI() {
        binding.guidedModeContainer.visible(false)
    }

    fun updateCurrentStats(currentPace: String, currentDistance: String) {
        // Update current stats and compare with targets
        // This can be called from the parent activity
        binding.apply {
            // Show progress section when recording is active
            progressSection.visible(true)

            // Calculate progress based on target distance
            val targetDistanceValue = targetDistance.text.toString().replace(" km", "").toFloatOrNull() ?: 0f
            val currentDistanceValue = currentDistance.replace(" km", "").toFloatOrNull() ?: 0f

            if (targetDistanceValue > 0) {
                val progress = ((currentDistanceValue / targetDistanceValue) * 100).toInt().coerceAtMost(100)
                progressBar.progress = progress
                progressText.text = "$progress% hoàn thành"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun getViewModel() = RecordViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentGuidedModeBinding.inflate(inflater, container, false)

    override fun getFragmentRepositories(): List<BaseRepository> {
        val token = runBlocking { userPreferences.authToken.first() }
        val apiRegistration = retrofitInstance.buildApi(RegistrationApiService::class.java, token)
        val apiRecord = retrofitInstance.buildApi(RecordApiService::class.java, token)
        val apiTrainingDay = retrofitInstance.buildApi(TrainingDayApiService::class.java, token)
        return listOf(
            RegistrationRepository(apiRegistration),
            RecordRepository(apiRecord),
            TrainingDayRepository(apiTrainingDay)
        )
    }
}