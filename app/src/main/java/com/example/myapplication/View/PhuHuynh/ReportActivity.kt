package com.example.myapplication.View.PhuHuynh

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.ReportModel
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.ReportAdapter

class ReportActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var reportAdapter: ReportAdapter
    private val reportList = mutableListOf<ReportModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerViewReports)
        reportAdapter = ReportAdapter(reportList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = reportAdapter

        loadMockData()
    }

    private fun loadMockData() {
        reportList.add(ReportModel(1, "Báo cáo tháng 5", "18/05/2026", "Đã hoàn thành"))
        reportList.add(ReportModel(2, "Sự cố y tế", "15/05/2026", "Đang xử lý"))
        reportAdapter.notifyDataSetChanged()
    }
}
