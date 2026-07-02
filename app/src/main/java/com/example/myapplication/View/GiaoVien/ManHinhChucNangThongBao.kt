package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.NotificationModel
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.NotificationAdapter
import android.widget.TextView
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ManHinhChucNangThongBao : AppCompatActivity() {

    private lateinit var rvNotification: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private val notificationList = mutableListOf<NotificationModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chuc_nang_thong_bao)

        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.btn_mark_all_read).setOnClickListener { markAllAsRead() }

        setupRecyclerView()
        fetchNotifications()
    }

    private fun markAllAsRead() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/notifications/read-all")
            .addHeader("Authorization", "Bearer $token")
            .put("{}".toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    runOnUiThread {
                        if (response.isSuccessful) {
                            notificationList.forEach { it.isRead = 1 }
                            adapter.notifyDataSetChanged()
                            Toast.makeText(this, "Đã đọc tất cả thông báo", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return@use
                }
            } catch (e: Exception) { }
        }.start()
    }

    private fun setupRecyclerView() {
        rvNotification = findViewById(R.id.rcv_notifications)
        adapter = NotificationAdapter(notificationList) { item ->
            // Khi nhấn vào thông báo -> Đánh dấu là đã đọc trên server
            markNotificationAsRead(item.notifId)
        }
        rvNotification.layoutManager = LinearLayoutManager(this)
        rvNotification.adapter = adapter
    }

    private fun markNotificationAsRead(notifId: Int) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/notifications/$notifId/read")
            .addHeader("Authorization", "Bearer $token")
            .put("{}".toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    Log.d("NOTIF_READ", "Marked $notifId as read: ${response.code}")
                }
            } catch (e: Exception) { }
        }.start()
    }

    private fun fetchNotifications() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/notifications")
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
                        val temp = mutableListOf<NotificationModel>()
                        
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                val obj = it.getJSONObject(i)
                                temp.add(NotificationModel(
                                    notifId = obj.optInt("notifId"),
                                    userId = obj.optInt("userId"),
                                    title = obj.optString("title"),
                                    message = obj.optString("message"),
                                    type = obj.optString("type"),
                                    isRead = obj.optInt("isRead", 0),
                                    isCritical = obj.optInt("isCritical", 0),
                                    dataPayload = obj.optJSONObject("dataPayload")?.toString(),
                                    createdAt = obj.optLong("createdAt"),
                                    updatedAt = obj.optLong("updatedAt")
                                ))
                            }
                        }
                        
                        runOnUiThread {
                            notificationList.clear()
                            notificationList.addAll(temp.reversed())
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NOTIF_API", "Error: ${e.message}")
            }
        }.start()
    }
}
