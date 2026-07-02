package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.TeacherStudentResponse
import com.example.myapplication.R
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.imageview.ShapeableImageView

class StudentMealAdapter(
    private var studentList: List<TeacherStudentResponse>
) : RecyclerView.Adapter<StudentMealAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ShapeableImageView = itemView.findViewById(R.id.imgStudentAvatar)
        val tvName: TextView = itemView.findViewById(R.id.txtStudentName)
        val toggleGroup: MaterialButtonToggleGroup = itemView.findViewById(R.id.toggleGroupMeal)
        val edtNote: EditText = itemView.findViewById(R.id.edtNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student_meal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = studentList[position]
        holder.tvName.text = student.fullName
        // Tạm thời dùng ảnh mặc định, sau này dùng Glide load student.avatarUrl
        holder.ivAvatar.setImageResource(R.drawable.avatar)

        holder.toggleGroup.clearOnButtonCheckedListeners()

        val checkedId = when (student.status) {
            "FULL" -> R.id.btnFull
            "HALF" -> R.id.btnHalf
            "LESS" -> R.id.btnLess
            else -> R.id.btnFull
        }
        holder.toggleGroup.check(checkedId)

        holder.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                student.status = when (checkedId) {
                    R.id.btnFull -> "FULL"
                    R.id.btnHalf -> "HALF"
                    R.id.btnLess -> "LESS"
                    else -> "FULL"
                }
            }
        }
    }

    override fun getItemCount(): Int = studentList.size

    fun updateData(newList: List<TeacherStudentResponse>) {
        this.studentList = newList
        notifyDataSetChanged()
    }
}
