package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.TeacherClassResponse
import com.example.myapplication.R
import com.example.myapplication.Utils.NavigationUtils
import com.example.myapplication.View.Adapter.TeacherClassAdapter
import okhttp3.Request
import org.json.JSONObject

class ManHinhDanhSachLop : AppCompatActivity() {
    private lateinit var rvClasses: RecyclerView
    private lateinit var adapter: TeacherClassAdapter
    private val classList = mutableListOf<TeacherClassResponse>()

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

        initViews()
        setupRecyclerView()
        setupSearch()
        setupTopActions()
        setupBottomNavigation()
        
        fetchTeacherClasses()
    }

    private fun initViews() {
        rvClasses = findViewById(R.id.rev_hocsinh) 
        findViewById<EditText>(R.id.etSearchHocSinh).hint = "Tìm kiếm lớp học..."
        
        setupAvatarNavigation()
        setupNotificationNavigation()
    }

    private fun setupRecyclerView() {
        rvClasses.layoutManager = LinearLayoutManager(this)
        adapter = TeacherClassAdapter(classList) { selectedClass ->
            val intent = Intent(this, ManHinhDanhSachHocSinhCuaLop::class.java)
            intent.putExtra("CLASS_ID", selectedClass.classId)
            intent.putExtra("CLASS_NAME", selectedClass.className)
            startActivity(intent)
        }
        rvClasses.adapter = adapter
    }

    private fun setupSearch() {
        findViewById<EditText>(R.id.etSearchHocSinh).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupTopActions() {
        findViewById<ImageView>(R.id.imgCalendar).setOnClickListener {
            Toast.makeText(this, "Tính năng Lịch đang phát triển", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchTeacherClasses() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)

        if (token.isNullOrEmpty()) return

        val url = "https://web-test.kindercare.app/api/v1/teacher/classes"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    Log.d("CLASS_API", "Danh sách lớp trả về: $body")

                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val dataArray = jsonResponse.optJSONArray("data")

                        val tempClasses = mutableListOf<TeacherClassResponse>()
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                val obj = it.getJSONObject(i)
                                tempClasses.add(TeacherClassResponse(
                                    classId = obj.optInt("classId"),
                                    className = obj.optString("className", "Lớp chưa đặt tên"),
                                    studentCount = obj.optInt("studentCount", 0)
                                ))
                            }
                        }

                        runOnUiThread {
                            classList.clear()
                            classList.addAll(tempClasses)
                            adapter.updateData(classList)
                        }
                    } else {
                        Log.e("CLASS_API", "Lỗi mạng hoặc Token: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.e("CLASS_API", "Lỗi kết nối: ${e.message}")
            }
        }.start()
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

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationGV(this, "CLASS_LIST")
    }
}
