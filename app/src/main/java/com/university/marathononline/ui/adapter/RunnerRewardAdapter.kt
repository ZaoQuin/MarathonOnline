package com.university.marathononline.ui.adapter

import com.university.marathononline.databinding.ItemRunnerRewardBinding

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.data.models.ERewardType
import com.university.marathononline.data.models.Reward
import com.university.marathononline.utils.visible

class RunnerRewardAdapter(private var rewards: List<Reward>) : RecyclerView.Adapter<RunnerRewardAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemRunnerRewardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Reward) {
            binding.apply {
                rewardName.text = item.name ?: "No Name"
                rewardName.text = item.description ?: "No Description"
                rewardRank.text = "Hạng: ${item.rewardRank}"
                claimButton.visible(item.type == ERewardType.PHYSICAL && item.isClaim == false)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRunnerRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(rewards[position])
    }

    override fun getItemCount(): Int = rewards.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newRewards: List<Reward>) {
        rewards = newRewards
        notifyDataSetChanged()
    }
}
