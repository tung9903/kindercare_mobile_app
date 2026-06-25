package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.google.android.material.button.MaterialButton

class ManHinhChucNangThayDoiMatKhau : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chuc_nang_thay_doi_mat_khau)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<LinearLayout>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnCancelUpdate).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnUpdatePassword).setOnClickListener {
            Toast.makeText(this, "Đã cập nhật mật khẩu mới thành công!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}