package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.university.marathononline.R
import com.university.marathononline.ui.view.activity.ContestDetailsActivity
import com.university.marathononline.databinding.ItemResultBinding
import com.university.marathononline.data.models.Contest
import com.university.marathononline.utils.KEY_CONTEST
import com.university.marathononline.utils.startNewActivity

class ResultAdapter (private var results: List<Contest>): RecyclerView.Adapter<ResultAdapter.ViewHolder>(){
    class ViewHolder(private val binding: ItemResultBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: Contest){
            binding.apply {
                name.text = item.name
                organizerName.text = item.organizer?.fullName

                if (item.imgUrl.isNullOrEmpty()) {
                    eventImage.setImageResource(R.drawable.example_event)
                } else {
                    Glide.with(root.context)
                        .load(item.imgUrl)
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.example_event)
                        .into(eventImage)
                }

                showDetailsBtn.setOnClickListener {
                    it.context.startNewActivity(ContestDetailsActivity::class.java,
                        mapOf( KEY_CONTEST to item)
                    )
                }
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