package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.R
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.ERewardType
import com.university.marathononline.data.models.Reward
import com.university.marathononline.databinding.ItemRunnerRewardBinding
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.startNewActivity

class RunnerRewardAdapter(
    private var rewardOfContests: MutableList<Pair<Contest, Reward>>,
    private val onItemClick: ((Reward, Contest) -> Unit)? = null
) : RecyclerView.Adapter<RunnerRewardAdapter.ViewHolder>() {

    class ViewHolder(
        private val binding: ItemRunnerRewardBinding,
        private val onItemClick: ((Reward, Contest) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Reward, contest: Contest, position: Int) {
            binding.apply {
                // Basic reward info
                rewardName.text = item.name ?: "No Name"
                rewardDescription.text = item.description ?: "No Description"
                contestName.text = contest.name

                // Handle rank display
                when (item.rewardRank) {
                    0 -> {
                        rewardRank.text = "🏅 Hoàn thành cuộc thi"
                        rewardRank.setTextColor(ContextCompat.getColor(binding.root.context, R.color.success_color))
                    }
                    1 -> {
                        rewardRank.text = "🥇 Hạng nhất"
                        rewardRank.setTextColor(ContextCompat.getColor(binding.root.context, R.color.gold_color))
                    }
                    2 -> {
                        rewardRank.text = "🥈 Hạng nhì"
                        rewardRank.setTextColor(ContextCompat.getColor(binding.root.context, R.color.silver_color))
                    }
                    3 -> {
                        rewardRank.text = "🥉 Hạng ba"
                        rewardRank.setTextColor(ContextCompat.getColor(binding.root.context, R.color.bronze_color))
                    }
                    else -> {
                        rewardRank.text = "🏆 Hạng ${item.rewardRank}"
                        rewardRank.setTextColor(ContextCompat.getColor(binding.root.context, R.color.text_color))
                    }
                }

                // Highlight special rewards (top 3 ranks)
                val cardView = rewardCardView.parent as androidx.cardview.widget.CardView
                when (item.rewardRank) {
                    1 -> {
                        // Gold highlight for 1st place
                        cardView.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.gold_background))
                        cardView.cardElevation = 16f
                        rewardName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.gold_text))
                    }
                    2 -> {
                        // Silver highlight for 2nd place
                        cardView.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.silver_background))
                        cardView.cardElevation = 14f
                        rewardName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.silver_text))
                    }
                    3 -> {
                        // Bronze highlight for 3rd place
                        cardView.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.bronze_background))
                        cardView.cardElevation = 12f
                        rewardName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.bronze_text))
                    }
                    else -> {
                        // Default styling
                        cardView.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.white))
                        cardView.cardElevation = 8f
                        rewardName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.main_color))
                    }
                }

                // Add reward type indicator
                val typeIndicator = when (item.type) {
                    ERewardType.PHYSICAL -> "📦 Vật lý"
                    ERewardType.VIRTUAL -> "💻 Ảo"
                }

                // You can add this to your layout or append to description
                rewardDescription.text = "${item.description ?: "No Description"} • $typeIndicator"

                // Handle clicks
                rewardCardView.setOnClickListener {
                    onItemClick?.invoke(item, contest) ?: run {
                        // Default action: navigate to contest details
                        it.context.startNewActivity(
                            ContestDetailsActivity::class.java,
                            mapOf(KEY_CONTEST to contest)
                        )
                    }
                }

                // Add animation for top rewards
                if (item.rewardRank in 1..3) {
                    binding.root.animate()
                        .scaleX(1.02f)
                        .scaleY(1.02f)
                        .setDuration(200)
                        .start()
                } else {
                    binding.root.scaleX = 1f
                    binding.root.scaleY = 1f
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRunnerRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = rewardOfContests[position]
        val contest = item.first
        val reward = item.second
        holder.bind(reward, contest, position)
    }

    override fun getItemCount(): Int = rewardOfContests.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: MutableList<Pair<Contest, Reward>>) {
        rewardOfContests = newData
        notifyDataSetChanged()
    }

    fun addReward(contest: Contest, reward: Reward) {
        rewardOfContests.add(Pair(contest, reward))
        notifyItemInserted(rewardOfContests.size - 1)
    }

    fun removeReward(position: Int) {
        if (position in 0 until rewardOfContests.size) {
            rewardOfContests.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}