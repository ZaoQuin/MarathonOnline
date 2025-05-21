package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.databinding.ItemContestBinding
import com.university.marathononline.data.models.Contest
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.convertToVND
import com.university.marathononline.utils.enableRegister
import com.university.marathononline.utils.getContestStatusColor
import com.university.marathononline.utils.isStarting
import com.university.marathononline.utils.startNewActivity
import com.university.marathononline.utils.visible

class ContestAdapter(private var contests: List<Contest>) :
    RecyclerView.Adapter<ContestAdapter.ViewHolder>() {
    class ViewHolder(private val binding: ItemContestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Contest) {
            binding.apply {
                recordNameTextView.text = item.name
                recordStartDateTextView.text = DateUtils.convertToVietnameseDate(item.startDate!!)
                recordEndDateTextView.text = DateUtils.convertToVietnameseDate(item.endDate!!)
                recordDeadlineRegisterTextView.text = DateUtils.convertToVietnameseDate(item.registrationDeadline!!)

                countMembersText.text = "${item.registrations?.size.toString()}/ ${item.maxMembers}"
                registrationFee.text = item.fee?.let { convertToVND(it) }

                contestCardView.setOnClickListener {
                    it.context.startNewActivity(
                        ContestDetailsActivity::class.java,
                        mapOf(KEY_CONTEST to item)
                    )
                }

                tvContestStatus.apply {
                    text = item.status?.value
                    setTextColor(getContestStatusColor(context, item.status!!))
                }

                tvStartStatus.visible( isStarting(item))
                tvRegisterStatus.visible(enableRegister(item))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = contests.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(contests[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newContest: List<Contest>) {
        contests = newContest
        notifyDataSetChanged()
    }
}