package com.example.myapplication.View.Adapter

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.Model.NotificationModel
import com.example.myapplication.R

class NotificationGVAdapter(
    private var notiList: List<NotificationModel>,
    private val onItemClick: (NotificationModel) -> Unit
) : RecyclerView.Adapter<NotificationGVAdapter.NotiViewHolder>() {

    class NotiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconContainer: FrameLayout = itemView.findViewById(R.id.layout_icon_container)
        val imgIcon: ImageView = itemView.findViewById(R.id.img_noti_icon)
        val txtTitle: TextView = itemView.findViewById(R.id.txt_noti_title)
        val txtTime: TextView = itemView.findViewById(R.id.txt_noti_time)
        val txtContent: TextView = itemView.findViewById(R.id.txt_noti_content)
        val txtNewBadge: TextView = itemView.findViewById(R.id.txt_new_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification_gv, parent, false)
        return NotiViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotiViewHolder, position: Int) {
        val item = notiList[position]

        holder.txtTitle.text = item.title
        holder.txtContent.text = item.message
        holder.txtTime.text = DateHelper.formatLongToDate(item.createdAt)

        if (item.isReadBool()) {
            holder.txtTitle.setTextColor(Color.parseColor("#475467"))
            holder.txtTitle.typeface = Typeface.create(holder.txtTitle.typeface, Typeface.NORMAL)
            holder.txtContent.setTextColor(Color.parseColor("#98A2B3"))
            holder.txtNewBadge.visibility = View.GONE
        } else {
            holder.txtTitle.setTextColor(Color.parseColor("#101828"))
            holder.txtTitle.typeface = Typeface.create(holder.txtTitle.typeface, Typeface.BOLD)
            holder.txtContent.setTextColor(Color.parseColor("#475467"))
            holder.txtNewBadge.visibility = View.VISIBLE
        }

        when (item.type) {
            "MANAGEMENT" -> {
                holder.iconContainer.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.blue_light)
                holder.imgIcon.setImageResource(R.drawable.ic_warning)
                holder.imgIcon.setColorFilter(Color.parseColor("#2F80ED"))
            }
            else -> {
                holder.iconContainer.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.gray_light)
                holder.imgIcon.setImageResource(R.drawable.ic_activity)
                holder.imgIcon.setColorFilter(Color.parseColor("#667085"))
            }
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
