package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.databinding.ItemRuleBinding
import com.university.marathononline.data.models.Rule

class RuleAdapter(private var rules: List<Rule>) : RecyclerView.Adapter<RuleAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemRuleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Rule) {
            binding.apply {
//                icon.setImageResource(item.icon)
                name.text = item.name
                description.text = item.description
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = rules.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(rules[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newRules: List<Rule>) {
        rules = newRules
        notifyDataSetChanged()
    }
}
