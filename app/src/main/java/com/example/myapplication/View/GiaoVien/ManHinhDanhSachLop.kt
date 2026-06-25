package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.example.myapplication.Utils.NavigationUtils
import com.example.myapplication.View.Adapter.HocSinhAdapter

class ManHinhDanhSachLop : AppCompatActivity() {

    private lateinit var rvHocSinh: RecyclerView
    private lateinit var adapter: HocSinhAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_danh_sach_lop)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupBottomNavigation()
        setupSearch()
        setupNotificationNavigation()
        setupAvatarNavigation()
        setupTopActions()
    }

    private fun setupTopActions() {
        findViewById<View>(R.id.imgCalendar).setOnClickListener {
            Toast.makeText(this, "Tính năng lịch trình đang được phát triển", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.updateData(DataManager.studentList)
    }

    private fun setupAvatarNavigation() {
        findViewById<View>(R.id.imgAvatar).setOnClickListener {
            startActivity(Intent(this, ManHinhThongTinTaiKhoanCaNhan::class.java))
        }
    }

    private fun setupNotificationNavigation() {
        findViewById<View>(R.id.layoutNotification).setOnClickListener {
            startActivity(Intent(this, ManHinhChucNangThongBao::class.java))
        }
    }

    private fun setupRecyclerView() {
        rvHocSinh = findViewById(R.id.rev_hocsinh)
        adapter = HocSinhAdapter(DataManager.studentList)
        rvHocSinh.layoutManager = LinearLayoutManager(this)
        rvHocSinh.adapter = adapter
    }

    private fun setupSearch() {
        val etSearch = findViewById<EditText>(R.id.etSearchHocSinh)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationGV(this, "CLASS_LIST")
    }
}
