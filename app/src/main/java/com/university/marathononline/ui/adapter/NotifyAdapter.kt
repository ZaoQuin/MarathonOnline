package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.R
import com.university.marathononline.data.models.ENotificationType
import com.university.marathononline.databinding.ItemNotifyBinding
import com.university.marathononline.data.models.Notification
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.ui.view.activity.RecordFeedbackActivity
import com.university.marathononline.ui.view.activity.RunnerRewardsActivity
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_CONTEST_ID
import com.university.marathononline.utils.KEY_RECORD_ID
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
                val objectId = item.objectId

                if (item.isRead == true) {
                    title.setTextColor(itemView.context.getColor(R.color.gray))
                    binding.icon.setColorFilter(
                        ContextCompat.getColor(itemView.context, R.color.gray),
                        PorterDuff.Mode.SRC_IN
                    )
                    ViewCompat.setBackgroundTintList(
                        binding.unreadIndicator,
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                itemView.context,
                                R.color.gray
                            )
                        )
                    )
                }


                notifyCardView.setOnClickListener {
                    if (item.isRead == false)
                        setRead(item)

                    when (item.type) {
                        ENotificationType.REWARD -> {
                            it.context.startNewActivity(
                                RunnerRewardsActivity::class.java,
                                mapOf(KEY_CONTEST_ID to objectId!!)
                            )
                        }

                        ENotificationType.NEW_CONTEST,
                        ENotificationType.BLOCK_CONTEST -> {
                            it.context.startNewActivity(
                                ContestDetailsActivity::class.java,
                                mapOf(KEY_CONTEST_ID to objectId!!)
                            )
                        }

                        ENotificationType.REJECTED_RECORD,
                        ENotificationType.RECORD_FEEDBACK -> {
                            it.context.startNewActivity(
                                RecordFeedbackActivity::class.java,
                                mapOf(KEY_RECORD_ID to objectId!!)
                            )
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
        // Fix: Handle null createAt values safely
        val sortedNotifies = notifies.sortedByDescending { notification ->
            notification.createAt?.let {
                try {
                    DateUtils.convertStringToLocalDateTime(it)
                } catch (e: Exception) {
                    // If date parsing fails, treat as oldest date
                    null
                }
            }
            // Notifications with null createAt will be sorted to the end
        }
        holder.bind(sortedNotifies[position], setRead)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newNotifies: List<Notification>) {
        notifies = newNotifies
        notifyDataSetChanged()
    }

    fun getCurrentData(): List<Notification> {
        return notifies
    }
}