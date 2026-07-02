package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.TeacherStudentResponse
import com.example.myapplication.R

class StudentActivityLogAdapter(
    private var studentList: List<TeacherStudentResponse>,
    private val onStatusClick: (Int, String) -> Unit
) : RecyclerView.Adapter<StudentActivityLogAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvStudentName)
        val tvBreakfast: TextView = view.findViewById(R.id.tvBreakfast)
        val tvLunch: TextView = view.findViewById(R.id.tvLunch)
        val tvSnack: TextView = view.findViewById(R.id.tvSnack)
        val tvNap: TextView = view.findViewById(R.id.tvNap)
        val tvHygiene: TextView = view.findViewById(R.id.tvHygiene)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_activity_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = studentList[position]
        holder.tvName.text = student.fullName
        
        // Mock statuses - In real app, these come from DailyActivity model
        holder.tvBreakfast.text = "Ăn hết"
        holder.tvLunch.text = "Ăn hết"
        holder.tvSnack.text = "Ăn hết"
        holder.tvNap.text = "Ngoan"
        holder.tvHygiene.text = "Tốt"

        // Set click listeners for each box to cycle statuses
        holder.tvBreakfast.setOnClickListener { onStatusClick(student.studentId, "BREAKFAST") }
        holder.tvLunch.setOnClickListener { onStatusClick(student.studentId, "LUNCH") }
        holder.tvSnack.setOnClickListener { onStatusClick(student.studentId, "SNACK") }
        holder.tvNap.setOnClickListener { onStatusClick(student.studentId, "NAP") }
        holder.tvHygiene.setOnClickListener { onStatusClick(student.studentId, "HYGIENE") }
    }

    override fun getItemCount() = studentList.size

    fun updateData(newList: List<TeacherStudentResponse>) {
        this.studentList = newList
        notifyDataSetChanged()
    }
}
