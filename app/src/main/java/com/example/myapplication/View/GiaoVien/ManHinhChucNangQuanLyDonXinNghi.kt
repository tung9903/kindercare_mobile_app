package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.View.Adapter.DuyetDonAdapter
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.LeaveRequestModel
import com.example.myapplication.R

class ManHinhChucNangQuanLyDonXinNghi : AppCompatActivity() {

    private lateinit var rvRequests: RecyclerView
    private lateinit var adapter: DuyetDonAdapter
    
    private var displayList = mutableListOf<LeaveRequestModel>()
    private var currentFilter = "Pending"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chuc_nang_quan_ly_don_xin_nghi)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<LinearLayout>(R.id.btn_back).setOnClickListener { finish() }

        setupRecyclerView()
        setupTabs()
        
        filterByStatus("Pending")
    }

    override fun onResume() {
        super.onResume()
        filterByStatus(currentFilter)
    }

    private fun setupRecyclerView() {
        rvRequests = findViewById(R.id.rv_leave_requests)
        rvRequests.layoutManager = LinearLayoutManager(this)
        
        adapter = DuyetDonAdapter(
            displayList,
            onAction = { request, action ->
                if (action == "APPROVE") {
                    DataManager.approveRequest(request.RequestID)
                    Toast.makeText(this, "Đã duyệt đơn của ${request.studentName}", Toast.LENGTH_SHORT).show()
                } else {
                    DataManager.rejectRequest(request.RequestID)
                    Toast.makeText(this, "Đã từ chối đơn của ${request.studentName}", Toast.LENGTH_SHORT).show()
                }
                filterByStatus(currentFilter)
            },
            onItemClick = { request ->
                val intent = Intent(this, ManHinhChiTietDonXinNghi::class.java)
                intent.putExtra("REQUEST_ID", request.RequestID)
                startActivity(intent)
            }
        )
        rvRequests.adapter = adapter
    }

    private fun setupTabs() {
        val tabPending = findViewById<TextView>(R.id.tab_pending)
        val tabApproved = findViewById<TextView>(R.id.tab_approved)
        val tabRejected = findViewById<TextView>(R.id.tab_rejected)

        tabPending.setOnClickListener { filterByStatus("Pending") }
        tabApproved.setOnClickListener { filterByStatus("Approved") }
        tabRejected.setOnClickListener { filterByStatus("Rejected") }
    }

    private fun filterByStatus(status: String) {
        currentFilter = status
        displayList.clear()
        val filtered = DataManager.leaveRequests.filter { 
            it.Status.equals(status, ignoreCase = true) 
        }
        displayList.addAll(filtered)
        adapter.updateData(displayList)
        
        updateTabStyles(status)
    }

    private fun updateTabStyles(status: String) {
        val tabPending = findViewById<TextView>(R.id.tab_pending)
        val tabApproved = findViewById<TextView>(R.id.tab_approved)
        val tabRejected = findViewById<TextView>(R.id.tab_rejected)
        
        val activeBg = R.drawable.bg_tab_selected
        val inactiveBg = R.drawable.bg_filter_chip
        
        tabPending.setBackgroundResource(if (status == "Pending") activeBg else inactiveBg)
        tabApproved.setBackgroundResource(if (status == "Approved") activeBg else inactiveBg)
        tabRejected.setBackgroundResource(if (status == "Rejected") activeBg else inactiveBg)
    }
}
