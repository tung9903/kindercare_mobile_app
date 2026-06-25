package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.Student
import com.example.myapplication.R

class DiemDanhAdapter(
    private var studentList: List<Student>,
    private val onStatusChanged: () -> Unit
) : RecyclerView.Adapter<DiemDanhAdapter.DiemDanhViewHolder>() {

    private var fullStudentList: List<Student> = studentList
    var isReadOnly: Boolean = false

    fun filter(query: String) {
        studentList = if (query.isEmpty()) {
            fullStudentList
        } else {
            fullStudentList.filter { it.FullName.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    fun updateData(newList: List<Student>) {
        this.studentList = newList
        this.fullStudentList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiemDanhViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diem_danh, parent, false)
        return DiemDanhViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiemDanhViewHolder, position: Int) {
        val student = studentList[position]
        holder.tvIndex.text = "${position + 1}#"
        holder.tvName.text = student.FullName
        holder.ivAvatar.setImageResource(student.avatarResId)

        // Hiển thị nhãn "Có phép" nếu đơn nghỉ đã được duyệt
        if (student.attendanceStatus == "Nghỉ" && DataManager.isLeaveApproved(student.StudentID)) {
            holder.tvLeaveBadge.visibility = View.VISIBLE
        } else {
            holder.tvLeaveBadge.visibility = View.GONE
        }

        updateStatusUI(holder.tvStatusButton, student.attendanceStatus)

        holder.tvStatusButton.setOnClickListener {
            if (isReadOnly) {
                android.widget.Toast.makeText(holder.itemView.context, "Trạng thái đã khóa, không thể thay đổi", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            
            // Logic chốt chặn sau 9h
            if (hour >= 9) {
                 android.widget.Toast.makeText(holder.itemView.context, "Đã quá 09:00, hệ thống đã tự động chốt và khóa điểm danh", android.widget.Toast.LENGTH_SHORT).show()
                 return@setOnClickListener
            }

            if (hour < 8 && !DataManager.isLeaveApproved(student.StudentID)) {
                android.widget.Toast.makeText(holder.itemView.context, "Chưa đến giờ điểm danh (08:00 - 09:00)", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Chuyển đổi trạng thái: Chưa có mặt -> Hiện diện -> Nghỉ -> Hiện diện
            student.attendanceStatus = when (student.attendanceStatus) {
                "Chưa có mặt" -> "Hiện diện"
                "Hiện diện" -> "Nghỉ"
                "Nghỉ" -> "Hiện diện"
                else -> "Hiện diện"
            }

            // Cập nhật lại nhãn nghỉ phép
            if (student.attendanceStatus == "Nghỉ" && DataManager.isLeaveApproved(student.StudentID)) {
                holder.tvLeaveBadge.visibility = View.VISIBLE
            } else {
                holder.tvLeaveBadge.visibility = View.GONE
            }

            updateStatusUI(holder.tvStatusButton, student.attendanceStatus)
            onStatusChanged()
        }
    }

    private fun updateStatusUI(tvStatus: TextView, status: String) {
        tvStatus.text = status
        when (status) {
            "Hiện diện" -> {
                tvStatus.setBackgroundResource(R.drawable.bg_status_present)
                tvStatus.setTextColor(android.graphics.Color.WHITE)
            }
            "Nghỉ" -> {
                tvStatus.setBackgroundResource(R.drawable.bg_status_leave)
                tvStatus.setTextColor(android.graphics.Color.WHITE)
            }
            "Chưa có mặt" -> {
                tvStatus.setBackgroundResource(R.drawable.bg_status_unmarked)
                tvStatus.setTextColor(android.graphics.Color.parseColor("#64748B"))
            }
        }
    }

    override fun getItemCount(): Int = studentList.size

    class DiemDanhViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIndex: TextView = itemView.findViewById(R.id.tvStudentIndex)
        val ivAvatar: ImageView = itemView.findViewById(R.id.ivStudentAvatar)
        val tvName: TextView = itemView.findViewById(R.id.tvStudentName)
        val tvStatusButton: TextView = itemView.findViewById(R.id.tvStatusButton)
        val tvLeaveBadge: TextView = itemView.findViewById(R.id.tvLeaveBadge)
    }
}