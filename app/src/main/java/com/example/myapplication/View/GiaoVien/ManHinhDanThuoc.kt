package com.example.myapplication.View.GiaoVien

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
import com.example.myapplication.Model.MedicationRequestModel
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.MedicationAdapter

class ManHinhDanThuoc : AppCompatActivity() {
    
    private lateinit var rvMedication: RecyclerView
    private lateinit var adapter: MedicationAdapter
    private lateinit var tvRequestCount: TextView
    private var medicationList = mutableListOf<MedicationRequestModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_dan_thuoc)

        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        tvRequestCount = findViewById(R.id.tvRequestCount)
        rvMedication = findViewById(R.id.rvMedication)
        
        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = MedicationAdapter(medicationList) { item, action, note ->
            handleAction(item, action, note)
        }
        rvMedication.layoutManager = LinearLayoutManager(this)
        rvMedication.adapter = adapter
    }

    private fun loadData() {
        medicationList.clear()
        medicationList.addAll(DataManager.medicationRequests)
        adapter.notifyDataSetChanged()
        updateSummary()
    }

    private fun updateSummary() {
        val pendingCount = medicationList.count { it.Status == "Pending" || it.Status == "Accepted" }
        tvRequestCount.text = "$pendingCount Yêu cầu"
    }

    private fun handleAction(item: MedicationRequestModel, action: String, note: String?) {
        val request = DataManager.medicationRequests.find { it.MedRequestID == item.MedRequestID }
        
        when (action) {
            "ACCEPT" -> {
                request?.Status = "Accepted"
                Toast.makeText(this, "Đã tiếp nhận yêu cầu của bé ${item.studentName}", Toast.LENGTH_SHORT).show()
            }
            "REJECT" -> {
                request?.Status = "Rejected"
                Toast.makeText(this, "Đã từ chối yêu cầu", Toast.LENGTH_SHORT).show()
            }
            "COMPLETE" -> {
                request?.Status = "Completed"
                request?.TeacherNote = note
                Toast.makeText(this, "Xác nhận đã cho bé ${item.studentName} uống thuốc", Toast.LENGTH_SHORT).show()
            }
            "VIEW_IMAGE" -> {
                Toast.makeText(this, "Tính năng xem ảnh đang được phát triển", Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        loadData() // Refresh list
    }
}
