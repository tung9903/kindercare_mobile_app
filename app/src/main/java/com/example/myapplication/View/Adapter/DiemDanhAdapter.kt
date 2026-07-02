package com.example.myapplication.View.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.Model.TeacherStudentResponse
import com.example.myapplication.R
import com.example.myapplication.View.GiaoVien.ManHinhChucNangHoSoHocSinh

class DiemDanhAdapter(
    private var studentList: List<TeacherStudentResponse>,
    private val onStatusChanged: () -> Unit
) : RecyclerView.Adapter<DiemDanhAdapter.DiemDanhViewHolder>() {

    private var fullStudentList: List<TeacherStudentResponse> = studentList
    var isReadOnly: Boolean = false

    fun filter(query: String) {
        studentList = if (query.isEmpty()) {
            fullStudentList
        } else {
            fullStudentList.filter { it.fullName.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    fun updateData(newList: List<TeacherStudentResponse>) {
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
        holder.tvIndex.text = String.format("%02d", position + 1)
        holder.tvName.text = student.fullName
        holder.ivAvatar.setImageResource(R.drawable.avatar)

        if (student.leaveRequest != null && student.leaveRequest.status == "Approved") {
            holder.tvLeaveBadge.visibility = View.VISIBLE
            holder.tvLeaveBadge.text = "Có đơn nghỉ: ${student.leaveRequest.reason ?: "Nghỉ phép"}"
        } else {
            holder.tvLeaveBadge.visibility = View.GONE
        }

        if (!student.healthNote.isNullOrEmpty()) {
            holder.tvHealthNote.visibility = View.VISIBLE
            holder.tvHealthNote.text = "⚠️ Sức khỏe: ${student.healthNote}"
        } else {
            holder.tvHealthNote.visibility = View.GONE
        }

        val checkIn = student.checkInTime
        if (checkIn != null) {
            holder.tvAttendanceTime.visibility = View.VISIBLE
            val inTime = DateHelper.formatLongToTime(checkIn)
            val outTime = student.checkOutTime?.let { DateHelper.formatLongToTime(it) } ?: "--:--"
            holder.tvAttendanceTime.text = "Vào: $inTime | Ra: $outTime"
        } else {
            holder.tvAttendanceTime.visibility = View.GONE
        }

        val displayStatus = student.status ?: "Chưa có mặt"
        updateStatusUI(holder.tvStatusButton, displayStatus)

        holder.tvStatusButton.setOnClickListener {
            if (isReadOnly) return@setOnClickListener
            student.status = when (student.status) {
                "Present" -> "Absent"
                "Absent" -> "Present"
                else -> "Present"
            }
            updateStatusUI(holder.tvStatusButton, student.status!!)
            onStatusChanged()
        }
        
        holder.ivAvatar.setOnClickListener {
            val intent = Intent(holder.itemView.context, ManHinhChucNangHoSoHocSinh::class.java)
            intent.putExtra("STUDENT_ID", student.studentId)
            holder.itemView.context.startActivity(intent)
        }
    }

    private fun updateStatusUI(tvStatus: TextView, status: String) {
        when (status) {
            "Present" -> {
                tvStatus.text = "Hiện diện"
                tvStatus.setBackgroundResource(R.drawable.bg_status_present)
                tvStatus.setTextColor(android.graphics.Color.WHITE)
            }
            "Absent" -> {
                tvStatus.text = "Nghỉ"
                tvStatus.setBackgroundResource(R.drawable.bg_status_leave)
                tvStatus.setTextColor(android.graphics.Color.WHITE)
            }
            else -> {
                tvStatus.text = "Chưa có mặt"
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
        val tvHealthNote: TextView = itemView.findViewById(R.id.tvHealthNote)
        val tvAttendanceTime: TextView = itemView.findViewById(R.id.tvAttendanceTime)
    }
}
