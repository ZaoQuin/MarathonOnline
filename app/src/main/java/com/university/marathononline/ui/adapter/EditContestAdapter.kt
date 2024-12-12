package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.databinding.ItemEditContestBinding
import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.ERegistrationStatus
import com.university.marathononline.ui.view.activity.ManagementDetailsContestActivity
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.getContestStatusColor
import com.university.marathononline.utils.isStarting
import com.university.marathononline.utils.enableRegister
import com.university.marathononline.utils.startNewActivity
import com.university.marathononline.utils.visible

class EditContestAdapter(private var contests: List<Contest>) :
    RecyclerView.Adapter<EditContestAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemEditContestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Contest) {
            binding.apply {
                tvContestName.text = item.name
                tvContestDescription.text = item.description
                tvContestStatus.apply {
                    text = item.status?.value
                    setTextColor(getContestStatusColor(context, item.status!!))
                }

                tvStartStatus.visible(isStarting(item))

                tvRegistrationStatus.visible(enableRegister(item))

                val count = item.registrations?.count{
                    it.status != ERegistrationStatus.PENDING
                }.toString()

                tvRegistrationCount.text = "Số lượng: ${count}/ ${item.maxMembers}"

                contestCardView.setOnClickListener{
                    it.context.startNewActivity(ManagementDetailsContestActivity::class.java,
                        mapOf(KEY_CONTEST to item)
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditContestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
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
