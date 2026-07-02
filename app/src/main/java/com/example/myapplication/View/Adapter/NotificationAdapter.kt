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
        val layoutContainer: View = itemView.findViewById(R.id.layoutContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotiViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotiViewHolder, position: Int) {
        val item = notiList[position]

        holder.tvTitle.text = item.title
        holder.tvContent.text = item.message
        holder.tvTime.text = DateHelper.formatLongToDate(item.createdAt)

        // Phân loại thông báo khẩn cấp (isCritical là Int theo Swagger)
        if (item.isCritical == 1) {
            holder.layoutContainer.setBackgroundResource(R.drawable.bg_alert_red)
            holder.tvTitle.setTextColor(Color.parseColor("#B42318"))
        } else {
            holder.layoutContainer.setBackgroundColor(Color.WHITE)
            holder.tvTitle.setTextColor(Color.parseColor("#101828"))
        }

        if (item.isReadBool()) {
            holder.tvTitle.typeface = Typeface.create(holder.tvTitle.typeface, Typeface.NORMAL)
            holder.tvContent.setTextColor(Color.parseColor("#98A2B3"))
            holder.viewUnreadDot.visibility = View.GONE
        } else {
            holder.tvTitle.typeface = Typeface.create(holder.tvTitle.typeface, Typeface.BOLD)
            holder.tvContent.setTextColor(Color.parseColor("#475467"))
            holder.viewUnreadDot.visibility = View.VISIBLE
            holder.viewUnreadDot.setBackgroundResource(R.drawable.bg_circle_green)
        }

        // Logic action button dựa trên dataPayload hoặc type
        if (item.dataPayload != null) {
            holder.btnAction.visibility = View.VISIBLE
            holder.btnAction.setOnClickListener { onItemClick(item) }
        } else {
            holder.btnAction.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (!item.isReadBool()) {
                item.isRead = 1
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
