package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.TeacherStudentAdapter
import okhttp3.Request
import org.json.JSONObject

class ManHinhDanhSachHocSinhCuaLop : AppCompatActivity() {

    private lateinit var rvStudents: RecyclerView
    private lateinit var adapter: TeacherStudentAdapter
    private val studentList = mutableListOf<JSONObject>()
    private var classId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_danh_sach_hoc_sinh_cua_lop)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        classId = intent.getIntExtra("CLASS_ID", -1)
        val className = intent.getStringExtra("CLASS_NAME") ?: "Danh sách lớp"
        findViewById<TextView>(R.id.tvHeaderTitle).text = className

        initViews()
        setupRecyclerView()
        setupSearch()
        
        fetchStudents()
    }

    private fun initViews() {
        rvStudents = findViewById(R.id.rvStudents)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = TeacherStudentAdapter(studentList) { studentId ->
            val intent = Intent(this, ManHinhChucNangHoSoHocSinh::class.java)
            intent.putExtra("STUDENT_ID", studentId)
            startActivity(intent)
        }
        rvStudents.layoutManager = LinearLayoutManager(this)
        rvStudents.adapter = adapter
    }

    private fun setupSearch() {
        findViewById<EditText>(R.id.etSearchStudent).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun fetchStudents() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty() || classId == -1) return

        // Dùng API lấy danh sách học sinh (không truyền date để lấy hồ sơ tĩnh)
        val url = "https://web-test.kindercare.app/api/v1/teacher/classes/$classId/students"
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
                        
                        val tempStudents = mutableListOf<JSONObject>()
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                tempStudents.add(it.getJSONObject(i))
                            }
                        }

                        runOnUiThread {
                            studentList.clear()
                            studentList.addAll(tempStudents)
                            adapter.updateData(studentList)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("STUDENT_LIST_API", "Error: ${e.message}")
            }
        }.start()
    }
}
