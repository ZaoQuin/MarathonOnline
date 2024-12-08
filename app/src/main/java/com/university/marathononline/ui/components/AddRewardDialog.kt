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
    private val onRewardAdded: (Reward) -> Unit,
    private val onRewardUpdated: (Reward) -> Unit // Hàm callback khi cập nhật phần thưởng
) : Dialog(context) {

    private lateinit var binding: DialogAddRewardBinding
    private var rewardToEdit: Reward? = null

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
                        id = rewardToEdit?.id ?: System.currentTimeMillis(), // Nếu là chỉnh sửa, dùng id cũ
                        name = rewardName,
                        description = rewardDescription,
                        rewardRank = rewardRank,
                        type = ERewardType.valueOf(binding.spinnerRewardType.selectedItem.toString()),
                        isClaim = rewardToEdit?.isClaim ?: false // Nếu chỉnh sửa, giữ lại giá trị `isClaim` cũ
                    )

                    if (rewardToEdit == null) {
                        onRewardAdded(newReward)
                    } else {
                        onRewardUpdated(newReward) // Nếu là cập nhật, gọi hàm onRewardUpdated
                    }

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

    override fun onStart() {
        super.onStart()
        rewardToEdit?.let {
            setReward(it)
        }
    }

    // Hàm để cập nhật các trường trong dialog với dữ liệu của phần thưởng
    fun setReward(reward: Reward) {
        rewardToEdit = reward
        if (this::binding.isInitialized) {
            binding.etRewardName.setText(reward.name)
            binding.etRewardDescription.setText(reward.description)
            binding.etRewardRank.setText(reward.rewardRank.toString())
            binding.spinnerRewardType.setSelection(ERewardType.values().indexOf(reward.type))
        }
    }
}
