package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.TeacherClassResponse
import com.example.myapplication.R

class TeacherClassAdapter(
    private var classList: List<TeacherClassResponse>,
    private val onClassClick: (TeacherClassResponse) -> Unit
) : RecyclerView.Adapter<TeacherClassAdapter.ClassViewHolder>() {

    private var filteredList: List<TeacherClassResponse> = classList

    class ClassViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvClassName: TextView = view.findViewById(R.id.tvClassName)
        val tvStudentCount: TextView = view.findViewById(R.id.tvStudentCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teacher_class, parent, false)
        return ClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val item = filteredList[position]
        holder.tvClassName.text = item.className
        holder.tvStudentCount.text = "Sĩ số: ${item.studentCount} học sinh"
        
        holder.itemView.setOnClickListener {
            onClassClick(item)
        }
    }

    override fun getItemCount() = filteredList.size

    fun updateData(newList: List<TeacherClassResponse>) {
        classList = newList
        filteredList = newList
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            classList
        } else {
            classList.filter { it.className.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }
}
