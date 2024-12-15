package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.R
import com.university.marathononline.data.models.ENotificationType
import com.university.marathononline.databinding.ItemNotifyBinding
import com.university.marathononline.data.models.Notification
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.ui.view.activity.ManagementDetailsContestActivity
import com.university.marathononline.ui.view.activity.RunnerRewardsActivity
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.KEY_CONTESTS
import com.university.marathononline.utils.KEY_EMAIL
import com.university.marathononline.utils.startNewActivity

class NotifyAdapter(private var notifies: List<Notification>,
                    private val setRead: (Notification) -> Unit): RecyclerView.Adapter<NotifyAdapter.ViewHolder>() {
    class ViewHolder (private val binding: ItemNotifyBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: Notification, setRead: (Notification) -> Unit){
            binding.apply {
                timeStamp.text = item.createAt?.let { DateUtils.convertToVietnameseDateTime(it) }
                title.text = item.title
                content.text = item.content
                val contest = item.contest

                if(item.isRead == true)
                    title.setTextColor(itemView.context.getColor(R.color.gray))

                contest?.let {
                    notifyCardView.setOnClickListener {
                        if(item.isRead == false)
                            setRead(item)
                        if(item.type == ENotificationType.ACCEPT_CONTEST ||
                            item.type == ENotificationType.NOT_APPROVAL_CONTEST )
                            it.context.startNewActivity(
                                ManagementDetailsContestActivity::class.java,
                                mapOf(KEY_CONTEST to contest)
                            )
                        else
                        if(item.type == ENotificationType.NEW_CONTEST ||
                            item.type == ENotificationType.BLOCK_CONTEST)
                            it.context.startNewActivity(
                                ContestDetailsActivity::class.java,
                                mapOf(KEY_CONTEST to contest)
                            )
                        else {
                            val email = item.receiver?.email
                            val contests = listOf(contest)
                            if(email != null && contests != null) {
                                it.context.startNewActivity(
                                    RunnerRewardsActivity::class.java,
                                    mapOf(
                                        KEY_EMAIL to email,
                                        KEY_CONTESTS to contests
                                    )
                                )
                            }
                        }
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
        val sortedNotifies = notifies.sortedByDescending { DateUtils.convertStringToLocalDateTime(it.createAt!!) }
        holder.bind(sortedNotifies[position], setRead)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newNotifies: List<Notification>){
        notifies = newNotifies
        notifyDataSetChanged()
    }

    fun getCurrentData(): List<Notification> {
        return notifies
    }
}
