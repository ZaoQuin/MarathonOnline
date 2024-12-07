package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.databinding.ItemEditRewardBinding
import com.university.marathononline.data.models.Reward

class EditRewardAdapter(
    private var rewards: List<Reward>,
    private val onEditClick: (Reward) -> Unit,
    private val onDeleteClick: (Reward) -> Unit
) : RecyclerView.Adapter<EditRewardAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemEditRewardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reward: Reward, onEditClick: (Reward) -> Unit, onDeleteClick: (Reward) -> Unit) {
            binding.apply {
                tvName.text = reward.name
                btnEdit.setOnClickListener { onEditClick(reward) }
                btnDelete.setOnClickListener { onDeleteClick(reward) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = rewards.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(rewards[position], onEditClick, onDeleteClick)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newRewards: List<Reward>) {
        rewards = newRewards
        notifyDataSetChanged()
    }

    fun getCurrentData(): List<Reward> {
        return rewards
    }
}
