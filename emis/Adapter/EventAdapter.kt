package com.example.emis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(private val eventList: List<Event>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_event_appointment, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.title.text = event.title
        holder.date.text = event.date
        holder.description.text = event.description
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.editTextTitle)
        val date: TextView = itemView.findViewById(R.id.editTextDate)
        val description: TextView = itemView.findViewById(R.id.editTextDescription)
    }
}
