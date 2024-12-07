package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.R
import com.university.marathononline.R.string.*;
import com.university.marathononline.R.color.*;
import com.university.marathononline.databinding.ItemContestRunnerBinding
import com.university.marathononline.data.models.Contest
import com.university.marathononline.ui.components.ContestStatisticsDialog
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.getContestStatusColor
import com.university.marathononline.utils.getContestStatusText
import com.university.marathononline.utils.startNewActivity
import com.university.marathononline.utils.updateCompletionStatus

class ContestRunnerAdapter(private var contests: List<Contest>, private val email: String) : RecyclerView.Adapter<ContestRunnerAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemContestRunnerBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Contest, email: String) {
            binding.apply {
                val context = itemView.context

                tvContestName.text = item.name

                tvContestStatus.apply {
                    text = getContestStatusText(context, item.status!!)
                    setTextColor(getContestStatusColor(context, item.status!!))
                }

                val startDate = item.startDate?.let { DateUtils.convertToVietnameseDate(it) }
                val endDate = item.endDate?.let { DateUtils.convertToVietnameseDate(it) }

                context.apply {
                    tvContestDatesStart.text = getString(contest_start_date, startDate ?: getString(date_not_available))
                    tvContestDatesEnd.text = getString(contest_end_date, endDate ?: getString(date_not_available))
                }


                val registration = item.registrations?.find { it.runner.email == email }

                tvCompletionStatus.apply {
                    updateCompletionStatus(context, tvCompletionStatus, registration!!.status)
                }

                val currentDistance = registration!!.races.sumOf { it.distance }
                val contestDistance = item.distance
                val ratio = (currentDistance / contestDistance!!)*100
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
                    val dialog = ContestStatisticsDialog(context, item, email)
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
        // Gọi phương thức bind để cập nhật UI
        holder.bind(contests[position], email)
    }

    override fun getItemCount(): Int = contests.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newContest: List<Contest>) {
        contests = newContest
        notifyDataSetChanged()
    }
}
