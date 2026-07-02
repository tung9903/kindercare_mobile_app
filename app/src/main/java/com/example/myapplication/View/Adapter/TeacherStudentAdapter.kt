package com.example.myapplication.View.Adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.android.material.imageview.ShapeableImageView
import org.json.JSONObject

class TeacherStudentAdapter(
    private var studentList: List<JSONObject>,
    private val onStudentClick: (Int) -> Unit
) : RecyclerView.Adapter<TeacherStudentAdapter.ViewHolder>() {

    private var fullList: List<JSONObject> = studentList

    fun filter(query: String) {
        studentList = if (query.isEmpty()) {
            fullList
        } else {
            fullList.filter { 
                it.optString("fullName").contains(query, ignoreCase = true) ||
                it.optString("parentPhone").contains(query)
            }
        }
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgStudent: ShapeableImageView = view.findViewById(R.id.imgStudent)
        val tvName: TextView = view.findViewById(R.id.tvStudentName)
        val tvPhone: TextView = view.findViewById(R.id.tvParentPhone)
        val tvBloodType: TextView = view.findViewById(R.id.tvBloodType)
        val tvAllergy: TextView = view.findViewById(R.id.tvAllergyBadge)
        val btnCall: ImageView = view.findViewById(R.id.btnCall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_gv, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = studentList[position]
        val studentId = student.optInt("studentId")
        
        holder.tvName.text = student.optString("fullName", "Học sinh")
        holder.tvPhone.text = "PH: " + student.optString("parentPhone", "Chưa có SĐT")
        
        val bloodType = student.optString("bloodType", "N/A")
        holder.tvBloodType.text = "Máu $bloodType"
        
        val allergies = student.optString("allergies", "")
        if (allergies.isNotEmpty() && allergies != "Không" && allergies != "null") {
            holder.tvAllergy.visibility = View.VISIBLE
            holder.tvAllergy.text = "⚠️ $allergies"
        } else {
            holder.tvAllergy.visibility = View.GONE
        }

        holder.btnCall.setOnClickListener {
            val phone = student.optString("parentPhone")
            if (phone.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                holder.itemView.context.startActivity(intent)
            }
        }

        holder.itemView.setOnClickListener {
            onStudentClick(studentId)
        }
    }

    override fun getItemCount() = studentList.size
    
    fun updateData(newList: List<JSONObject>) {
        this.studentList = newList
        this.fullList = newList
        notifyDataSetChanged()
    }
}
