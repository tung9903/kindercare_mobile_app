package com.example.myapplication.View.PhuHuynh

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.NotificationModel
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.NotificationAdapter
import com.google.android.material.button.MaterialButton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class NotificationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private val fullNotificationList = mutableListOf<NotificationModel>()
    private val displayList = mutableListOf<NotificationModel>()
    private lateinit var btnback: ImageView
    private var currentFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        setupTabs()

        btnback.setOnClickListener { finish() }

        fetchNotifications()

        findViewById<TextView>(R.id.tvMarkAllRead).setOnClickListener {
            markAllAsRead()
        }
    }

    private fun initViews() {
        btnback = findViewById(R.id.btnBack)
        recyclerView = findViewById(R.id.recyclerViewNotifications)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(displayList) { selectedItem ->
            // Khi nhấn vào thông báo -> Đánh dấu là đã đọc trên server
            markNotificationAsRead(selectedItem.notifId)
            
            // Điều hướng dựa trên loại thông báo
            when (selectedItem.type?.uppercase()) {
                "MEDICATION" -> {
                    startActivity(Intent(this, MedicationRequestPHActivity::class.java))
                }
                "LEAVE_REQUEST" -> {
                    startActivity(Intent(this, LeaveRequestActivity::class.java))
                }
                "FINANCE", "INVOICE" -> {
                    startActivity(Intent(this, FeeActivity::class.java))
                }
                "STUDENT_ASSESSMENT" -> {
                    startActivity(Intent(this, AssessmentPHActivity::class.java))
                }
                "DAILY_ACTIVITY" -> {
                    // Có thể quay về Home hoặc màn hình chi tiết nếu có
                    finish() 
                }
                else -> {
                    Toast.makeText(this, "Thông báo: ${selectedItem.title}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        recyclerView.adapter = adapter
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
            } catch (e: Exception) {
                Log.e("NOTIF_READ", "Error: ${e.message}")
            }
        }.start()
    }

    private fun setupTabs() {
        val btnAll = findViewById<MaterialButton>(R.id.btnFilterAll)
        val btnUnread = findViewById<MaterialButton>(R.id.btnFilterUnread)
        val btnFinance = findViewById<MaterialButton>(R.id.btnFilterFinance)
        val btnActivity = findViewById<MaterialButton>(R.id.btnFilterActivity)

        val buttons = listOf(btnAll, btnUnread, btnFinance, btnActivity)

        btnAll.setOnClickListener { updateTabSelection("All", buttons) }
        btnUnread.setOnClickListener { updateTabSelection("Unread", buttons) }
        btnFinance.setOnClickListener { updateTabSelection("Finance", buttons) }
        btnActivity.setOnClickListener { updateTabSelection("Activity", buttons) }
    }

    private fun updateTabSelection(filter: String, buttons: List<MaterialButton>) {
        currentFilter = filter
        buttons.forEach { btn ->
            val isSelected = (btn.id == R.id.btnFilterAll && filter == "All") ||
                             (btn.id == R.id.btnFilterUnread && filter == "Unread") ||
                             (btn.id == R.id.btnFilterFinance && filter == "Finance") ||
                             (btn.id == R.id.btnFilterActivity && filter == "Activity")
            
            if (isSelected) {
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_green))
                btn.setTextColor(Color.WHITE)
            } else {
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.border_color))
                btn.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            }
        }
        filterNotifications(filter)
    }

    private fun fetchNotifications() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val client = DataManager.okHttpClient
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/notifications")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    Log.d("NOTIF_API", "Response: $body")
                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val dataArray = jsonResponse.optJSONArray("data")
                        
                        val tempNotis = mutableListOf<NotificationModel>()
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                tempNotis.add(parseNotification(it.getJSONObject(i)))
                            }
                        }

                        runOnUiThread {
                            fullNotificationList.clear()
                            fullNotificationList.addAll(tempNotis)
                            filterNotifications(currentFilter)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NOTIF_API", "Error: ${e.message}")
            }
        }.start()
    }

    private fun parseNotification(obj: JSONObject): NotificationModel {
        return NotificationModel(
            notifId = obj.optInt("notifId"),
            userId = if (obj.isNull("userId")) null else obj.optInt("userId"),
            title = obj.optString("title", "Thông báo"),
            message = obj.optString("message", ""),
            type = obj.optString("type", "SYSTEM"),
            isRead = obj.optInt("isRead", 0),
            isCritical = obj.optInt("isCritical", 0),
            dataPayload = obj.optJSONObject("dataPayload")?.toString(),
            createdAt = obj.optLong("createdAt"),
            updatedAt = obj.optLong("updatedAt")
        )
    }

    private fun filterNotifications(filter: String) {
        currentFilter = filter
        displayList.clear()
        when (filter) {
            "All" -> displayList.addAll(fullNotificationList)
            "Unread" -> displayList.addAll(fullNotificationList.filter { !it.isReadBool() })
            "Finance" -> displayList.addAll(fullNotificationList.filter { it.type == "FINANCE" || it.title.contains("học phí", true) })
            "Activity" -> displayList.addAll(fullNotificationList.filter { it.type == "ACTIVITY" })
        }
        adapter.updateData(displayList)
    }

    private fun markAllAsRead() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        // Gọi API PUT chuẩn Swagger
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
                            // Cập nhật giao diện cục bộ sau khi server xác nhận thành công
                            fullNotificationList.forEach { it.isRead = 1 }
                            filterNotifications(currentFilter)
                            Toast.makeText(this, "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Không thể cập nhật trạng thái", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return@use
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }
}
