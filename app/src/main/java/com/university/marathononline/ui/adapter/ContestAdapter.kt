package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.databinding.ItemContestBinding
import com.university.marathononline.data.models.Contest
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.startNewActivity

class ContestAdapter (private var contests: List<Contest>): RecyclerView.Adapter<ContestAdapter.ViewHolder>(){
    class ViewHolder(private val binding: ItemContestBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: Contest){
            binding.apply {
                raceNameTextView.text = item.name
                raceStartDateTextView.text = item.startDate
                raceEndDateTextView.text = item.endDate
                countMembersText.text = "0"
                registrationFee.text = item.fee.toString()

                contestCardView.setOnClickListener{
                    it.context.startNewActivity(ContestDetailsActivity::class.java,
                        mapOf( KEY_CONTEST to item)
                        )
                }
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