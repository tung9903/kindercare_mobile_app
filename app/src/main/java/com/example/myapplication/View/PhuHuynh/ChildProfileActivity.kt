package com.example.myapplication.View.PhuHuynh

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.R
import com.example.myapplication.Utils.NavigationUtils

class ChildProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_child_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<View>(R.id.btnNotification).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        findViewById<View>(R.id.imgAvatarHeader).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        bindStudentData()

        findViewById<View>(R.id.btnRequestUpdate).setOnClickListener {
            startActivity(Intent(this, RequestChangeActivity::class.java))
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationPH(this, "PROFILE")
    }

    private fun bindStudentData() {
        val student = DataManager.studentList.firstOrNull() ?: return
        
        findViewById<TextView>(R.id.tvStudentName).text = student.FullName
        findViewById<TextView>(R.id.tvNickname).text = student.nickname ?: "Chưa có biệt danh"
        findViewById<ImageView>(R.id.imgChildAvatar).setImageResource(student.avatarResId)

        // Bind Detail Boxes
        val boxBirth = findViewById<View>(R.id.boxNgaySinh)
        boxBirth.findViewById<TextView>(R.id.tvBoxLabel).text = "NGÀY SINH"
        boxBirth.findViewById<TextView>(R.id.tvBoxValue).text = student.getFormattedDob()

        val boxGender = findViewById<View>(R.id.boxGioiTinh)
        boxGender.findViewById<TextView>(R.id.tvBoxLabel).text = "GIỚI TÍNH"
        boxGender.findViewById<TextView>(R.id.tvBoxValue).text = student.Gender

        val boxId = findViewById<View>(R.id.boxMaHocSinh)
        boxId.findViewById<TextView>(R.id.tvBoxLabel).text = "MÃ HỌC SINH"
        boxId.findViewById<TextView>(R.id.tvBoxValue).text = "KC-2024-${student.StudentID}"

        val boxAdmission = findViewById<View>(R.id.boxNgayNhapHoc)
        boxAdmission.findViewById<TextView>(R.id.tvBoxLabel).text = "NGÀY NHẬP HỌC"
        boxAdmission.findViewById<TextView>(R.id.tvBoxValue).text = 
            if (student.AdmissionDate != null) DateHelper.formatLongToDate(student.AdmissionDate) else "Chưa rõ"
    }
}
