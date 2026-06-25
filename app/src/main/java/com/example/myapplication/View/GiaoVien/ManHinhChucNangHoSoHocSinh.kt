package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R

class ManHinhChucNangHoSoHocSinh : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chuc_nang_ho_so_hoc_sinh)
        
        val root = findViewById<android.view.View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val studentId = intent.getIntExtra("STUDENT_ID", -1)
        val student = DataManager.studentList.find { it.StudentID == studentId }

        if (student != null) {
            findViewById<ImageView>(R.id.ivAvatarProfile).setImageResource(student.avatarResId)
            findViewById<TextView>(R.id.tvTenProfile).text = student.FullName
            findViewById<TextView>(R.id.tvNgaySinhProfile).text = student.getFormattedDob()
            findViewById<TextView>(R.id.tvThongTinPhuProfile).text = student.Gender
            findViewById<TextView>(R.id.tvTenPhuHuynhProfile).text = student.parentName
            findViewById<TextView>(R.id.tvDiUngProfile).text = student.Allergies ?: "Không có"
        }

        findViewById<TextView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        setupButtons(studentId)
    }

    private fun setupButtons(studentId: Int) {
        findViewById<AppCompatButton>(R.id.btn_general_info).setOnClickListener {
            val intent = Intent(this, ManHinhThongTinChungCuaHocSinh::class.java)
            intent.putExtra("STUDENT_ID", studentId)
            startActivity(intent)
        }

        findViewById<AppCompatButton>(R.id.btn_health_height).setOnClickListener {
            val intent = Intent(this, ManHinhChucNangSucKhoeVaChieuCao::class.java)
            intent.putExtra("STUDENT_ID", studentId)
            startActivity(intent)
        }

        findViewById<AppCompatButton>(R.id.btn_reviews).setOnClickListener {
            val intent = Intent(this, ManHinhNhanXetVaDanhGia::class.java)
            intent.putExtra("STUDENT_ID", studentId)
            startActivity(intent)
        }

        findViewById<AppCompatButton>(R.id.btn_tuition).setOnClickListener {
            val intent = Intent(this, ManHinhChucNangHocPhi::class.java)
            intent.putExtra("STUDENT_ID", studentId)
            startActivity(intent)
        }
    }
}
