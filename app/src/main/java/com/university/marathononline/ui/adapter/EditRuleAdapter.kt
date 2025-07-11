package com.university.marathononline.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.marathononline.databinding.ItemEditRuleBinding
import com.university.marathononline.data.models.Rule

class EditRuleAdapter(
    private var rules: List<Rule>,
    private val onEditClick: (Rule) -> Unit,
    private val onDeleteClick: (Rule) -> Unit
) : RecyclerView.Adapter<EditRuleAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemEditRuleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(rule: Rule, onEditClick: (Rule) -> Unit, onDeleteClick: (Rule) -> Unit) {
            binding.apply {
                tvName.text = rule.name
                btnEdit.setOnClickListener { onEditClick(rule) }
                btnDelete.setOnClickListener { onDeleteClick(rule) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = rules.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(rules[position], onEditClick, onDeleteClick)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newRules: List<Rule>) {
        rules = newRules
        notifyDataSetChanged()
    }

    fun getCurrentData(): List<Rule> {
        return rules
    }
}
