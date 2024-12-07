package com.university.marathononline.ui.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import com.university.marathononline.data.models.Reward
import com.university.marathononline.data.models.ERewardType
import com.university.marathononline.databinding.DialogAddRewardBinding

class AddRewardDialog(
    context: Context,
    private val onRewardAdded: (Reward) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogAddRewardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogAddRewardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rewardTypes = ERewardType.values().map { it.name }
        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            rewardTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRewardType.adapter = adapter

        binding.btnSaveReward.setOnClickListener {
            val rewardName = binding.etRewardName.text.toString().trim()
            val rewardDescription = binding.etRewardDescription.text.toString().trim()
            val rewardRankStr = binding.etRewardRank.text.toString().trim()

            if (rewardName.isNotEmpty() && rewardDescription.isNotEmpty() && rewardRankStr.isNotEmpty()) {
                val rewardRank = rewardRankStr.toIntOrNull()
                if (rewardRank != null) {
                    val newReward = Reward(
                        id = System.currentTimeMillis(),
                        name = rewardName,
                        description = rewardDescription,
                        rewardRank = rewardRank,
                        type = ERewardType.valueOf(binding.spinnerRewardType.selectedItem.toString()),
                        isClaim = false
                    )
                    onRewardAdded(newReward)
                    dismiss()
                } else {
                    binding.etRewardRank.error = "Vui lòng nhập hạng hợp lệ"
                }
            } else {
                if (rewardName.isEmpty()) binding.etRewardName.error = "Tên giải thưởng là bắt buộc"
                if (rewardDescription.isEmpty()) binding.etRewardDescription.error = "Chi tiết giải thưởng là bắt buộc"
                if (rewardRankStr.isEmpty()) binding.etRewardRank.error = "Hạng giải thưởng là bắt buộc"
            }
        }

        // Set up the cancel button click listener
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }
}
