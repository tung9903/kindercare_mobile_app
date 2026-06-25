package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R

class ManHinhThongTinChungCuaHocSinh : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_thong_tin_chung_cua_hoc_sinh)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        val studentId = intent.getIntExtra("STUDENT_ID", -1)
        val student = DataManager.studentList.find { it.StudentID == studentId }
        
        if (student != null) {
            // 1. Cập nhật Thẻ học sinh (Grid Style Card)
            findViewById<ImageView>(R.id.ivStudentAvatarCard).setImageResource(student.avatarResId)
            findViewById<TextView>(R.id.tvStudentNameCard).text = student.FullName
            findViewById<TextView>(R.id.tvStudentNickNameCard).text = "Biệt danh: ${student.nickname}"
            findViewById<TextView>(R.id.tvStudentDobCard).text = "📅 ${student.getFormattedDob()}"
            
            val tvAllergy = findViewById<TextView>(R.id.tvStudentAllergyCard)
            if (!student.Allergies.isNullOrEmpty() && student.Allergies != "Không") {
                tvAllergy.visibility = View.VISIBLE
                tvAllergy.text = "⚠️ ${student.Allergies}"
            } else {
                tvAllergy.visibility = View.GONE
            }

            // 2. Cập nhật phần Thông tin cơ bản
            findViewById<TextView>(R.id.tvFullName).text = "Họ và tên: ${student.FullName}"
            findViewById<TextView>(R.id.tvDob).text = "Ngày sinh: ${student.getFormattedDob()}"
            findViewById<TextView>(R.id.tvParentName).text = "Phụ huynh: ${student.parentName}"
            findViewById<TextView>(R.id.tvHeaderTitle).text = "Hồ sơ của ${student.FullName}"

            // 3. Cập nhật phần Thông tin liên hệ
            findViewById<TextView>(R.id.tvContactName).text = student.parentName
            findViewById<TextView>(R.id.tvContactPhone).text = student.parentPhone
        }
    }
}
