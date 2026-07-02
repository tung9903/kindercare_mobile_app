package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.HealthRecord
import com.example.myapplication.R

class HealthRecordAdapter(private val recordList: List<HealthRecord>) :
    RecyclerView.Adapter<HealthRecordAdapter.HealthViewHolder>() {

    class HealthViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTermPeriod: TextView = view.findViewById(R.id.tvTermPeriod)
        val tvBMIValue: TextView = view.findViewById(R.id.tvBMIValue)
        val tvHeightValue: TextView = view.findViewById(R.id.tvHeightValue)
        val tvWeightValue: TextView = view.findViewById(R.id.tvWeightValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealthViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_health_record, parent, false)
        return HealthViewHolder(view)
    }

    override fun onBindViewHolder(holder: HealthViewHolder, position: Int) {
        val record = recordList[position]
        holder.tvTermPeriod.text = record.termPeriod
        holder.tvBMIValue.text = "BMI: ${record.bmi}"
        holder.tvHeightValue.text = "Chiều cao: ${record.height} cm"
        holder.tvWeightValue.text = "Cân nặng: ${record.weight} kg"
    }

    override fun getItemCount() = recordList.size
}
