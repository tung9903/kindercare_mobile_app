package com.example.myapplication.View.PhuHuynh

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.NotificationModel
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.NotificationAdapter

class NotificationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private lateinit var btnback: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnback = findViewById(R.id.btnBack)
        recyclerView = findViewById(R.id.recyclerViewNotifications)
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnback.setOnClickListener {
            finish()
        }

        val now = System.currentTimeMillis() / 1000
        val notificationList = mutableListOf(
            NotificationModel(
                NotifID = 1,
                UserID = null,
                Title = "[KHẨN CẤP] Tới hạn thanh toán học phí",
                Message = "Hôm nay là hạn chót thanh toán học phí Quý 3 cho bé Ngọc Châu.",
                Type = "SYSTEM",
                ActionLink = null,
                IsRead = false,
                isUrgent = true,
                hasAction = true,
                CreatedAt = now
            ),
            NotificationModel(
                NotifID = 2,
                UserID = null,
                Title = "Thông báo thu học phí",
                Message = "Nhà trường đã phát hành hóa đơn học phí Quý 3.",
                Type = "SYSTEM",
                ActionLink = null,
                IsRead = false,
                CreatedAt = now - 600
            ),
            NotificationModel(
                NotifID = 3,
                UserID = null,
                Title = "Đơn xin nghỉ phép đã được duyệt",
                Message = "Giáo viên chủ nhiệm lớp Mầm 1 đã phê duyệt đơn xin nghỉ phép.",
                Type = "MANAGEMENT",
                ActionLink = null,
                IsRead = false,
                CreatedAt = now - 3600
            )
        )

        adapter = NotificationAdapter(notificationList) { selectedItem ->
            Toast.makeText(this, "Thực hiện hành động cho: ${selectedItem.Title}", Toast.LENGTH_SHORT).show()
        }

        recyclerView.adapter = adapter

        // Fix ID mismatch: use tvMarkAllRead instead of btn_mark_all_read
        findViewById<TextView>(R.id.tvMarkAllRead).setOnClickListener {
            for (noti in notificationList) {
                noti.IsRead = true
            }
            adapter.notifyDataSetChanged()
        }
    }
}
