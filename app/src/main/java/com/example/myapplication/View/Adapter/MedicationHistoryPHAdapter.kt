package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.Model.MedicationRequestModel
import com.example.myapplication.R

class MedicationHistoryPHAdapter(private val list: List<MedicationRequestModel>) :
    RecyclerView.Adapter<MedicationHistoryPHAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvMedRequestDate)
        val tvStatus: TextView = view.findViewById(R.id.tvMedStatus)
        val tvMedDetails: TextView = view.findViewById(R.id.tvMedicineDetails)
        val tvDosage: TextView = view.findViewById(R.id.tvDosage)
        val tvTeacherNote: TextView = view.findViewById(R.id.tvTeacherNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medication_history_ph, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = list[position]
        
        holder.tvDate.text = "Ngày: ${DateHelper.formatLongToDate(item.RequestDate)}"
        holder.tvMedDetails.text = "Thuốc: ${item.MedicineDetails}"
        holder.tvDosage.text = "Liều dùng: ${item.Dosage}"
        
        holder.tvStatus.text = when(item.Status) {
            "Pending" -> "Đang chờ"
            "Completed" -> "Đã uống"
            "Rejected" -> "Từ chối"
            else -> item.Status
        }

        val bgRes = when(item.Status) {
            "Pending" -> R.drawable.bg_badge_gray
            "Completed" -> R.drawable.bg_badge_green
            "Rejected" -> R.drawable.bg_badge_red
            else -> R.drawable.bg_badge_gray
        }
        holder.tvStatus.setBackgroundResource(bgRes)

        if (!item.TeacherNote.isNullOrEmpty()) {
            holder.tvTeacherNote.visibility = View.VISIBLE
            holder.tvTeacherNote.text = "Phản hồi GV: ${item.TeacherNote}"
        } else {
            holder.tvTeacherNote.visibility = View.GONE
        }
    }

    override fun getItemCount() = list.size
}
