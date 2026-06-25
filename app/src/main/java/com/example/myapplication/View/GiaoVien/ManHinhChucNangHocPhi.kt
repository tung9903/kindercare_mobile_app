package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R

class ManHinhChucNangHocPhi : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chuc_nang_hoc_phi)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<LinearLayout>(R.id.btn_back).setOnClickListener {
            finish()
        }

        val studentId = intent.getIntExtra("STUDENT_ID", -1)
        val student = DataManager.studentList.find { it.StudentID == studentId }
        
        if (student != null) {
            findViewById<TextView>(R.id.tvHeaderTitle).text = "Học phí: ${student.FullName}"
        }
    }
}
