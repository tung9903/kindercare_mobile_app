package com.example.myapplication.View.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DailySchedule
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.R
import com.google.android.material.card.MaterialCardView

class TimelinePHAdapter(
    private val list: List<DailySchedule>,
    private val onItemClick: (DailySchedule) -> Unit
) : RecyclerView.Adapter<TimelinePHAdapter.TimelineViewHolder>() {

    inner class TimelineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val viewDot: View = view.findViewById(R.id.viewDot)
        val viewLine: View = view.findViewById(R.id.viewLine)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvContent: TextView = view.findViewById(R.id.tvContent)
        val imgActivity: ImageView = view.findViewById(R.id.imgActivity)
        val card: MaterialCardView = view.findViewById(R.id.cardTimelineItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timeline_parent, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val item = list[position]
        
        holder.tvTime.text = DateHelper.formatLongToTime(item.startTime)
        holder.tvTitle.text = getIconForType(item.activityType) + " " + item.activityName
        holder.tvContent.text = item.details ?: "Hoạt động diễn ra bình thường"
        
        // Cập nhật màu sắc dựa trên loại hoạt động
        val colorCode = getColorForType(item.activityType)
        holder.viewDot.setBackgroundColor(Color.parseColor(colorCode))
        holder.tvTime.setTextColor(Color.parseColor(colorCode))
        
        if (item.activityType == "study" || item.activityType == "play") {
            holder.imgActivity.visibility = View.VISIBLE
            // holder.imgActivity.setImageResource(...)
        } else {
            holder.imgActivity.visibility = View.GONE
        }

        // Nếu là bữa ăn, đổi màu nền thẻ cho nổi bật
        if (item.activityType == "meal") {
            holder.card.setCardBackgroundColor(Color.parseColor("#FFF7ED")) // Màu cam nhạt
        } else if (item.activityType == "nap") {
            holder.card.setCardBackgroundColor(Color.parseColor("#EEF2FF")) // Màu tím nhạt
        } else {
            holder.card.setCardBackgroundColor(Color.WHITE)
        }

        // Ẩn dòng kẻ ở item cuối cùng
        holder.viewLine.visibility = if (position == list.size - 1) View.GONE else View.VISIBLE

        holder.card.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = list.size

    private fun getIconForType(type: String): String = when(type) {
        "pickup" -> "🏫"
        "meal" -> "🍲"
        "study" -> "🎨"
        "play" -> "⚽"
        "nap" -> "💤"
        "dropoff" -> "🏠"
        else -> "✨"
    }

    private fun getColorForType(type: String): String = when(type) {
        "pickup" -> "#10B981" // Green
        "meal" -> "#F97316"   // Orange
        "study", "play" -> "#3B82F6" // Blue
        "nap" -> "#8B5CF6"    // Purple
        "dropoff" -> "#F43F5E" // Red
        else -> "#64748B"
    }
}
