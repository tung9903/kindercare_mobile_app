package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.example.myapplication.Utils.NavigationUtils
import com.example.myapplication.View.Adapter.InboxAdapter
import okhttp3.Request
import org.json.JSONObject

class ManHinhHomThuGiaoVien : AppCompatActivity() {

    private lateinit var rvInbox: RecyclerView
    private lateinit var adapter: InboxAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var allMessages = mutableListOf<JSONObject>()
    private var currentFilter = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_hom_thu_giao_vien)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupBottomNavigation()
        setupFilters()
        
        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener { fetchAllInboxData() }

        addDummyData()
        fetchAllInboxData()
    }

    private fun addDummyData() {
        allMessages.add(JSONObject().apply {
            put("type", "MEDICINE")
            put("senderName", "Phụ huynh bé Minh")
            put("medicineDetails", "Siro ho Prospan")
            put("parentNote", "Nhờ cô cho bé uống sau ăn trưa")
            put("createdAt", System.currentTimeMillis() / 1000)
            put("isRead", 0)
        })
        allMessages.add(JSONObject().apply {
            put("type", "LEAVE")
            put("senderName", "Mẹ bé Vy")
            put("reason", "Bé bị sốt nhẹ")
            put("parentNote", "Gia đình xin cho bé nghỉ 2 ngày")
            put("createdAt", (System.currentTimeMillis() / 1000) - 3600)
            put("isRead", 1)
        })
        filterAndDisplay()
    }

    private fun setupRecyclerView() {
        rvInbox = findViewById(R.id.rvInbox)
        adapter = InboxAdapter(allMessages) { item ->
            // Navigate to detail based on type
            val type = item.optString("type")
            when(type) {
                "MEDICINE" -> {
                    val intent = Intent(this, ManHinhDanThuoc::class.java)
                    startActivity(intent)
                }
                "LEAVE" -> {
                    val intent = Intent(this, ManHinhChucNangQuanLyDonXinNghi::class.java)
                    startActivity(intent)
                }
            }
        }
        rvInbox.layoutManager = LinearLayoutManager(this)
        rvInbox.adapter = adapter
    }

    private fun setupFilters() {
        val tabAll = findViewById<TextView>(R.id.tabAll)
        val tabMed = findViewById<TextView>(R.id.tabMedicine)
        val tabLeave = findViewById<TextView>(R.id.tabLeave)
        val tabFeedback = findViewById<TextView>(R.id.tabFeedback)
        
        val tabs = listOf(tabAll, tabMed, tabLeave, tabFeedback)
        
        tabAll.setOnClickListener { updateFilter("ALL", tabs, it) }
        tabMed.setOnClickListener { updateFilter("MEDICINE", tabs, it) }
        tabLeave.setOnClickListener { updateFilter("LEAVE", tabs, it) }
        tabFeedback.setOnClickListener { updateFilter("FEEDBACK", tabs, it) }
    }

    private fun updateFilter(type: String, allTabs: List<TextView>, activeTab: View) {
        currentFilter = type
        allTabs.forEach {
            it.setBackgroundResource(R.drawable.bg_chip_unselected)
            it.setTextColor(Color.parseColor("#64748B"))
        }
        activeTab.setBackgroundResource(R.drawable.bg_chip_selected)
        (activeTab as TextView).setTextColor(Color.WHITE)
        
        filterAndDisplay()
    }

    private fun filterAndDisplay() {
        val filtered = if (currentFilter == "ALL") {
            allMessages
        } else {
            allMessages.filter { it.optString("type") == currentFilter }
        }
        adapter.updateData(filtered)
    }

    private fun fetchAllInboxData() {
        swipeRefresh.isRefreshing = true
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        // In a real Gmail-like system, there would be a single /inbox API.
        // For now, we simulate by fetching medicine and leave requests.
        val url = "https://web-test.kindercare.app/api/v1/teacher/inbox"
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
                        val temp = mutableListOf<JSONObject>()
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                temp.add(it.getJSONObject(i))
                            }
                        }
                        runOnUiThread {
                            allMessages.clear()
                            allMessages.addAll(temp)
                            filterAndDisplay()
                            swipeRefresh.isRefreshing = false
                        }
                    } else {
                        runOnUiThread { swipeRefresh.isRefreshing = false }
                    }
                }
            } catch (e: Exception) {
                Log.e("INBOX", "Error: ${e.message}")
                runOnUiThread { swipeRefresh.isRefreshing = false }
            }
        }.start()
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationGV(this, "INBOX")
    }
}
