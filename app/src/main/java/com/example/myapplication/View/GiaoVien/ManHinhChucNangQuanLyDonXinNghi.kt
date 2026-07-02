package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.graphics.Color
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
import com.example.myapplication.View.Adapter.DuyetDonAdapter
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.LeaveRequestModel
import com.example.myapplication.R
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

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

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        setupRecyclerView()
        setupTabs()
        
        fetchLeaveRequests(currentFilter)
    }

    private fun setupRecyclerView() {
        rvRequests = findViewById(R.id.rv_leave_requests)
        rvRequests.layoutManager = LinearLayoutManager(this)
        
        adapter = DuyetDonAdapter(
            displayList,
            onAction = { request, action ->
                if (action == "APPROVE") {
                    updateRequestStatus(request.requestId, "Approved")
                } else if (action == "REJECT") {
                    updateRequestStatus(request.requestId, "Rejected")
                }
            },
            onItemClick = { request ->
                val intent = Intent(this, ManHinhChiTietDonXinNghi::class.java)
                intent.putExtra("REQUEST_ID", request.requestId)
                startActivity(intent)
            }
        )
        rvRequests.adapter = adapter
    }

    private fun setupTabs() {
        val tabPending = findViewById<TextView>(R.id.tab_pending)
        val tabApproved = findViewById<TextView>(R.id.tab_approved)
        val tabRejected = findViewById<TextView>(R.id.tab_rejected)

        tabPending.setOnClickListener { 
            currentFilter = "Pending"
            fetchLeaveRequests("Pending") 
        }
        tabApproved.setOnClickListener { 
            currentFilter = "Approved"
            fetchLeaveRequests("Approved") 
        }
        tabRejected.setOnClickListener { 
            currentFilter = "Rejected"
            fetchLeaveRequests("Rejected") 
        }
    }

    private fun fetchLeaveRequests(status: String) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        updateTabStyles(status)

        val url = "https://web-test.kindercare.app/api/v1/teacher/leave-requests?status=$status"
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
                        val jsonResponse = JSONObject(body)
                        val dataArray = jsonResponse.optJSONArray("data")
                        
                        val tempList = mutableListOf<LeaveRequestModel>()
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                tempList.add(parseLeaveRequest(it.getJSONObject(i)))
                            }
                        }
                        
                        runOnUiThread {
                            displayList.clear()
                            displayList.addAll(tempList)
                            adapter.updateData(displayList)
                            
                            findViewById<TextView>(R.id.txt_filter_info).text = when(status) {
                                "Pending" -> "Danh sách đơn cần xử lý trong ngày"
                                "Approved" -> "Danh sách đơn đã chấp thuận"
                                else -> "Danh sách đơn đã từ chối"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LEAVE_API_GV", "Lỗi: ${e.message}")
            }
        }.start()
    }

    private fun updateRequestStatus(requestId: Int, newStatus: String) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        Toast.makeText(this, "Đang xử lý yêu cầu...", Toast.LENGTH_SHORT).show()

        val json = JSONObject().apply {
            put("status", newStatus)
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/teacher/leave-requests/$requestId/status")
            .addHeader("Authorization", "Bearer $token")
            .put(body)
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val resBody = response.body?.string()
                    runOnUiThread {
                        if (response.isSuccessful) {
                            val msg = if (newStatus == "Approved") "Đã duyệt đơn thành công!" else "Đã từ chối đơn thành công!"
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                            fetchLeaveRequests(currentFilter) // Tải lại danh sách theo filter hiện tại
                        } else {
                            val msg = JSONObject(resBody ?: "{}").optString("message", "Lỗi cập nhật")
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun parseLeaveRequest(obj: JSONObject): LeaveRequestModel {
        return LeaveRequestModel(
            requestId = obj.optInt("requestId"),
            studentId = obj.optInt("studentId"),
            studentName = obj.optString("studentName"),
            studentAvatar = obj.optString("studentAvatar"),
            className = obj.optString("className"),
            parentId = obj.optInt("parentId"),
            parentName = obj.optString("parentName"),
            fromDate = obj.optLong("fromDate"),
            toDate = obj.optLong("toDate"),
            reason = obj.optString("reason"),
            evidenceUrl = obj.optString("evidenceUrl"),
            status = obj.optString("status"),
            isMealFeeDeducted = obj.optInt("isMealFeeDeducted") == 1,
            parentNotes = obj.optString("parentNotes"),
            createdAt = obj.optLong("createdAt")
        )
    }

    private fun updateTabStyles(status: String) {
        val tabs = listOf(
            findViewById<TextView>(R.id.tab_pending),
            findViewById<TextView>(R.id.tab_approved),
            findViewById<TextView>(R.id.tab_rejected)
        )
        
        val statusList = listOf("Pending", "Approved", "Rejected")
        
        tabs.forEachIndexed { index, textView ->
            if (statusList[index] == status) {
                textView.setBackgroundResource(R.drawable.bg_tab_selected)
                textView.setTextColor(Color.parseColor("#0E7055"))
                textView.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                textView.setBackgroundResource(R.drawable.bg_filter_chip)
                textView.setTextColor(Color.parseColor("#475467"))
                textView.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }
}
