package com.university.marathononline.notify.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.databinding.ItemNotifyBinding
import com.university.marathononline.entity.Notify

class NotifyAdapter(private var notifies: List<Notify>): RecyclerView.Adapter<NotifyAdapter.ViewHolder>() {
    class ViewHolder (private val binding: ItemNotifyBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: Notify){
            binding.timeStamp.text = item.timeStamp.toString()
            binding.title.text = item.title
            binding.title.text = item.content
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
    fun updateData(newNotifies: List<Notify>){
        notifies = newNotifies
        notifyDataSetChanged()
    }
}