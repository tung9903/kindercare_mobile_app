package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R

class ManHinhChucNangBaoCaoSuCoYTe : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chuc_nang_bao_cao_su_co_yte)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btn_back)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<LinearLayout>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        val tenHocSinh = intent.getStringExtra("TEN_HOC_SINH") ?: "Học sinh"
        val diUng = intent.getStringExtra("NOI_DUNG_DI_UNG") ?: "Không có thông tin dị ứng"

        val tvTenVaDiUng = findViewById<TextView>(R.id.tvTenVaDiUng)
        val tvChiTietDiUng = findViewById<TextView>(R.id.tvChiTietDiUng)

        tvTenVaDiUng.text = "$tenHocSinh - $diUng"
        tvChiTietDiUng.text = "Lưu ý đặc biệt: Đối với trường hợp $diUng, tuyệt đối không tiếp xúc với tác nhân gây dị ứng. Theo dõi sát sao biểu hiện của bé."

        findViewById<View>(R.id.btn_emergency_alarm).setOnClickListener {
            Toast.makeText(this, "Đã phát báo động khẩn cấp cho học sinh $tenHocSinh!", Toast.LENGTH_LONG).show()
        }
    }
}