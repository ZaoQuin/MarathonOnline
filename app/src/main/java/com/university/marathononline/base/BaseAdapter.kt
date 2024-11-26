package com.university.marathononline.base

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<T, VB : ViewBinding>(
    private val items: MutableList<T>
) : RecyclerView.Adapter<BaseAdapter.BaseViewHolder<VB>>() {

    abstract fun bind(binding: VB, item: T)

    abstract fun inflateBinding(layoutInflater: LayoutInflater, parent: ViewGroup): VB

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        val binding = inflateBinding(LayoutInflater.from(parent.context), parent)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        val item = items[position]
        bind(holder.binding, item)
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<T>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class BaseViewHolder<VB : ViewBinding>(val binding: VB) :
        RecyclerView.ViewHolder(binding.root)
}