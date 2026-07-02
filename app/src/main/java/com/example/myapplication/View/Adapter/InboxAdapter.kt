package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.R
import org.json.JSONObject

class InboxAdapter(
    private var messageList: List<JSONObject>,
    private val onItemClick: (JSONObject) -> Unit
) : RecyclerView.Adapter<InboxAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSenderName: TextView = view.findViewById(R.id.tvSenderName)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvSubject: TextView = view.findViewById(R.id.tvSubject)
        val tvSnippet: TextView = view.findViewById(R.id.tvSnippet)
        val unreadDot: View = view.findViewById(R.id.unreadDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inbox_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = messageList[position]
        val type = item.optString("type") // "MEDICINE", "LEAVE", "FEEDBACK"
        
        holder.tvSenderName.text = item.optString("senderName", "Phụ huynh")
        
        val time = item.optLong("updatedTime", item.optLong("createdAt", 0))
        holder.tvTime.text = if (time > 0) DateHelper.formatLongToTime(time) else ""
        
        holder.tvSubject.text = when(type) {
            "MEDICINE" -> "Dặn thuốc: " + item.optString("medicineDetails")
            "LEAVE" -> "Nghỉ phép: " + item.optString("reason")
            "FEEDBACK" -> "Ý kiến PH: " + item.optString("content").take(20) + "..."
            else -> item.optString("title", "Thông báo mới")
        }

        holder.tvSnippet.text = item.optString("parentNote", item.optString("content", "Nhấn để xem chi tiết"))
        
        // Gmail style: unread items have bold text and blue dot
        val isRead = item.optInt("isRead", 1) == 1
        holder.unreadDot.visibility = if (isRead) View.GONE else View.VISIBLE
        
        if (!isRead) {
            holder.tvSubject.setTypeface(null, android.graphics.Typeface.BOLD)
            holder.tvSenderName.setTypeface(null, android.graphics.Typeface.BOLD)
        } else {
            holder.tvSubject.setTypeface(null, android.graphics.Typeface.NORMAL)
            holder.tvSenderName.setTypeface(null, android.graphics.Typeface.NORMAL)
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = messageList.size

    fun updateData(newList: List<JSONObject>) {
        this.messageList = newList
        notifyDataSetChanged()
    }
}
