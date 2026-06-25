package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.TuitionItem
import com.example.myapplication.R

class TuitionAdapter(private val items: List<TuitionItem>) :
    RecyclerView.Adapter<TuitionAdapter.TuitionViewHolder>() {

    class TuitionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTuitionTitle)
        val tvStatus: TextView = view.findViewById(R.id.tvStatusBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TuitionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tuition, parent, false)
        return TuitionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TuitionViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title

        if (item.isPaid) {
            holder.tvStatus.text = "Đã thanh toán"
            holder.tvStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
        } else {
            holder.tvStatus.text = "Chưa thanh toán"
            holder.tvStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
        }
    }

    override fun getItemCount(): Int = items.size
}
