package com.example.myapplication.View.Adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.Model.LeaveRequestModel
import com.example.myapplication.R
import com.google.android.material.button.MaterialButton
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
        val tvDuration: TextView = itemView.findViewById(R.id.txt_leave_duration)
        val tvReason: TextView = itemView.findViewById(R.id.txt_leave_reason)
        val tvStatus: TextView = itemView.findViewById(R.id.txt_status_label)
        val btnApprove: MaterialButton = itemView.findViewById(R.id.btn_approve)
        val btnReject: MaterialButton = itemView.findViewById(R.id.btn_reject)
        
        val tvRefundBadge: TextView = itemView.findViewById(R.id.txt_refund_badge)
        val tvParentNotes: TextView = itemView.findViewById(R.id.txt_parent_notes)
        val layoutActions: View = itemView.findViewById(R.id.layout_actions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leave_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listRequests[position]

        if (!item.studentAvatar.isNullOrEmpty() && item.studentAvatar != "null") {
            Glide.with(holder.itemView.context)
                .load(item.studentAvatar)
                .placeholder(R.drawable.avatar)
                .into(holder.ivAvatar)
        } else {
            holder.ivAvatar.setImageResource(R.drawable.avatar)
        }

        holder.tvStudent.text = item.studentName
        holder.tvParent.text = "Phụ huynh: ${item.parentName}"
        holder.tvDuration.text = item.getDurationText()
        holder.tvReason.text = item.reason

        holder.tvRefundBadge.visibility = if (item.isMealFeeDeducted) View.VISIBLE else View.GONE

        if (!item.parentNotes.isNullOrEmpty()) {
            holder.tvParentNotes.visibility = View.VISIBLE
            holder.tvParentNotes.text = "Ghi chú: ${item.parentNotes}"
        } else {
            holder.tvParentNotes.visibility = View.GONE
        }

        // UI State based on Status
        when (item.status) {
            "Pending" -> {
                holder.tvStatus.text = "ĐANG CHỜ"
                holder.tvStatus.setTextColor(Color.parseColor("#B45309"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_rounded_orange)
                holder.layoutActions.visibility = View.VISIBLE
            }
            "Approved" -> {
                holder.tvStatus.text = "ĐÃ DUYỆT"
                holder.tvStatus.setTextColor(Color.parseColor("#059669"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_light_green)
                holder.layoutActions.visibility = View.GONE
            }
            "Rejected" -> {
                holder.tvStatus.text = "TỪ CHỐI"
                holder.tvStatus.setTextColor(Color.parseColor("#DC2626"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_alert_red)
                holder.layoutActions.visibility = View.GONE
            }
        }

        // Button Click Listeners with logs for debugging
        holder.btnApprove.setOnClickListener { 
            Log.d("LEAVE_ADAPTER", "Approve clicked for ${item.studentName}")
            onAction(item, "APPROVE") 
        }
        
        holder.btnReject.setOnClickListener { 
            Log.d("LEAVE_ADAPTER", "Reject clicked for ${item.studentName}")
            onAction(item, "REJECT") 
        }
        
        holder.itemView.setOnClickListener { 
            Log.d("LEAVE_ADAPTER", "Item clicked for ${item.studentName}")
            onItemClick(item) 
        }
    }

    override fun getItemCount(): Int = listRequests.size

    fun updateData(newList: List<LeaveRequestModel>) {
        this.listRequests = newList
        notifyDataSetChanged()
    }
}
