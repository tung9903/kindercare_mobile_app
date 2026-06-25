package com.example.myapplication.View.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.LeaveRequestModel
import com.example.myapplication.R
import com.google.android.material.imageview.ShapeableImageView

class DuyetDonAdapter(
    private var listRequests: List<LeaveRequestModel>,
    private val onAction: (LeaveRequestModel, String) -> Unit,
    private val onItemClick: (LeaveRequestModel) -> Unit
) : RecyclerView.Adapter<DuyetDonAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ShapeableImageView = itemView.findViewById(R.id.img_student_avatar)
        val tvParent: TextView = itemView.findViewById(R.id.txt_parent_name)
        val tvStudent: TextView = itemView.findViewById(R.id.txt_student_name)
        val tvClass: TextView = itemView.findViewById(R.id.txt_class_name)
        val tvDuration: TextView = itemView.findViewById(R.id.txt_leave_duration)
        val tvReason: TextView = itemView.findViewById(R.id.txt_leave_reason)
        val tvStatus: TextView = itemView.findViewById(R.id.txt_status_label)
        val btnApprove: AppCompatButton = itemView.findViewById(R.id.btn_approve)
        val btnReject: AppCompatButton = itemView.findViewById(R.id.btn_reject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leave_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listRequests[position]

        holder.ivAvatar.setImageResource(item.avatarResId)
        holder.tvParent.text = "PH: ${item.parentName}"
        holder.tvStudent.text = item.studentName
        holder.tvClass.text = item.className ?: ""
        holder.tvDuration.text = item.getDurationText()
        holder.tvReason.text = "Lý do: ${item.Reason}"

        when (item.Status) {
            "Pending" -> {
                holder.tvStatus.text = "Chờ duyệt"
                holder.tvStatus.setTextColor(Color.parseColor("#E65100"))
                holder.btnApprove.visibility = View.VISIBLE
                holder.btnReject.visibility = View.VISIBLE
            }
            "Approved" -> {
                holder.tvStatus.text = "Đã duyệt"
                holder.tvStatus.setTextColor(Color.parseColor("#027A48"))
                holder.btnApprove.visibility = View.GONE
                holder.btnReject.visibility = View.GONE
            }
            "Rejected" -> {
                holder.tvStatus.text = "Từ chối"
                holder.tvStatus.setTextColor(Color.parseColor("#D92D20"))
                holder.btnApprove.visibility = View.GONE
                holder.btnReject.visibility = View.GONE
            }
        }

        holder.btnApprove.setOnClickListener { onAction(item, "APPROVE") }
        holder.btnReject.setOnClickListener { onAction(item, "REJECT") }
        
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = listRequests.size

    fun updateData(newList: List<LeaveRequestModel>) {
        this.listRequests = newList
        notifyDataSetChanged()
    }
}
