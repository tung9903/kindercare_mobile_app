package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.Model.LeaveRequestModel
import com.example.myapplication.R

class LeaveHistoryAdapter(
    private var leaveList: List<LeaveRequestModel>,
    private val onCancelClick: (Int) -> Unit
) : RecyclerView.Adapter<LeaveHistoryAdapter.LeaveViewHolder>() {

    class LeaveViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLeaveDateRange: TextView = view.findViewById(R.id.tvLeaveDateRange)
        val tvLeaveStatus: TextView = view.findViewById(R.id.tvLeaveStatus)
        val tvLeaveReason: TextView = view.findViewById(R.id.tvLeaveReason)
        val tvParentNotes: TextView = view.findViewById(R.id.tvParentNotes)
        val tvMealDeduction: TextView = view.findViewById(R.id.tvMealDeduction)
        val tvCreatedAt: TextView = view.findViewById(R.id.tvCreatedAt)
        val btnCancel: Button = view.findViewById(R.id.btnCancelRequest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaveViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leave_history, parent, false)
        return LeaveViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaveViewHolder, position: Int) {
        val request = leaveList[position]
        
        holder.tvLeaveDateRange.text = if (request.fromDate == request.toDate) {
            DateHelper.formatLongToDate(request.fromDate)
        } else {
            "${DateHelper.formatLongToDate(request.fromDate)} - ${DateHelper.formatLongToDate(request.toDate)}"
        }

        holder.tvLeaveReason.text = "Lý do: ${request.reason ?: "Chưa chọn lý do"}"
        holder.tvParentNotes.text = "Ghi chú: ${request.parentNotes ?: "Không có ghi chú thêm"}"
        holder.tvCreatedAt.text = "Ngày gửi: ${request.createdAt?.let { DateHelper.formatLongToDate(it) } ?: "N/A"}"

        holder.tvLeaveStatus.text = when (request.status) {
            "Pending" -> "Đang chờ"
            "Approved" -> "Đã duyệt"
            "Rejected" -> "Từ chối"
            "Cancelled" -> "Đã hủy"
            else -> request.status
        }

        val bgRes = when (request.status) {
            "Pending" -> R.drawable.bg_badge_gray
            "Approved" -> R.drawable.bg_badge_green
            "Rejected" -> R.drawable.bg_badge_red
            "Cancelled" -> R.drawable.bg_badge_gray
            else -> R.drawable.bg_badge_gray
        }
        holder.tvLeaveStatus.setBackgroundResource(bgRes)

        // Chỉ hiện nút Hủy nếu đơn đang ở trạng thái Pending
        if (request.status == "Pending") {
            holder.btnCancel.visibility = View.VISIBLE
            holder.btnCancel.setOnClickListener { onCancelClick(request.requestId) }
        } else {
            holder.btnCancel.visibility = View.GONE
        }

        if (request.isMealFeeDeducted) {
            holder.tvMealDeduction.visibility = View.VISIBLE
        } else {
            holder.tvMealDeduction.visibility = View.GONE
        }
    }

    override fun getItemCount() = leaveList.size

    fun updateData(newList: List<LeaveRequestModel>) {
        this.leaveList = newList
        notifyDataSetChanged()
    }
}
