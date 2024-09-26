package com.university.marathononline.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.university.marathononline.databinding.ItemEventBinding
import com.university.marathononline.entity.Event
import com.university.marathononline.utils.DateUtils

class EventAdapter(private var events: List<Event>, private val viewPager2: ViewPager2) :
    RecyclerView.Adapter<EventAdapter.ViewHolder>() {
    class ViewHolder(private val binding: ItemEventBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Event) {
            binding.raceNameTextView.text = item.title
            binding.raceStartDateTextView.text = DateUtils.getFormattedDate(item.startDate)
            binding.raceEndDateTextView.text = DateUtils.getFormattedDate(item.endDate)
            binding.countMembersText.text = "0"
            binding.registrationFee.text = item.registrationFee.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(events[position])
        if (position == events.size - 1){
            viewPager2.post(runnable)
        }
    }

    override fun getItemCount(): Int = events.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newEvents: List<Event>?) {
        if(newEvents != null){
            events = newEvents
            notifyDataSetChanged()
        }
    }

    private val runnable = Runnable {
        if (events.isNotEmpty()) {
            val nextItem = (viewPager2.currentItem + 1) % events.size
            viewPager2.setCurrentItem(nextItem, true)
        }
    }
}