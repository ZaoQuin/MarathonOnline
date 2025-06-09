package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.R
import com.university.marathononline.data.models.ENotificationType
import com.university.marathononline.data.models.Notification
import com.university.marathononline.databinding.ItemNotifyBinding
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.ui.view.activity.RecordFeedbackActivity
import com.university.marathononline.ui.view.activity.RunnerRewardsActivity
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_CONTEST_ID
import com.university.marathononline.utils.KEY_FEEDBACK_ID
import com.university.marathononline.utils.KEY_RECORD_ID
import com.university.marathononline.utils.KEY_REGISTRATION_ID
import com.university.marathononline.utils.startNewActivity

class NotifyAdapter(
    private var notifies: List<Notification>,
    private val setRead: (Notification) -> Unit
) : RecyclerView.Adapter<NotifyAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemNotifyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Notification, setRead: (Notification) -> Unit) {
            binding.apply {
                timeStamp.text = item.createAt?.let { DateUtils.convertToVietnameseDateTime(it) }
                title.text = item.title
                content.text = item.content

                // Update UI based on read status
                if (item.isRead == true) {
                    title.setTextColor(itemView.context.getColor(R.color.gray))
                    binding.icon.setColorFilter(
                        ContextCompat.getColor(itemView.context, R.color.gray),
                        PorterDuff.Mode.SRC_IN
                    )
                    ViewCompat.setBackgroundTintList(
                        binding.unreadIndicator,
                        ColorStateList.valueOf(
                            ContextCompat.getColor(itemView.context, R.color.gray)
                        )
                    )
                } else {
                    // Reset to default colors when unread
                    title.setTextColor(itemView.context.getColor(R.color.main_color)) // Adjust to your default color
                    binding.icon.setColorFilter(
                        ContextCompat.getColor(itemView.context, R.color.main_color), // Adjust to your default color
                        PorterDuff.Mode.SRC_IN
                    )
                    ViewCompat.setBackgroundTintList(
                        binding.unreadIndicator,
                        ColorStateList.valueOf(
                            ContextCompat.getColor(itemView.context, R.color.main_color)) // Adjust to your unread indicator color
                    )
                }

                notifyCardView.setOnClickListener {
                    if (item.isRead == false) {
                        setRead(item)
                    }

                    when (item.type) {
                        ENotificationType.REWARD -> {
                            item.objectId?.let { objectId ->
                                it.context.startNewActivity(
                                    RunnerRewardsActivity::class.java,
                                    mapOf(KEY_CONTEST_ID to objectId)
                                )
                            }
                        }
                        ENotificationType.NEW_CONTEST -> {
                            item.objectId?.let { objectId ->
                                it.context.startNewActivity(
                                    ContestDetailsActivity::class.java,
                                    mapOf(KEY_CONTEST_ID to objectId)
                                )
                            }
                        }
                        ENotificationType.REJECTED_RECORD,
                        ENotificationType.RECORD_FEEDBACK -> {
                            item.objectId?.let { objectId ->
                                it.context.startNewActivity(
                                    RecordFeedbackActivity::class.java,
                                    mapOf(
                                        KEY_RECORD_ID to objectId,
                                        KEY_FEEDBACK_ID to item.id!!
                                    )
                                )
                            }
                        }
                        ENotificationType.BLOCK_CONTEST -> {
                            item.objectId?.let { objectId ->
                                it.context.startNewActivity(
                                    RecordFeedbackActivity::class.java,
                                    mapOf(KEY_REGISTRATION_ID to objectId)
                                )
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotifyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = notifies.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(notifies[position], setRead)
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newNotifies: List<Notification>) {
        val sortedNotifies = newNotifies.sortedByDescending { notification ->
            notification.createAt?.let {
                try {
                    DateUtils.convertStringToLocalDateTime(it)
                } catch (e: Exception) {
                    null
                }
            }
        }
        val diffCallback = NotificationDiffCallback(notifies, sortedNotifies)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        notifies = sortedNotifies
        diffResult.dispatchUpdatesTo(this)
    }

    fun getCurrentData(): List<Notification> = notifies

    private class NotificationDiffCallback(
        private val oldList: List<Notification>,
        private val newList: List<Notification>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem == newItem && oldItem.isRead == newItem.isRead
        }
    }
}