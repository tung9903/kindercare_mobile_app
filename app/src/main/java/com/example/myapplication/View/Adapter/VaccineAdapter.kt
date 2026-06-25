package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.VaccineModel
import com.example.myapplication.R

class VaccineAdapter(private val vaccineList: List<VaccineModel>) :
    RecyclerView.Adapter<VaccineAdapter.VaccineViewHolder>() {

    class VaccineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvVaccineName: TextView = itemView.findViewById(R.id.tvVaccineName)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaccineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vaccine, parent, false)
        return VaccineViewHolder(view)
    }

    override fun onBindViewHolder(holder: VaccineViewHolder, position: Int) {
        val vaccine = vaccineList[position]
        holder.tvVaccineName.text = vaccine.name
        holder.tvStatus.text = vaccine.status

        // Đổi màu background và chữ theo trạng thái
        if (vaccine.isVaccinated) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_done)
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green_text))
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.orange_text))
        }
    }

    override fun getItemCount(): Int = vaccineList.size
}