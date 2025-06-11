package com.university.marathononline.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.R
import com.university.marathononline.data.models.Feedback
import com.university.marathononline.databinding.ItemFeedbackBinding
import com.university.marathononline.utils.DateUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FeedbackAdapter(
    private var feedbacks: List<Feedback>,
    private val onFeedbackClick: (Feedback) -> Unit
) : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    inner class FeedbackViewHolder(
        private val binding: ItemFeedbackBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(feedback: Feedback) {
            binding.apply {
                tvSenderName.text = feedback.sender.fullName ?: feedback.sender.username
                tvSenderRole.text = when {
                    feedback.sender.role?.name?.contains("ADMIN", true) == true -> "Admin"
                    feedback.sender.role?.name?.contains("ORGANIZER", true) == true -> "Organizer"
                    feedback.sender.role?.name?.contains("RUNNER", true) == true -> "Runner"
                    else -> "User"
                }

                tvMessage.text = feedback.message

                tvSentAt.text = try {
                   DateUtils.formatLocalDateTimeStrToDateTimeString(feedback.sentAt)
                } catch (e: Exception) {
                    "Không xác định"
                }

                val backgroundColor = when {
                    feedback.sender.role?.name?.contains("RUNNER", true) == true -> {
                        itemView.context.getColor(android.R.color.white)
                    }
                    else -> {
                        itemView.context.getColor(R.color.light_main_color)
                    }
                }
                cardView.setCardBackgroundColor(backgroundColor)

                root.setOnClickListener {
                    onFeedbackClick(feedback)
                }

                root.setOnLongClickListener {
                    onFeedbackClick(feedback)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val binding = ItemFeedbackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FeedbackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        holder.bind(feedbacks[position])
    }

    override fun getItemCount() = feedbacks.size

    fun updateData(newFeedbacks: List<Feedback>) {
        val sortedList = newFeedbacks.sortedBy {
            it.sentAt?.let { sentAt ->
                try {
                    DateUtils.convertStringToLocalDateTime(sentAt)
                } catch (e: Exception) {
                    Log.w("FeedbackAdapter", "Error parsing sentAt: ${e.message}")
                    LocalDateTime.now()
                }
            } ?: LocalDateTime.now()
        }
        val diffCallback = FeedbackDiffCallback(feedbacks, sortedList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        feedbacks = sortedList
        diffResult.dispatchUpdatesTo(this)
    }

    fun getCurrentData(): List<Feedback> = feedbacks

    private class FeedbackDiffCallback(
        private val oldList: List<Feedback>,
        private val newList: List<Feedback>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldFeedback = oldList[oldItemPosition]
            val newFeedback = newList[newItemPosition]

            return oldFeedback.id == newFeedback.id &&
                    oldFeedback.message == newFeedback.message &&
                    oldFeedback.sentAt == newFeedback.sentAt &&
                    oldFeedback.sender.id == newFeedback.sender.id
        }
    }
}