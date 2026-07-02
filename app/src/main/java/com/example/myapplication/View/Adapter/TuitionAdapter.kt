package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.InvoiceModel
import com.example.myapplication.R
import java.text.DecimalFormat

class TuitionAdapter(
    private var invoiceList: List<InvoiceModel>,
    private val onPayClick: (InvoiceModel) -> Unit
) : RecyclerView.Adapter<TuitionAdapter.TuitionViewHolder>() {

    private val formatter = DecimalFormat("#,###")

    class TuitionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTuitionTitle)
        val tvTotal: TextView = view.findViewById(R.id.tvTuitionSubtitle)
        val tvStatus: TextView = view.findViewById(R.id.tvStatusBadge)
        val tvMainFee: TextView = view.findViewById(R.id.tvMainFee)
        val tvMealFee: TextView = view.findViewById(R.id.tvMealFee)
        val tvDiscount: TextView = view.findViewById(R.id.tvDiscount)
        val rowDiscount: View = view.findViewById(R.id.rowDiscount)
        val btnPay: Button = view.findViewById(R.id.btnPayNow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TuitionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tuition, parent, false)
        return TuitionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TuitionViewHolder, position: Int) {
        val invoice = invoiceList[position]
        
        holder.tvTitle.text = "Học phí tháng ${invoice.billingMonth}"
        holder.tvMainFee.text = "${formatter.format(invoice.tuitionFee)}đ"
        holder.tvMealFee.text = "${formatter.format(invoice.expectedMealFee)}đ"
        
        if (invoice.discountAmount > 0) {
            holder.rowDiscount.visibility = View.VISIBLE
            holder.tvDiscount.text = "-${formatter.format(invoice.discountAmount)}đ"
        } else {
            holder.rowDiscount.visibility = View.GONE
        }

        holder.tvTotal.text = "${formatter.format(invoice.totalAmount)}đ"

        // Trạng thái nộp tiền
        if (invoice.paymentStatus == "Paid") {
            holder.tvStatus.text = "Đã nộp"
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_status_green)
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#027A48"))
            holder.btnPay.visibility = View.GONE
        } else {
            holder.tvStatus.text = "Chưa nộp"
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
            holder.tvStatus.setTextColor(android.graphics.Color.RED)
            holder.btnPay.visibility = View.VISIBLE
        }

        holder.btnPay.setOnClickListener { onPayClick(invoice) }
    }

    override fun getItemCount() = invoiceList.size

    fun updateData(newList: List<InvoiceModel>) {
        this.invoiceList = newList
        notifyDataSetChanged()
    }
}
