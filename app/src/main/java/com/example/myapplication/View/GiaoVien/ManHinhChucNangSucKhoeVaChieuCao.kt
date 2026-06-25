package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.google.android.material.button.MaterialButton
import java.util.Locale

class ManHinhChucNangSucKhoeVaChieuCao : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chuc_nang_suc_khoe_va_chieu_cao)
        
        val mainView = findViewById<View>(R.id.layout_main_health)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        val studentId = intent.getIntExtra("STUDENT_ID", -1)
        val student = DataManager.studentList.find { it.StudentID == studentId }

        if (student == null) {
            Toast.makeText(this, "Không tìm thấy thông tin bé", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<TextView>(R.id.tvHeaderTitle).text = "Số đo bé: ${student.FullName}"
        
        val tvHeight = findViewById<TextView>(R.id.tvCurrentHeight)
        val tvWeight = findViewById<TextView>(R.id.tvCurrentWeight)
        val tvBMI = findViewById<TextView>(R.id.tvCurrentBMI)

        fun updateUI() {
            tvHeight.text = String.format(Locale.getDefault(), "%.1f cm", student.height)
            tvWeight.text = String.format(Locale.getDefault(), "%.1f kg", student.weight)
            tvBMI.text = String.format(Locale.getDefault(), "%.1f", student.bmi)
        }

        updateUI()

        findViewById<TextView>(R.id.btn_update_metrics).setOnClickListener {
            // Hiển thị form cập nhật (Trong thực tế có thể dùng Dialog)
            Toast.makeText(this, "Đang mở form cập nhật số đo...", Toast.LENGTH_SHORT).show()
            // Tạm thời update giả định
            student.height += 0.5
            student.weight += 0.2
            student.bmi = student.weight / ((student.height/100) * (student.height/100))
            updateUI()
            Toast.makeText(this, "Đã cập nhật số đo mới cho bé!", Toast.LENGTH_SHORT).show()
        }
    }
}
