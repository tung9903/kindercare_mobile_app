package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.Model.LeaveRequestModel
import com.example.myapplication.R

class LeaveHistoryAdapter(private val leaveList: List<LeaveRequestModel>) :
    RecyclerView.Adapter<LeaveHistoryAdapter.LeaveViewHolder>() {

    class LeaveViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLeaveDateRange: TextView = view.findViewById(R.id.tvLeaveDateRange)
        val tvLeaveStatus: TextView = view.findViewById(R.id.tvLeaveStatus)
        val tvLeaveReason: TextView = view.findViewById(R.id.tvLeaveReason)
        val tvParentNotes: TextView = view.findViewById(R.id.tvParentNotes)
        val tvMealDeduction: TextView = view.findViewById(R.id.tvMealDeduction)
        val tvCreatedAt: TextView = view.findViewById(R.id.tvCreatedAt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaveViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leave_history, parent, false)
        return LeaveViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaveViewHolder, position: Int) {
        val request = leaveList[position]
        
        holder.tvLeaveDateRange.text = if (request.FromDate == request.ToDate) {
            DateHelper.formatLongToDate(request.FromDate)
        } else {
            "${DateHelper.formatLongToDate(request.FromDate)} - ${DateHelper.formatLongToDate(request.ToDate)}"
        }

        holder.tvLeaveReason.text = "Lý do: ${request.reasonCategory.ifEmpty { "Chưa chọn lý do" }}"
        holder.tvParentNotes.text = "Ghi chú: ${request.ParentNotes ?: "Không có ghi chú thêm"}"
        holder.tvCreatedAt.text = "Ngày gửi: ${request.CreatedAt?.let { DateHelper.formatLongToDate(it) } ?: "N/A"}"

        holder.tvLeaveStatus.text = when (request.Status) {
            "Pending" -> "Đang chờ"
            "Approved" -> "Đã duyệt"
            "Rejected" -> "Từ chối"
            else -> request.Status
        }

        val bgRes = when (request.Status) {
            "Pending" -> R.drawable.bg_badge_gray
            "Approved" -> R.drawable.bg_badge_green
            "Rejected" -> R.drawable.bg_badge_red
            else -> R.drawable.bg_badge_gray
        }
        holder.tvLeaveStatus.setBackgroundResource(bgRes)

        // Hiển thị trạng thái hoàn tiền ăn nếu có
        if (request.IsMealFeeDeducted) {
            holder.tvMealDeduction.visibility = View.VISIBLE
        } else {
            holder.tvMealDeduction.visibility = View.GONE
        }
    }

    override fun getItemCount() = leaveList.size
}
