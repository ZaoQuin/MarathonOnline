package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.databinding.ItemContestBinding
import com.university.marathononline.data.models.Contest

class EventAdapter(private var events: List<Contest>) :
    RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemContestBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Contest) {
            binding.raceNameTextView.text = item.name
            binding.raceStartDateTextView.text = item.startDate
            binding.raceEndDateTextView.text = item.endDate
            binding.countMembersText.text = "0"
            binding.registrationFee.text = item.fee.toString()

            binding.contestCardView.setOnClickListener{
                val intent = Intent(binding.root.context, ContestDetailsActivity::class.java)
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemContestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount() = events.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newEvents: List<Contest>) {
        events = newEvents
        notifyDataSetChanged()
    }
}
