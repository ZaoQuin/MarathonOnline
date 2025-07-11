package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.R
import com.university.marathononline.databinding.ItemRewardGroupBinding
import com.university.marathononline.databinding.ItemRewardBinding
import com.university.marathononline.data.models.Reward
import com.university.marathononline.data.models.RewardGroup

class RewardAdapter(private var rewardGroups: List<RewardGroup>) :
    RecyclerView.Adapter<RewardAdapter.RewardGroupViewHolder>() {

    class RewardGroupViewHolder(private val binding: ItemRewardGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(group: RewardGroup) {
            binding.apply {
                if(group.rewardRank != 0)
                    rewardRankTextView.text = "Xếp hạng: ${group.rewardRank}"
                else
                    rewardRankTextView.text = "Hoàn thành cuộc thi"
                rewardsRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
                val childAdapter = RewardChildAdapter(group.rewards)
                rewardsRecyclerView.adapter = childAdapter
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardGroupViewHolder {
        val binding =
            ItemRewardGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RewardGroupViewHolder(binding)
    }

    override fun getItemCount(): Int = rewardGroups.size

    override fun onBindViewHolder(holder: RewardGroupViewHolder, position: Int) {
        holder.bind(rewardGroups[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newGroups: List<RewardGroup>) {
        rewardGroups = newGroups
        notifyDataSetChanged()
    }
}

class RewardChildAdapter(private val rewards: List<Reward>) :
    RecyclerView.Adapter<RewardChildAdapter.RewardViewHolder>() {

    class RewardViewHolder(private val binding: ItemRewardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reward: Reward) {
            binding.apply {
                rewardName.text = reward.name
                rewardDescription.text = reward.description
                when (reward.rewardRank) {
                    1 -> rewardIcon.setImageResource(R.drawable.ic_gold_medal)
                    2 -> rewardIcon.setImageResource(R.drawable.ic_silver_medal)
                    3 -> rewardIcon.setImageResource(R.drawable.ic_bronze_medal)
                    else -> rewardIcon.setImageResource(R.drawable.ic_reward)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val binding =
            ItemRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RewardViewHolder(binding)
    }

    override fun getItemCount(): Int = rewards.size

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        holder.bind(rewards[position])
    }
}
