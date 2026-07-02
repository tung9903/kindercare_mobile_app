package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DailySchedule
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.R

class SchedulePHAdapter(private val list: List<DailySchedule>) :
    RecyclerView.Adapter<SchedulePHAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tvScheduleTime)
        val tvName: TextView = view.findViewById(R.id.tvActivityName)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val viewIndicator: View = view.findViewById(R.id.viewTypeIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily_schedule_ph, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val item = list[position]
        holder.tvTime.text = "${DateHelper.formatLongToTime(item.startTime)} - ${DateHelper.formatLongToTime(item.endTime)}"
        holder.tvName.text = item.activityName
        holder.tvLocation.text = item.location ?: "Phòng học"
        holder.tvStatus.text = item.status

        // Change color based on activity type
        val color = when(item.activityType) {
            "pickup", "dropoff" -> android.graphics.Color.parseColor("#10B981") // Green
            "meal" -> android.graphics.Color.parseColor("#F59E0B") // Orange
            "study" -> android.graphics.Color.parseColor("#3B82F6") // Blue
            "play" -> android.graphics.Color.parseColor("#8B5CF6") // Purple
            "nap" -> android.graphics.Color.parseColor("#64748B") // Gray
            else -> android.graphics.Color.parseColor("#94A3B8")
        }
        holder.viewIndicator.setBackgroundColor(color)
    }

    override fun getItemCount() = list.size
}
