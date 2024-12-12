package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.data.models.ENotificationType
import com.university.marathononline.databinding.ItemNotifyBinding
import com.university.marathononline.data.models.Notification
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.utils.DateUtils
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.startNewActivity

class NotifyAdapter(private var notifies: List<Notification>): RecyclerView.Adapter<NotifyAdapter.ViewHolder>() {
    class ViewHolder (private val binding: ItemNotifyBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: Notification){
            binding.apply {
                timeStamp.text = item.createAt?.let { DateUtils.convertToVietnameseDate(it) }
                title.text = item.title
                content.text = item.content
                val contest = item.contest

                contest?.let {
                    notifyCardView.setOnClickListener {
                        if(item.type == ENotificationType.NEW_CONTEST)
                            it.context.startNewActivity(
                                ContestDetailsActivity::class.java,
                                mapOf(KEY_CONTEST to contest)
                            )
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
        holder.bind(notifies[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newNotifies: List<Notification>){
        notifies = newNotifies
        notifyDataSetChanged()
    }
}