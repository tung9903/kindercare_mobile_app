package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.Model.MedicationRequestModel
import com.example.myapplication.R
import com.bumptech.glide.Glide

class MedicationHistoryPHAdapter(
    private var list: List<MedicationRequestModel>,
    private val onCancelClick: (Int) -> Unit
) : RecyclerView.Adapter<MedicationHistoryPHAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvMedRequestDate)
        val tvStatus: TextView = view.findViewById(R.id.tvMedStatus)
        val tvMedDetails: TextView = view.findViewById(R.id.tvMedicineDetails)
        val tvDosage: TextView = view.findViewById(R.id.tvDosage)
        val tvFrequencyAndTime: TextView = view.findViewById(R.id.tvFrequencyAndTime)
        val tvParentNote: TextView = view.findViewById(R.id.tvParentNote)
        val imgMedicine: ImageView = view.findViewById(R.id.imgMedicine)
        val tvTeacherNote: TextView = view.findViewById(R.id.tvTeacherNote)
        val btnCancel: Button = view.findViewById(R.id.btnCancelMedRequest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medication_history_ph, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = list[position]
        
        holder.tvDate.text = "Ngày: ${DateHelper.formatLongToDate(item.requestDate)}"
        holder.tvMedDetails.text = "Thuốc: ${item.medicineDetails}"
        holder.tvDosage.text = "Liều dùng: ${item.dosage}"
        
        val freq = item.frequency ?: "N/A"
        val time = item.timeToTake ?: "N/A"
        holder.tvFrequencyAndTime.text = "Tần suất: $freq ($time)"

        if (!item.parentNote.isNullOrEmpty()) {
            holder.tvParentNote.visibility = View.VISIBLE
            holder.tvParentNote.text = "Dặn dò: ${item.parentNote}"
        } else {
            holder.tvParentNote.visibility = View.GONE
        }

        holder.tvStatus.text = when(item.status) {
            "Pending" -> "Đang chờ"
            "Completed" -> "Đã uống"
            "Rejected" -> "Từ chối"
            "Cancelled" -> "Đã hủy"
            else -> item.status
        }

        val bgRes = when(item.status) {
            "Pending" -> R.drawable.bg_badge_gray
            "Completed" -> R.drawable.bg_badge_green
            "Rejected" -> R.drawable.bg_badge_red
            "Cancelled" -> R.drawable.bg_badge_gray
            else -> R.drawable.bg_badge_gray
        }
        holder.tvStatus.setBackgroundResource(bgRes)

        // Chỉ hiện nút Hủy nếu đang Pending
        if (item.status == "Pending") {
            holder.btnCancel.visibility = View.VISIBLE
            holder.btnCancel.setOnClickListener { onCancelClick(item.medRequestId) }
        } else {
            holder.btnCancel.visibility = View.GONE
        }

        if (!item.medicineImageUrl.isNullOrEmpty()) {
            holder.imgMedicine.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(item.medicineImageUrl)
                .placeholder(R.drawable.ic_pill)
                .into(holder.imgMedicine)
        } else {
            holder.imgMedicine.visibility = View.GONE
        }

        if (!item.teacherNote.isNullOrEmpty()) {
            holder.tvTeacherNote.visibility = View.VISIBLE
            holder.tvTeacherNote.text = "Phản hồi GV: ${item.teacherNote}"
        } else {
            holder.tvTeacherNote.visibility = View.GONE
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<MedicationRequestModel>) {
        this.list = newList
        notifyDataSetChanged()
    }
}
