package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DailyLesson
import com.example.myapplication.R

class LessonPHAdapter(private val list: List<DailyLesson>) :
    RecyclerView.Adapter<LessonPHAdapter.LessonViewHolder>() {

    class LessonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcon: TextView = view.findViewById(R.id.tvLessonIcon)
        val tvSubject: TextView = view.findViewById(R.id.tvSubjectName)
        val tvTitle: TextView = view.findViewById(R.id.tvLessonTitle)
        val tvDetails: TextView = view.findViewById(R.id.tvLessonDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily_lesson_ph, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val item = list[position]
        holder.tvSubject.text = item.subjectName.uppercase()
        holder.tvTitle.text = item.lessonTitle
        holder.tvDetails.text = item.details

        // Dynamic icon based on iconType from Swagger
        holder.tvIcon.text = when(item.iconType?.lowercase()) {
            "draw" -> "🎨"
            "music" -> "🎵"
            "math" -> "🔢"
            "english" -> "🅰️"
            "physical" -> "⚽"
            else -> "📚"
        }
    }

    override fun getItemCount() = list.size
}
