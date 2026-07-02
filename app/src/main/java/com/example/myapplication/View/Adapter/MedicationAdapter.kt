package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.MedicationRequestModel
import com.example.myapplication.R
import com.google.android.material.imageview.ShapeableImageView
import com.bumptech.glide.Glide

class MedicationAdapter(
    private var list: List<MedicationRequestModel>,
    private val onAction: (MedicationRequestModel, String, String?) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.MedViewHolder>() {

    class MedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgStudent: ShapeableImageView = itemView.findViewById(R.id.imgStudent)
        val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvMedName: TextView = itemView.findViewById(R.id.tvMedName)
        val tvDosage: TextView = itemView.findViewById(R.id.tvDosage)
        val tvFrequency: TextView = itemView.findViewById(R.id.tvFrequency)
        val tvParentNote: TextView = itemView.findViewById(R.id.tvParentNote)
        val tvViewEvidence: TextView = itemView.findViewById(R.id.tvViewEvidence)
        val viewIndicator: View = itemView.findViewById(R.id.viewStatusIndicator)
        
        val layoutPending: View = itemView.findViewById(R.id.layoutPendingActions)
        val btnAccept: Button = itemView.findViewById(R.id.btnAccept)
        val btnReject: Button = itemView.findViewById(R.id.btnReject)
        
        val layoutCompletion: View = itemView.findViewById(R.id.layoutCompletionArea)
        val edtNote: EditText = itemView.findViewById(R.id.edtNote)
        val btnConfirmDone: Button = itemView.findViewById(R.id.btnConfirmDone)
        
        val tvCompletedNote: TextView = itemView.findViewById(R.id.tvCompletedNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medication, parent, false)
        return MedViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedViewHolder, position: Int) {
        val item = list[position]
        
        holder.tvStudentName.text = item.studentName
        holder.tvMedName.text = "Tên thuốc: ${item.medicineDetails}"
        holder.tvDosage.text = "Thời điểm: ${item.timeToTake ?: "Theo dặn dò"} (${item.dosage})"
        holder.tvFrequency.text = "Tần suất: ${item.frequency ?: "N/A"}"

        Glide.with(holder.itemView.context)
            .load(item.studentAvatar)
            .placeholder(R.drawable.avatar)
            .into(holder.imgStudent)

        // Hiển thị nút xem ảnh nếu có
        holder.tvViewEvidence.visibility = if (!item.medicineImageUrl.isNullOrEmpty()) View.VISIBLE else View.GONE
        
        // Hiển thị ghi chú phụ huynh
        if (!item.parentNote.isNullOrEmpty()) {
            holder.tvParentNote.visibility = View.VISIBLE
            holder.tvParentNote.text = "Ghi chú PH: ${item.parentNote}"
        } else {
            holder.tvParentNote.visibility = View.GONE
        }
        
        // Cấu hình UI theo trạng thái
        when (item.status) {
            "Pending" -> {
                holder.tvStatus.text = "● Chờ tiếp nhận"
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#D97706"))
                holder.layoutPending.visibility = View.VISIBLE
                holder.layoutCompletion.visibility = View.GONE
                holder.tvCompletedNote.visibility = View.GONE
            }
            "Accepted" -> {
                holder.tvStatus.text = "● Đã tiếp nhận"
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#10B981"))
                holder.layoutPending.visibility = View.GONE
                holder.layoutCompletion.visibility = View.VISIBLE
                holder.tvCompletedNote.visibility = View.GONE
            }
            "Completed" -> {
                holder.tvStatus.text = "● Đã cho uống thuốc"
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#059669"))
                holder.layoutPending.visibility = View.GONE
                holder.layoutCompletion.visibility = View.GONE
                holder.tvCompletedNote.visibility = View.VISIBLE
                holder.tvCompletedNote.text = "Ghi chú GV: ${item.teacherNote ?: "Đã cho bé uống thuốc"}"
            }
            "Rejected" -> {
                holder.tvStatus.text = "● Đã từ chối"
                holder.tvStatus.setTextColor(android.graphics.Color.RED)
                holder.layoutPending.visibility = View.GONE
                holder.layoutCompletion.visibility = View.GONE
                holder.tvCompletedNote.visibility = View.GONE
            }
        }

        holder.btnAccept.setOnClickListener { onAction(item, "ACCEPT", null) }
        holder.btnReject.setOnClickListener { onAction(item, "REJECT", null) }
        holder.btnConfirmDone.setOnClickListener { 
            val note = holder.edtNote.text.toString()
            onAction(item, "COMPLETE", note) 
        }
        holder.tvViewEvidence.setOnClickListener { onAction(item, "VIEW_IMAGE", null) }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<MedicationRequestModel>) {
        this.list = newList
        notifyDataSetChanged()
    }
}
