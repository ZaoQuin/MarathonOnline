package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.R.string.*;
import com.university.marathononline.databinding.ItemContestRunnerBinding
import com.university.marathononline.data.models.Contest
import com.university.marathononline.ui.components.ContestStatisticsDialog
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.getContestStatusColor
import com.university.marathononline.utils.startNewActivity

class ContestRunnerAdapter(private var contests: List<Contest>, private val email: String) : RecyclerView.Adapter<ContestRunnerAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemContestRunnerBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Contest, email: String) {
            binding.apply {
                val context = itemView.context

                tvContestName.text = item.name

                tvContestStatus.apply {
                    text = item.status?.value
                    setTextColor(getContestStatusColor(context, item.status!!))
                }

                val startDate = item.startDate?.let { DateUtils.convertToVietnameseDate(it) }
                val endDate = item.endDate?.let { DateUtils.convertToVietnameseDate(it) }

                context.apply {
                    tvContestDatesStart.text = getString(contest_start_date, startDate ?: getString(date_not_available))
                    tvContestDatesEnd.text = getString(contest_end_date, endDate ?: getString(date_not_available))
                }


                val registration = item.registrations?.find { it.runner.email == email }

                tvCompletionStatus.text = "Trạng thái: ${registration!!.status.value }"

                val currentDistance = registration?.records?.sumOf { it.distance ?: 0.0 } ?: 0.0
                val contestDistance = item.distance ?: 0.0

                val ratio = if (contestDistance > 0) (currentDistance / contestDistance) * 100 else 0.0

                binding.apply {
                    processBar.progress = ratio.toInt()
                    processBarValue.text = "${formatDistance(currentDistance)}/${formatDistance(contestDistance)}"
                }

                contestCardView.setOnClickListener {
                    it.context.startNewActivity(
                        ContestDetailsActivity::class.java,
                        mapOf( KEY_CONTEST to item)
                    )
                }

                statisticsContest.setOnClickListener {
                    val context = it.context
                    val dialog = ContestStatisticsDialog(context, item, email, false, null)
                    dialog.show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContestRunnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(contests[position], email)
    }

    override fun getItemCount(): Int = contests.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newContest: List<Contest>) {
        contests = newContest
        notifyDataSetChanged()
    }
}
