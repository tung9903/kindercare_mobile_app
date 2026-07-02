package com.example.myapplication.View.PhuHuynh

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DailySchedule
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.SchedulePHAdapter
import okhttp3.Request
import org.json.JSONObject
import java.util.Calendar

class PlayActivity : AppCompatActivity() {

    private lateinit var rvSchedule: RecyclerView
    private lateinit var adapter: SchedulePHAdapter
    private val scheduleList = mutableListOf<DailySchedule>()
    private var currentStudentId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play)

        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // Lấy studentId từ DataManager
        currentStudentId = DataManager.selectedChild?.optInt("studentId") ?: -1
        
        setupRecyclerView()
        
        if (currentStudentId != -1) {
            fetchDailySchedule()
        }
    }

    private fun setupRecyclerView() {
        rvSchedule = findViewById(R.id.rvDailySchedule)
        adapter = SchedulePHAdapter(scheduleList)
        rvSchedule.layoutManager = LinearLayoutManager(this)
        rvSchedule.adapter = adapter
    }

    private fun fetchDailySchedule() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        // Mặc định lấy ngày hôm nay (Unix timestamp seconds)
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis / 1000

        val url = "https://web-test.kindercare.app/api/v1/parent/children/$currentStudentId/daily-schedule?date=$today"
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
                        val json = JSONObject(body)
                        val dataArray = json.optJSONArray("data")
                        val temp = mutableListOf<DailySchedule>()
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                val obj = it.getJSONObject(i)
                                temp.add(DailySchedule(
                                    dailyScheduleId = obj.optInt("dailyScheduleId"),
                                    classId = obj.optInt("classId"),
                                    scheduleDate = obj.optLong("scheduleDate"),
                                    startTime = obj.optLong("startTime"),
                                    endTime = obj.optLong("endTime"),
                                    activityName = obj.optString("activityName"),
                                    details = if (obj.isNull("details")) null else obj.optString("details"),
                                    location = obj.optString("location"),
                                    activityType = obj.optString("activityType"),
                                    status = obj.optString("status")
                                ))
                            }
                        }
                        runOnUiThread {
                            scheduleList.clear()
                            scheduleList.addAll(temp)
                            adapter.notifyDataSetChanged()
                        }
                    }
                    return@use
                }
            } catch (e: Exception) {
                Log.e("SCHEDULE_API", "Error: ${e.message}")
            }
        }.start()
    }
}
