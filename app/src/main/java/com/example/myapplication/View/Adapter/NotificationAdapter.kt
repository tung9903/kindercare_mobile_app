package com.example.myapplication.View.Adapter

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.Model.NotificationModel
import com.example.myapplication.R

class NotificationAdapter(
    private var notiList: List<NotificationModel>,
    private val onItemClick: (NotificationModel) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotiViewHolder>() {

    class NotiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val btnAction: Button = itemView.findViewById(R.id.btnAction)
        val viewUnreadDot: View = itemView.findViewById(R.id.viewUnreadDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotiViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotiViewHolder, position: Int) {
        val item = notiList[position]

        holder.tvTitle.text = item.Title
        holder.tvContent.text = item.Message
        holder.tvTime.text = DateHelper.formatLongToDate(item.CreatedAt)

        if (item.IsRead) {
            holder.tvTitle.setTextColor(Color.parseColor("#475467"))
            holder.tvTitle.typeface = Typeface.create(holder.tvTitle.typeface, Typeface.NORMAL)
            holder.tvContent.setTextColor(Color.parseColor("#98A2B3"))
            holder.viewUnreadDot.visibility = View.GONE
        } else {
            holder.tvTitle.setTextColor(Color.parseColor("#101828"))
            holder.tvTitle.typeface = Typeface.create(holder.tvTitle.typeface, Typeface.BOLD)
            holder.tvContent.setTextColor(Color.parseColor("#475467"))
            holder.viewUnreadDot.visibility = View.VISIBLE
            holder.viewUnreadDot.setBackgroundColor(Color.parseColor("#2F80ED"))
        }

        if (item.hasAction) {
            holder.btnAction.visibility = View.VISIBLE
            holder.btnAction.setOnClickListener { onItemClick(item) }
        } else {
            holder.btnAction.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (!item.IsRead) {
                item.IsRead = true
                notifyItemChanged(position)
            }
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = notiList.size

    fun updateData(newList: List<NotificationModel>) {
        this.notiList = newList
        notifyDataSetChanged()
    }
}
