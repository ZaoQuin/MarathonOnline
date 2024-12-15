package com.university.marathononline.ui.adapter

import com.university.marathononline.databinding.ItemRunnerRewardBinding

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.Reward
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.startNewActivity

class RunnerRewardAdapter(private var rewardOfContests: MutableList<Pair<Contest, Reward>>) : RecyclerView.Adapter<RunnerRewardAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemRunnerRewardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Reward, contest: Contest) {
            binding.apply {
                rewardName.text = item.name ?: "No Name"
                rewardDescription.text = item.description ?: "No Description"
                if(item.rewardRank != 0)
                    rewardRank.text = "Hạng: ${item.rewardRank}"
                else
                    rewardRank.text = "Hoàn thành cuộc thi"

                contestName.text = contest.name

                rewardCardView.setOnClickListener {
                    it.context.startNewActivity(
                        ContestDetailsActivity::class.java,
                        mapOf( KEY_CONTEST to contest)
                    )
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRunnerRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = rewardOfContests[position]
        val contest = item.first
        val reward = item.second
        holder.bind(reward, contest)
    }

    override fun getItemCount(): Int = rewardOfContests.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: MutableList<Pair<Contest, Reward>>) {
        rewardOfContests = newData
        notifyDataSetChanged()
    }
}
