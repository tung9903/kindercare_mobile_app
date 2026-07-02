package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.R
import com.google.android.material.button.MaterialButton

class ManHinhChiTietThongBao : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chi_tiet_thong_bao)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val notiId = intent.getIntExtra("NOTI_ID", -1)
        val notification = DataManager.allNotifications.find { it.notifId == notiId }

        if (notification == null) {
            finish()
            return
        }

        // Mark as read when opened
        notification.isRead = 1

        setupUI(notification)
    }

    private fun setupUI(noti: com.example.myapplication.Model.NotificationModel) {
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnConfirm).setOnClickListener { finish() }

        findViewById<TextView>(R.id.tvNotiTitle).text = noti.title
        findViewById<TextView>(R.id.tvNotiMessage).text = noti.message
        findViewById<TextView>(R.id.tvNotiTime).text = DateHelper.formatLongToDate(noti.createdAt) + " • " + DateHelper.formatLongToTime(noti.createdAt)

        val tvType = findViewById<TextView>(R.id.tvNotiType)
        when (noti.type) {
            "SYSTEM" -> {
                tvType.text = "Hệ thống"
                tvType.setBackgroundResource(R.drawable.bg_rounded_blue)
            }
            "MANAGEMENT" -> {
                tvType.text = "Ban Giám Hiệu"
                tvType.setBackgroundResource(R.drawable.bg_rounded_orange)
            }
            "PARENTS" -> {
                tvType.text = "Phụ huynh"
                tvType.setBackgroundResource(R.drawable.bg_rounded_blue)
            }
        }
    }
}
