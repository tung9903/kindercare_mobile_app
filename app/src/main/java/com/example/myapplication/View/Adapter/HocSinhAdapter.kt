package com.example.myapplication.View.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ClassMenuBottomSheet
import com.example.myapplication.View.GiaoVien.ManHinhChatGiaoVien
import com.example.myapplication.View.GiaoVien.ManHinhChucNangHoSoHocSinh
import com.example.myapplication.Model.Student
import com.example.myapplication.R

class HocSinhAdapter(private var listHocSinh: List<Student>) :
    RecyclerView.Adapter<HocSinhAdapter.HocSinhViewHolder>() {

    private var fullListHocSinh: List<Student> = listHocSinh

    fun filter(query: String) {
        listHocSinh = if (query.isEmpty()) {
            fullListHocSinh
        } else {
            fullListHocSinh.filter { it.FullName.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    fun updateData(newList: List<Student>) {
        this.listHocSinh = newList
        this.fullListHocSinh = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HocSinhViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hocsinh, parent, false)
        return HocSinhViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: HocSinhViewHolder,
        position: Int
    ) {
        val hs = listHocSinh[position]

        holder.tvTen.text = hs.FullName
        holder.tvThongTinPhu.text = "${hs.Gender} | ${hs.EnrollmentStatus}"
        holder.tvNgaySinh.text = hs.getFormattedDob()
        holder.tvTenPhuHuynh.text = "PH: ${hs.parentName}"
        holder.imgAvatar.setImageResource(hs.avatarResId)
        
        if (!hs.Allergies.isNullOrEmpty() && hs.Allergies != "Không") {
            holder.layoutDiUng.visibility = View.VISIBLE
            holder.tvGhiChuDiUng.text = hs.Allergies
        } else {
            holder.layoutDiUng.visibility = View.GONE
        }

        holder.btnCall.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Gọi cho PH của ${hs.FullName}", Toast.LENGTH_SHORT).show()
        }

        holder.btnMessage.setOnClickListener {
            val intent = Intent(holder.itemView.context, ManHinhChatGiaoVien::class.java)
            intent.putExtra("PARENT_NAME", hs.parentName)
            intent.putExtra("STUDENT_NAME", hs.FullName)
            holder.itemView.context.startActivity(intent)
        }

        holder.btnMoreOptions.setOnClickListener { view ->
            val context = view.context
            if (context is FragmentActivity) {
                val bottomSheet = ClassMenuBottomSheet.newInstance(hs.StudentID)
                bottomSheet.show(context.supportFragmentManager, "ClassMenuBottomSheet")
            }
        }

        holder.itemView.setOnClickListener { view ->
            val intent = Intent(view.context, ManHinhChucNangHoSoHocSinh::class.java)
            intent.putExtra("STUDENT_ID", hs.StudentID)
            view.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return listHocSinh.size
    }

    class HocSinhViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatarHocSinh)
        val tvTen: TextView = itemView.findViewById(R.id.tvTenHocSinh)
        val tvThongTinPhu: TextView = itemView.findViewById(R.id.tvThongTinPhu)
        val tvNgaySinh: TextView = itemView.findViewById(R.id.tvNgaySinh)
        val tvTenPhuHuynh: TextView = itemView.findViewById(R.id.tvTenPhuHuynh)
        val layoutDiUng: LinearLayout = itemView.findViewById(R.id.layoutDiUng)
        val tvGhiChuDiUng: TextView = itemView.findViewById(R.id.tvGhiChuDiUng)
        val btnMoreOptions: ImageView = itemView.findViewById(R.id.btnMoreOptions)
        val btnCall: ImageView = itemView.findViewById(R.id.btnCall)
        val btnMessage: ImageView = itemView.findViewById(R.id.btnMessage)
    }
}
