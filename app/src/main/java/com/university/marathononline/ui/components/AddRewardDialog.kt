package com.university.marathononline.ui.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.university.marathononline.R
import com.university.marathononline.data.models.ERewardType
import com.university.marathononline.data.models.Reward

class AddRewardDialog(
    context: Context,
    private val onRewardAdded: (Reward) -> Unit
) : Dialog(context) {

    private lateinit var etRewardName: EditText
    private lateinit var etRewardDescription: EditText
    private lateinit var etRewardRank: EditText
    private lateinit var spinnerRewardType: Spinner
    private lateinit var btnSaveReward: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_reward)

        // Initialize views
        etRewardName = findViewById(R.id.etRewardName)
        etRewardDescription = findViewById(R.id.etRewardDescription)
        etRewardRank = findViewById(R.id.etRewardRank)
        spinnerRewardType = findViewById(R.id.spinnerRewardType)
        btnSaveReward = findViewById(R.id.btnSaveReward)
        btnCancel = findViewById(R.id.btnCancel)

        // Setup Reward Type Spinner
        val rewardTypes = ERewardType.values()
        val adapter = android.widget.ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            rewardTypes.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRewardType.adapter = adapter

        // Save Reward button click listener
        btnSaveReward.setOnClickListener {
            val rewardName = etRewardName.text.toString().trim()
            val rewardDescription = etRewardDescription.text.toString().trim()
            val rewardRankStr = etRewardRank.text.toString().trim()
            val rewardType = spinnerRewardType.selectedItem.toString()

            if (rewardName.isNotEmpty() && rewardDescription.isNotEmpty() && rewardRankStr.isNotEmpty()) {
                try {
                    val rewardRank = rewardRankStr.toInt()
                    val newReward = Reward(
                        id = System.currentTimeMillis(), // Temporary ID generation
                        name = rewardName,
                        description = rewardDescription,
                        rewardRank = rewardRank,
                        type = ERewardType.valueOf(rewardType),
                        isClaim = false
                    )
                    onRewardAdded(newReward)
                    dismiss()
                } catch (e: NumberFormatException) {
                    etRewardRank.error = "Invalid rank number"
                }
            } else {
                // Show errors for empty fields
                if (rewardName.isEmpty()) etRewardName.error = "Reward name is required"
                if (rewardDescription.isEmpty()) etRewardDescription.error = "Reward description is required"
                if (rewardRankStr.isEmpty()) etRewardRank.error = "Reward rank is required"
            }
        }

        // Cancel button click listener
        btnCancel.setOnClickListener {
            dismiss()
        }
    }
}