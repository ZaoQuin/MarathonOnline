package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.databinding.ItemLeaderBoardBinding
import com.university.marathononline.data.models.Registration
import com.university.marathononline.utils.formatDistance

class LeaderBoardAdapter (private val registrations: List<Registration>) : RecyclerView.Adapter<LeaderBoardAdapter.ViewHolder>(){

    class ViewHolder(private val binding: ItemLeaderBoardBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(distance: Double, fullName: String, position: Int){
            binding.position.text = (position + 4).toString()
            binding.fullName.text = fullName
            binding.distance.text = formatDistance(distance)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderBoardBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = registrations.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reg = registrations?.get(position)  // Kiểm tra null
        if (reg != null) {
            val user = reg.runner
            val distance = reg.records?.sumOf { it.distance } ?: 0.0

            holder.bind(distance, user.fullName, position)
        } else {
            // Xử lý khi `reg` là null (nếu cần thiết, ví dụ: ghi log hoặc thông báo người dùng)
            Log.e("LeaderBoardAdapter", "Registration at position $position is null")
        }
    }
}