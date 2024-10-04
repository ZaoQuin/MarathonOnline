package com.university.marathononline.search.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.contest.contestDetails.ContestDetailsActivity
import com.university.marathononline.databinding.ItemResultBinding
import com.university.marathononline.entity.Contest

class ResultAdapter (private var results: List<Contest>): RecyclerView.Adapter<ResultAdapter.ViewHolder>(){
    class ViewHolder(private val binding: ItemResultBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: Contest){
            binding.name.text = item.title
            binding.organizerName.text = "Tên tổ chức"

            binding.showDetailsBtn.setOnClickListener {
                val intent = Intent(binding.root.context, ContestDetailsActivity::class.java)
                binding.root.context.startActivity(intent)
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