package com.university.marathononline.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.R
import com.university.marathononline.data.models.ERecordApprovalStatus
import com.university.marathononline.data.models.Record
import com.university.marathononline.databinding.ItemRecordStatisticsBinding
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.DateUtils.convertSecondsToHHMMSS
import com.university.marathononline.utils.formatDistance
import java.time.Duration

class RecordStatisticsAdapter(
    private val records: List<Record>
) : RecyclerView.Adapter<RecordStatisticsAdapter.RecordStatisticsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordStatisticsViewHolder {
        val binding = ItemRecordStatisticsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordStatisticsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordStatisticsViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount(): Int = records.size

    inner class RecordStatisticsViewHolder(
        private val binding: ItemRecordStatisticsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: Record) {
            with(binding) {
                tvTimestamp.text = DateUtils.formatLocalDateTimeStrToDateTimeString(record.startTime)
                tvDistance.text = formatDistance(record.distance)
                val duration = Duration.between(
                    DateUtils.convertStringToLocalDateTime(record.startTime),
                    DateUtils.convertStringToLocalDateTime(record.endTime))
                tvTimeTaken.text = convertSecondsToHHMMSS(duration.seconds)

                setupApprovalStatus(record)
            }
        }

        private fun setupApprovalStatus(record: Record) {
            val context = binding.root.context

            when (record.approval?.approvalStatus) {
                ERecordApprovalStatus.APPROVED -> {
                    binding.tvApprovalStatus.text = "✓"
                    binding.tvApprovalStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.main_color)
                    )
                }
                ERecordApprovalStatus.PENDING -> {
                    binding.tvApprovalStatus.text = "⏳"
                    binding.tvApprovalStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.warning_color)
                    )
                }
                ERecordApprovalStatus.REJECTED -> {
                    binding.tvApprovalStatus.text = "✗"
                    binding.tvApprovalStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.red)
                    )
                }
                null -> {
                    binding.tvApprovalStatus.text = "○"
                    binding.tvApprovalStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.gray)
                    )
                }
            }

            // Optional: Add fraud risk indicator for managers
            record.approval?.let { approval ->
                if (approval.fraudRisk > 0.7) {
                    binding.tvApprovalStatus.text = "${binding.tvApprovalStatus.text}⚠️"
                }
            }
        }
    }
}