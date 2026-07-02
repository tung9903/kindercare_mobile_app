package com.example.myapplication.View.PhuHuynh

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.*
import com.example.myapplication.R
import com.example.myapplication.Utils.NavigationUtils
import com.example.myapplication.View.Adapter.TimelinePHAdapter
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ActivityActivity : AppCompatActivity() {

    private lateinit var rvTimeline: RecyclerView
    private lateinit var timelineAdapter: TimelinePHAdapter
    private val scheduleList = mutableListOf<DailySchedule>()

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

        // Ưu tiên lấy bé đang chọn
        val selectedChild = DataManager.selectedChild
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)

        if (selectedChild != null && token != null) {
            val classId = selectedChild.optInt("classId", 1)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis / 1000
            fetchClassSchedule(classId, today, token)
        } else {
            fetchChildAndLoadSchedule()
        }
    }

    private fun setupRecyclerView() {
        rvTimeline = findViewById(R.id.rvTimeline)
        timelineAdapter = TimelinePHAdapter(scheduleList) { item ->
            if (item.activityType == "meal") {
                startActivity(Intent(this, MealDetailActivity::class.java))
            }
        }
        rvTimeline.layoutManager = LinearLayoutManager(this)
        rvTimeline.adapter = timelineAdapter
    }

    private fun fetchChildAndLoadSchedule() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children")
            .addHeader("Authorization", "Bearer $token")
            .get().build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val dataArray = JSONObject(body).optJSONArray("data")
                        if (dataArray != null && dataArray.length() > 0) {
                            val child = dataArray.getJSONObject(0)
                            val classId = child.optInt("classId", 1)
                            val today = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                            }.timeInMillis / 1000
                            fetchClassSchedule(classId, today, token)
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    private fun fetchClassSchedule(classId: Int, date: Long, token: String) {
        val url = "https://web-test.kindercare.app/api/v1/teacher/classes/$classId/schedule?date=$date"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val dataArray = JSONObject(body).optJSONArray("data")
                        val tempSchedules = mutableListOf<DailySchedule>()
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                val obj = it.getJSONObject(i)
                                tempSchedules.add(DailySchedule(
                                    dailyScheduleId = obj.optInt("dailyScheduleId"),
                                    classId = obj.optInt("classId"),
                                    scheduleDate = obj.optLong("scheduleDate"),
                                    startTime = obj.optLong("startTime"),
                                    endTime = obj.optLong("endTime"),
                                    activityName = obj.optString("activityName"),
                                    details = obj.optString("details"),
                                    location = obj.optString("location"),
                                    activityType = obj.optString("activityType"),
                                    status = obj.optString("status")
                                ))
                            }
                        }
                        runOnUiThread {
                            scheduleList.clear()
                            scheduleList.addAll(tempSchedules)
                            timelineAdapter.notifyDataSetChanged()
                            if (scheduleList.isEmpty()) {
                                Toast.makeText(this@ActivityActivity, "Hôm nay lớp chưa có lịch trình", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
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
