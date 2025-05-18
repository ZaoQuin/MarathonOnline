package com.university.marathononline.ui.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.SeekBar
import com.university.marathononline.R
import com.university.marathononline.data.models.ETrainingPlanInputGoal
import com.university.marathononline.data.models.ETrainingPlanInputLevel
import com.university.marathononline.data.request.InputTrainingPlanRequest
import com.university.marathononline.databinding.DialogCreateTrainingPlanBinding

class CreateTrainingPlanDialog(
    context: Context,
    private val onCreatePlan: (InputTrainingPlanRequest) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogCreateTrainingPlanBinding
    private var selectedLevel = ETrainingPlanInputLevel.BEGINNER
    private var selectedGoal = ETrainingPlanInputGoal.FIVE_KM_FINISH
    private var daysPerWeek = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogCreateTrainingPlanBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Setup goal spinner
        val goals = ETrainingPlanInputGoal.values().map { it.toString() }.toTypedArray()
        val goalAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, goals)
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.goalSpinner.adapter = goalAdapter

        // Set initial days per week
        binding.daysPerWeekSeekbar.progress = daysPerWeek
        binding.daysPerWeekText.text = "$daysPerWeek ngày/tuần"

        // Set default radio button
        binding.levelBeginner.isChecked = true
    }

    private fun setupListeners() {
        // Level selection
        binding.levelRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedLevel = when (checkedId) {
                R.id.level_beginner -> ETrainingPlanInputLevel.BEGINNER
                R.id.level_intermediate -> ETrainingPlanInputLevel.INTERMEDIATE
                R.id.level_advanced -> ETrainingPlanInputLevel.ADVANCED
                else -> ETrainingPlanInputLevel.BEGINNER
            }
        }

        // Goal selection
        binding.goalSpinner.setOnItemSelectedListener { _, _, position, _ ->
            selectedGoal = ETrainingPlanInputGoal.values()[position]
        }

        // Days per week seek bar
        binding.daysPerWeekSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Ensure at least 1 day per week
                daysPerWeek = if (progress < 1) 1 else progress
                binding.daysPerWeekText.text = "$daysPerWeek ngày/tuần"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Button listeners
        binding.createPlanButton.setOnClickListener {
            val trainingPlanRequest = InputTrainingPlanRequest(
                level = selectedLevel,
                goal = selectedGoal,
                daysPerWeek = daysPerWeek
            )

            onCreatePlan(trainingPlanRequest)
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    // Helper extension function for spinner item selection
    private fun android.widget.Spinner.setOnItemSelectedListener(
        onItemSelected: (parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) -> Unit
    ) {
        this.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                onItemSelected(parent, view, position, id)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }
}