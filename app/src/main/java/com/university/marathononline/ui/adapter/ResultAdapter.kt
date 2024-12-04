package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.databinding.ItemResultBinding
import com.university.marathononline.data.models.Contest
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.startNewActivity

class ResultAdapter (private var results: List<Contest>): RecyclerView.Adapter<ResultAdapter.ViewHolder>(){
    class ViewHolder(private val binding: ItemResultBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: Contest){
            binding.name.text = item.name
            binding.organizerName.text = item.organizer?.fullName

            binding.showDetailsBtn.setOnClickListener {
                it.context.startNewActivity(ContestDetailsActivity::class.java,
                    mapOf( KEY_CONTEST to item)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = results.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(results[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newResults: List<Contest>) {
        results = newResults
        notifyDataSetChanged()
    }
}