package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.ChildInfoModel
import com.example.myapplication.R

class ChildDetailAdapter(private val detailsList: List<ChildInfoModel>) :
    RecyclerView.Adapter<ChildDetailAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLabel: TextView = view.findViewById(R.id.tvBoxLabel)
        val tvValue: TextView = view.findViewById(R.id.tvBoxValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_child_detail_box, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = detailsList[position]
        holder.tvLabel.text = data.label.uppercase()
        holder.tvValue.text = data.value
    }

    override fun getItemCount(): Int = detailsList.size
}