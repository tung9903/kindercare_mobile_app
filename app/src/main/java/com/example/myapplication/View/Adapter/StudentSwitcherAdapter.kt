package com.example.myapplication.View.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.google.android.material.imageview.ShapeableImageView
import org.json.JSONObject

class StudentSwitcherAdapter(
    private val studentList: List<JSONObject>,
    private var selectedId: Int,
    private val onStudentSelected: (JSONObject) -> Unit
) : RecyclerView.Adapter<StudentSwitcherAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAvatar: ShapeableImageView = view.findViewById(R.id.imgStudentAvatar)
        val tvName: TextView = view.findViewById(R.id.tvStudentFirstName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_switcher, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = studentList[position]
        val studentId = student.optInt("studentId")
        val fullName = student.optString("fullName")
        val firstName = fullName.split(" ").lastOrNull() ?: fullName

        holder.tvName.text = firstName
        
        val avatarUrl = student.optString("avatarUrl")
        if (avatarUrl.isNotEmpty() && avatarUrl != "null") {
            Glide.with(holder.itemView.context)
                .load(avatarUrl)
                .placeholder(R.drawable.avatar)
                .into(holder.imgAvatar)
        } else {
            holder.imgAvatar.setImageResource(R.drawable.avatar)
        }

        // Highlight if selected
        if (studentId == selectedId) {
            holder.imgAvatar.strokeWidth = 6f
            holder.tvName.setTextColor(Color.parseColor("#005A36"))
        } else {
            holder.imgAvatar.strokeWidth = 0f
            holder.tvName.setTextColor(Color.parseColor("#64748B"))
        }

        holder.itemView.setOnClickListener {
            selectedId = studentId
            notifyDataSetChanged()
            onStudentSelected(student)
        }
    }

    override fun getItemCount() = studentList.size
    
    fun updateSelectedId(newId: Int) {
        this.selectedId = newId
        notifyDataSetChanged()
    }
}
