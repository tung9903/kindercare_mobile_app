package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import java.util.Locale

class ManHinhXacNhanLuuDiemDanhVaGuiThongBao : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_xac_nhan_luu_diem_danh_va_gui_thong_bao)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Nhận dữ liệu từ Intent
        val presentCount = intent.getIntExtra("PRESENT_COUNT", 0)
        val absentCount = intent.getIntExtra("ABSENT_COUNT", 0)
        val totalCount = intent.getIntExtra("TOTAL_COUNT", 0)

        // Hiển thị dữ liệu lên các View
        findViewById<TextView>(R.id.tvPresentConfirm).text = String.format(Locale.getDefault(), "%02d", presentCount)
        findViewById<TextView>(R.id.tvAbsentConfirm).text = String.format(Locale.getDefault(), "%02d", absentCount)
        findViewById<TextView>(R.id.tvTotalConfirm).text = String.format(Locale.getDefault(), "%02d", totalCount)

        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            Toast.makeText(this, "Đã chốt danh sách và gửi thông báo thành công!", Toast.LENGTH_LONG).show()
            // Sau khi thành công có thể quay về màn hình chính
            val intent = Intent(this, ManHinhBangDieuKhien::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}
