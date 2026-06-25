package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.Model.UpcomingEvent

class EventAdapter(private val eventList: List<UpcomingEvent>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMonth: TextView = view.findViewById(R.id.tvEventMonth)
        val tvDay: TextView = view.findViewById(R.id.tvEventDay)
        val tvTitle: TextView = view.findViewById(R.id.tvEventTitle)
        val tvTime: TextView = view.findViewById(R.id.tvEventTime)
        val tvLocation: TextView = view.findViewById(R.id.tvEventLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_upcoming_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.tvMonth.text = "THÁNG ${event.month}"
        holder.tvDay.text = event.day
        holder.tvTitle.text = event.title
        holder.tvTime.text = "🕒 ${event.time}"
        holder.tvLocation.text = "📍 ${event.location}"
    }

    override fun getItemCount(): Int = eventList.size
}