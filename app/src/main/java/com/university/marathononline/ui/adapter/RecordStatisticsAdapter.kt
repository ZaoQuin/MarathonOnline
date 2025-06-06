package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.data.models.Record
import com.university.marathononline.databinding.ItemRecordStatisticsBinding
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.formatSpeed

class RecordStatisticsAdapter (private var records: List<Record>) :
    RecyclerView.Adapter<RecordStatisticsAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemRecordStatisticsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Record) {
            binding.apply {
                tvTimestamp.text = DateUtils.formatLocalDateTimeStrToDateTimeString(item.startTime)

                tvDistance.text = formatDistance(item.distance)

                tvTimeTaken.text = DateUtils.convertSecondsToHHMMSS(DateUtils.getDurationBetween(item.startTime, item.endTime).seconds)

                tvAvgSpeed.text = formatSpeed(item.avgSpeed)

                tvSteps.text = item.steps.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordStatisticsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = records.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(records[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newRecords: List<Record>) {
        records = newRecords
        notifyDataSetChanged()
    }
}