package com.university.marathononline.ui.components

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.university.marathononline.R
import com.university.marathononline.data.models.EDifficultyRating
import com.university.marathononline.data.models.EFeelingRating
import com.university.marathononline.data.models.TrainingFeedback
import com.university.marathononline.databinding.DialogTrainingFeedbackBinding

class TrainingFeedbackDialog(
    context: Context,
    private val existingFeedback: TrainingFeedback? = null,
    private val onFeedbackSubmitted: ((TrainingFeedback) -> Unit)? = null
) : Dialog(context) {

    private lateinit var binding: DialogTrainingFeedbackBinding
    private val isViewOnlyMode = existingFeedback != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogTrainingFeedbackBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        setupDialog()
        setupSpinners()
        setupButtons()

        if (isViewOnlyMode) {
            setupViewOnlyMode()
        }

        // Pre-fill if viewing existing feedback
        existingFeedback?.let { fillExistingFeedback(it) }
    }

    private fun setupDialog() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

    private fun setupSpinners() {
        // Setup Difficulty Rating Spinner
        val difficultyAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            EDifficultyRating.values().map { it.value }
        )
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDifficulty.adapter = difficultyAdapter

        // Setup Feeling Rating Spinner
        val feelingAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            EFeelingRating.values().map { it.value }
        )
        feelingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFeeling.adapter = feelingAdapter
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSubmit.setOnClickListener {
            if (isViewOnlyMode) {
                dismiss()
            } else {
                submitFeedback()
            }
        }
    }

    private fun setupViewOnlyMode() {
        // Change dialog title for view-only mode
        binding.tvDialogTitle.text = "Xem Phản Hồi"

        // Disable spinners for view-only mode
        binding.spinnerDifficulty.isEnabled = false
        binding.spinnerFeeling.isEnabled = false

        // Disable notes editing
        binding.etNotes.isEnabled = false
        binding.etNotes.isFocusable = false
        binding.etNotes.isFocusableInTouchMode = false

        // Change button text and hide cancel button
        binding.btnSubmit.text = "Đóng"
        binding.btnCancel.visibility = View.GONE

        // Adjust button layout to center the single button
        val layoutParams = binding.btnSubmit.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        binding.btnSubmit.layoutParams = layoutParams
    }

    private fun fillExistingFeedback(feedback: TrainingFeedback) {
        binding.spinnerDifficulty.setSelection(feedback.difficultyRating.ordinal)
        binding.spinnerFeeling.setSelection(feedback.feelingRating.ordinal)
        if(feedback.notes.isNullOrEmpty())
            binding.etNotes.setText("Không có bình luận")
        else
            binding.etNotes.setText(feedback.notes)
    }

    private fun submitFeedback() {
        val difficultyPosition = binding.spinnerDifficulty.selectedItemPosition
        val feelingPosition = binding.spinnerFeeling.selectedItemPosition
        val notes = binding.etNotes.text.toString().trim()

        if (difficultyPosition < 0 || feelingPosition < 0) {
            Toast.makeText(context, "Vui lòng chọn đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val feedback = TrainingFeedback(
            id = existingFeedback?.id ?: 0L,
            difficultyRating = EDifficultyRating.values()[difficultyPosition],
            feelingRating = EFeelingRating.values()[feelingPosition],
            notes = notes
        )

        println("Feedback: $feedback")
        onFeedbackSubmitted?.invoke(feedback)
        dismiss()
    }
}