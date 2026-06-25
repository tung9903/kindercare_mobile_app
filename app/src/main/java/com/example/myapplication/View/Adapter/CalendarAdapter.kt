package com.example.myapplication.View.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.Model.AttendanceDay

class CalendarAdapter(
    private val daysList: List<AttendanceDay>,
    private val onDayClick: (AttendanceDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayNumber: TextView = view.findViewById(R.id.tvDayNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = daysList[position]
        holder.tvDayNumber.text = day.dayNumber

        // Thay đổi background theo trạng thái điểm danh
        when (day.status) {
            1 -> { // Đi học
                holder.tvDayNumber.setBackgroundResource(R.drawable.bg_day_green)
                holder.tvDayNumber.setTextColor(Color.WHITE)
            }
            2 -> { // Vắng học
                holder.tvDayNumber.setBackgroundResource(R.drawable.bg_day_red)
                holder.tvDayNumber.setTextColor(Color.WHITE)
            }
            else -> { // Bình thường
                holder.tvDayNumber.setBackgroundResource(R.drawable.bg_day_normal)
                holder.tvDayNumber.setTextColor(Color.parseColor("#333333"))
            }
        }

        holder.itemView.setOnClickListener {
            onDayClick(day)
        }
    }

    override fun getItemCount(): Int = daysList.size
}
