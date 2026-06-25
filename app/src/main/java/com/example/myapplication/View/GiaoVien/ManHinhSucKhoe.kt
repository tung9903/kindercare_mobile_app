package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R

class ManHinhSucKhoe : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chuc_nang_suc_khoe_va_chieu_cao)

        // Xử lý Edge-to-Edge cho RelativeLayout gốc
        val root = findViewById<RelativeLayout>(R.id.layout_main_health) ?: findViewById(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.btn_update_metrics).setOnClickListener {
            Toast.makeText(this, "Tính năng cập nhật số đo đang được phát triển", Toast.LENGTH_SHORT).show()
        }
    }
}
