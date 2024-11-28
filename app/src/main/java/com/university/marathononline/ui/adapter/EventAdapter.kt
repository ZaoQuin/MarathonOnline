package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.databinding.ItemContestBinding
import com.university.marathononline.data.models.Contest
import com.university.marathononline.utils.DateUtils

class EventAdapter(private var events: List<Contest>) :
    RecyclerView.Adapter<EventAdapter.ViewHolder>() {

//    private val runnable = Runnable {
//        if (events.isNotEmpty()) {
//            val nextItem = (viewPager2.currentItem + 1) % events.size
//            viewPager2.setCurrentItem(nextItem, true)
//        }
//    }

    class ViewHolder(private val binding: ItemContestBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Contest) {
            binding.raceNameTextView.text = item.name
            binding.raceStartDateTextView.text = item.startDate?.let { DateUtils.getFormattedDate(it) }
            binding.raceEndDateTextView.text = item.endDate?.let { DateUtils.getFormattedDate(it) }
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
