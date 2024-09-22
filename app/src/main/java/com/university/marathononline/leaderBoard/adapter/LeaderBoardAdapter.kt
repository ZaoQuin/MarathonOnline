package com.university.marathononline.leaderBoard.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.databinding.ItemLeaderBoardBinding
import com.university.marathononline.entity.EventHistory
import com.university.marathononline.entity.User

class LeaderBoardAdapter (private val eventHistories: List<EventHistory>, private val users: List<User>) : RecyclerView.Adapter<LeaderBoardAdapter.ViewHolder>(){

    private val userMap = users.associateBy { it.id }

    class ViewHolder(private val binding: ItemLeaderBoardBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(distance: Double, fullName: String, position: Int){
            binding.position.text = (position + 4).toString()
            binding.fullName.text = fullName
            binding.distance.text = "${distance} km"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderBoardBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = eventHistories.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val eventHistory = eventHistories[position]
        val user = userMap[eventHistory.userId]
        val distance = eventHistory.raceResults.sumOf { it.distance.toDouble() } ?: 0.0

        holder.bind(distance, user?.fullName ?: "Unknown", position)
    }
}