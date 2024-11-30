package com.example.emis.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.emis.databinding.ItemSlideButtonBinding

class ViewPagerAdapter(private val items: List<String>) : RecyclerView.Adapter<ViewPagerAdapter.SlideViewHolder>() {

    inner class SlideViewHolder(val binding: ItemSlideButtonBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        val binding = ItemSlideButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SlideViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
        val item = items[position]
        holder.binding.button.text = item
    }

    override fun getItemCount() = items.size
}
