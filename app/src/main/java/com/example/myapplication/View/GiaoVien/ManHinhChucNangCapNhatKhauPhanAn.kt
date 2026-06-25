package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.View.Adapter.StudentMealAdapter
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.Student
import com.example.myapplication.R

class ManHinhChucNangCapNhatKhauPhanAn : AppCompatActivity() {
    private lateinit var rvStudents: RecyclerView
    private lateinit var adapter: StudentMealAdapter
    private var displayList = mutableListOf<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chuc_nang_cap_nhat_khau_phan_an)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val currentMeal = DataManager.getCurrentMeal()
        val currentDate = DataManager.dailyMenu.date
        findViewById<TextView>(R.id.tvMealInfo).text = "${currentMeal.MealType} • Ngày $currentDate"

        displayList.addAll(DataManager.studentList)
        setupRecyclerView()
        setupQuickActions()

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            Toast.makeText(this, "Đã cập nhật ${currentMeal.MealType} và đăng lên bảng tin thành công!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        rvStudents = findViewById(R.id.rvStudents)
        adapter = StudentMealAdapter(displayList)
        rvStudents.layoutManager = LinearLayoutManager(this)
        rvStudents.adapter = adapter
    }

    private fun setupQuickActions() {
        findViewById<Button>(R.id.btnQuickFull).setOnClickListener {
            updateAllAndFilter("FULL")
        }
        findViewById<Button>(R.id.btnQuickHalf).setOnClickListener {
            updateAllAndFilter("HALF")
        }
        findViewById<Button>(R.id.btnQuickLess).setOnClickListener {
            updateAllAndFilter("LESS")
        }
    }

    private fun updateAllAndFilter(status: String) {
        DataManager.studentList.forEach { it.mealStatus = status }
        
        displayList.clear()
        displayList.addAll(DataManager.studentList)
        
        adapter.notifyDataSetChanged()
        
        val statusText = when(status) {
            "FULL" -> "Ăn hết"
            "HALF" -> "Nửa suất"
            else -> "Ăn ít"
        }
        Toast.makeText(this, "Đã cập nhật trạng thái: Tất cả $statusText", Toast.LENGTH_SHORT).show()
    }
}
