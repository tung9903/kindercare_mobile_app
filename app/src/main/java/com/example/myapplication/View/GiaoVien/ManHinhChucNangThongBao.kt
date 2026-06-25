package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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
import com.example.myapplication.Model.NotificationModel
import com.example.myapplication.Model.DataManager
import com.example.myapplication.View.Adapter.NotificationGVAdapter
import com.example.myapplication.R

class ManHinhChucNangThongBao : AppCompatActivity() {

    private lateinit var rcvNotification: RecyclerView
    private lateinit var notiAdapter: NotificationGVAdapter
    private var displayList = mutableListOf<NotificationModel>()
    private var currentTab = "MANAGEMENT"

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

        setupRecyclerView()
        setupTabs()
        setupActions()
    }

    override fun onResume() {
        super.onResume()
        filterNotifications(currentTab)
    }

    private fun setupRecyclerView() {
        rcvNotification = findViewById(R.id.rcv_notifications)
        rcvNotification.layoutManager = LinearLayoutManager(this)
        
        notiAdapter = NotificationGVAdapter(displayList) { clickedItem ->
            val intent = Intent(this, ManHinhChiTietThongBao::class.java)
            intent.putExtra("NOTI_ID", clickedItem.NotifID)
            startActivity(intent)
        }
        rcvNotification.adapter = notiAdapter
    }

    private fun setupTabs() {
        val layoutSystem = findViewById<View>(R.id.layout_tab_system)
        val layoutManagement = findViewById<View>(R.id.layout_tab_management)
        val layoutParents = findViewById<View>(R.id.layout_tab_parents)

        layoutSystem.setOnClickListener { filterNotifications("SYSTEM") }
        layoutManagement.setOnClickListener { filterNotifications("MANAGEMENT") }
        layoutParents.setOnClickListener { filterNotifications("PARENTS") }
    }

    private fun filterNotifications(type: String) {
        currentTab = type
        displayList.clear()
        displayList.addAll(DataManager.allNotifications.filter { it.Type == type })
        notiAdapter.notifyDataSetChanged()
        
        updateTabUI(type)
    }

    private fun updateTabUI(selectedType: String) {
        val tabSystem = findViewById<View>(R.id.layout_tab_system)
        val tabManagement = findViewById<View>(R.id.layout_tab_management)
        val tabParents = findViewById<View>(R.id.layout_tab_parents)

        val txtSystem = findViewById<TextView>(R.id.txt_tab_system)
        val txtManagement = findViewById<TextView>(R.id.txt_tab_management)
        val txtParents = findViewById<TextView>(R.id.txt_tab_parents)

        val activeBg = R.drawable.bg_tab_selected
        val inactiveTextColor = Color.parseColor("#475467")
        val activeTextColor = Color.parseColor("#0E7055")

        listOf(tabSystem, tabManagement, tabParents).forEach { it.setBackgroundResource(0) }
        listOf(txtSystem, txtManagement, txtParents).forEach { 
            it.setTextColor(inactiveTextColor)
            it.setTypeface(null, Typeface.NORMAL)
        }

        when (selectedType) {
            "SYSTEM" -> {
                tabSystem.setBackgroundResource(activeBg)
                txtSystem.setTextColor(activeTextColor)
                txtSystem.setTypeface(null, Typeface.BOLD)
            }
            "MANAGEMENT" -> {
                tabManagement.setBackgroundResource(activeBg)
                txtManagement.setTextColor(activeTextColor)
                txtManagement.setTypeface(null, Typeface.BOLD)
            }
            "PARENTS" -> {
                tabParents.setBackgroundResource(activeBg)
                txtParents.setTextColor(activeTextColor)
                txtParents.setTypeface(null, Typeface.BOLD)
            }
        }
    }

    private fun setupActions() {
        findViewById<TextView>(R.id.btn_mark_all_read).setOnClickListener {
            DataManager.allNotifications.forEach { it.IsRead = true }
            filterNotifications(currentTab)
            Toast.makeText(this, "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<TextView>(R.id.btn_back).setOnClickListener { finish() }
        
        findViewById<View>(R.id.btn_view_history).setOnClickListener {
            Toast.makeText(this, "Lịch sử thông báo đang được đồng bộ...", Toast.LENGTH_SHORT).show()
        }
    }
}
