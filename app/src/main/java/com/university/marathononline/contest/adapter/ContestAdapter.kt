package com.university.marathononline.contest.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.contest.contestDetails.ContestDetailsActivity
import com.university.marathononline.databinding.ItemContestBinding
import com.university.marathononline.entity.Contest
import com.university.marathononline.utils.DateUtils

class ContestAdapter (private var contests: List<Contest>): RecyclerView.Adapter<ContestAdapter.ViewHolder>(){
    class ViewHolder(private val binding: ItemContestBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: Contest){
            binding.raceNameTextView.text = item.title
            binding.raceStartDateTextView.text = DateUtils.getFormattedDate(item.startDate)
            binding.raceEndDateTextView.text = DateUtils.getFormattedDate(item.endDate)
            binding.countMembersText.text = "0"
            binding.registrationFee.text = item.registrationFee.toString()

            binding.contestCardView.setOnClickListener{
                val intent = Intent(binding.root.context, ContestDetailsActivity::class.java)
                binding.root.context.startActivity(intent)
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