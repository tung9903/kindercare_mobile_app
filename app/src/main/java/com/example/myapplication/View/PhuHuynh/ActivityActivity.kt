package com.example.myapplication.View.PhuHuynh

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.example.myapplication.Utils.NavigationUtils
import com.example.myapplication.View.Adapter.TimelinePHAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityActivity : AppCompatActivity() {

    private lateinit var rvTimeline: RecyclerView
    private lateinit var timelineAdapter: TimelinePHAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_activity)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.tvTodayText).text = 
            SimpleDateFormat("'Thứ' EEEE, dd/MM/yyyy", Locale("vi", "VN")).format(Date())

        setupRecyclerView()
        setupHeaderActions()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        rvTimeline = findViewById(R.id.rvTimeline)
        
        // Lấy lịch trình thực tế từ DataManager
        val schedules = DataManager.getMockSchedules()
        
        timelineAdapter = TimelinePHAdapter(schedules) { item ->
            // Khi nhấn vào mốc thời gian
            if (item.ActivityType == "meal") {
                // Nếu là bữa ăn -> Dẫn tới chi tiết khẩu phần
                val intent = Intent(this, MealDetailActivity::class.java)
                startActivity(intent)
            } else {
                // Các hoạt động khác -> Hiển thị thông báo chi tiết (Hoặc mở Activity chi tiết hoạt động)
                Toast.makeText(this, "Hoạt động: ${item.ActivityName}\n${item.Details}", Toast.LENGTH_LONG).show()
            }
        }
        
        rvTimeline.layoutManager = LinearLayoutManager(this)
        rvTimeline.adapter = timelineAdapter
    }

    private fun setupHeaderActions() {
        findViewById<View>(R.id.btn_notification).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
        findViewById<View>(R.id.img_top_avatar).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationPH(this, "ACTIVITY")
    }
}
