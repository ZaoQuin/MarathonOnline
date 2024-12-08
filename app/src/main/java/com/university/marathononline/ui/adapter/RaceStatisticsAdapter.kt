package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.data.models.Race
import com.university.marathononline.databinding.ItemRaceStatisticsBinding
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.formatSpeed

class RaceStatisticsAdapter (private var races: List<Race>) :
    RecyclerView.Adapter<RaceStatisticsAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemRaceStatisticsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Race) {
            binding.apply {
                tvTimestamp.text = DateUtils.formatLocalDateTimeStrToDateTimeString(item.timestamp)

                tvDistance.text = formatDistance(item.distance)

                tvTimeTaken.text = DateUtils.convertSecondsToHHMMSS(item.timeTaken)

                tvAvgSpeed.text = formatSpeed(item.avgSpeed)

                tvSteps.text = item.steps.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRaceStatisticsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = races.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(races[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newRaces: List<Race>) {
        races = newRaces
        notifyDataSetChanged()
    }
}