package com.example.myapplication.View.Adapter

import android.graphics.Color
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

class MedicationAdapter(
    private var list: List<MedicationRequestModel>,
    private val onAction: (MedicationRequestModel, String, String?) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.MedViewHolder>() {

    class NotiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) // Fixed copy-paste naming if needed, but using MedViewHolder

    inner class MedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgStudent: ShapeableImageView = view.findViewById(R.id.imgStudent)
        val tvStudentName: TextView = view.findViewById(R.id.tvStudentName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvMedName: TextView = view.findViewById(R.id.tvMedName)
        val tvDosage: TextView = view.findViewById(R.id.tvDosage)
        val tvViewEvidence: TextView = view.findViewById(R.id.tvViewEvidence)
        val viewIndicator: View = view.findViewById(R.id.viewStatusIndicator)
        
        val layoutPending: View = view.findViewById(R.id.layoutPendingActions)
        val btnAccept: Button = view.findViewById(R.id.btnAccept)
        val btnReject: Button = view.findViewById(R.id.btnReject)
        
        val layoutCompletion: View = view.findViewById(R.id.layoutCompletionArea)
        val edtNote: EditText = view.findViewById(R.id.edtNote)
        val btnConfirmDone: Button = view.findViewById(R.id.btnConfirmDone)
        
        val tvCompletedNote: TextView = view.findViewById(R.id.tvCompletedNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medication, parent, false)
        return MedViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedViewHolder, position: Int) {
        val item = list[position]
        
        holder.tvStudentName.text = item.studentName
        holder.imgStudent.setImageResource(item.avatarResId)
        holder.tvMedName.text = "Tên thuốc: ${item.MedicineDetails}"
        holder.tvDosage.text = "Giờ uống: ${item.Dosage}"

        // Reset visibility
        holder.layoutPending.visibility = View.GONE
        holder.layoutCompletion.visibility = View.GONE
        holder.tvCompletedNote.visibility = View.GONE
        holder.viewIndicator.visibility = View.GONE

        when (item.Status) {
            "Pending" -> {
                holder.tvStatus.text = "● Chờ tiếp nhận"
                holder.tvStatus.setTextColor(Color.parseColor("#D97706"))
                holder.layoutPending.visibility = View.VISIBLE
            }
            "Accepted" -> {
                holder.tvStatus.text = "● Đến giờ uống thuốc"
                holder.tvStatus.setTextColor(Color.parseColor("#2563EB"))
                holder.layoutCompletion.visibility = View.VISIBLE
                holder.viewIndicator.visibility = View.VISIBLE
            }
            "Completed" -> {
                holder.tvStatus.text = "● Đã hoàn thành"
                holder.tvStatus.setTextColor(Color.parseColor("#059669"))
                holder.tvCompletedNote.visibility = View.VISIBLE
                holder.tvCompletedNote.text = "Ghi chú: ${item.TeacherNote ?: "Đã cho bé uống thuốc"}"
            }
            "Rejected" -> {
                holder.tvStatus.text = "● Đã từ chối"
                holder.tvStatus.setTextColor(Color.RED)
            }
        }

        holder.btnAccept.setOnClickListener { onAction(item, "ACCEPT", null) }
        holder.btnReject.setOnClickListener { onAction(item, "REJECT", null) }
        holder.btnConfirmDone.setOnClickListener { 
            val note = holder.edtNote.text.toString()
            onAction(item, "COMPLETE", note) 
        }
        
        holder.tvViewEvidence.setOnClickListener {
            onAction(item, "VIEW_IMAGE", null)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<MedicationRequestModel>) {
        this.list = newList
        notifyDataSetChanged()
    }
}
