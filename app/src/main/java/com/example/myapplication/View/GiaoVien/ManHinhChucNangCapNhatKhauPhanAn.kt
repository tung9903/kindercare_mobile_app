package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.View.Adapter.StudentActivityLogAdapter
import com.example.myapplication.Model.*
import com.example.myapplication.R
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class ManHinhChucNangCapNhatKhauPhanAn : AppCompatActivity() {
    private lateinit var rvStudents: RecyclerView
    private lateinit var adapter: StudentActivityLogAdapter
    private val studentList = mutableListOf<TeacherStudentResponse>()

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

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        setupRecyclerView()

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis / 1000
        
        fetchStudentsForActivityLog(1, today)

        findViewById<View>(R.id.btnSave).setOnClickListener {
            saveDailyActivities(1, today)
        }
    }

    private fun setupRecyclerView() {
        rvStudents = findViewById(R.id.rvStudents)
        adapter = StudentActivityLogAdapter(studentList) { studentId, type ->
            // Logic xoay vòng trạng thái khi click vào các ô 5 sao
            Toast.makeText(this, "Đang cập nhật trạng thái...", Toast.LENGTH_SHORT).show()
        }
        rvStudents.layoutManager = LinearLayoutManager(this)
        rvStudents.adapter = adapter
    }

    private fun saveDailyActivities(classId: Int, date: Long) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        // Chuẩn bị dữ liệu theo API POST /teacher/attendance/meals
        val json = JSONObject().apply {
            put("classId", classId)
            put("date", date)
            
            val array = JSONArray()
            studentList.forEach { s ->
                val obj = JSONObject().apply {
                    put("studentId", s.studentId)
                    // eatingStatus có thể là "Ăn hết", "Ăn một nửa", "Ăn ít" tùy thuộc vào UI
                    // Ở đây mặc định là "Ăn hết" như trong Example của bạn
                    put("eatingStatus", "Ăn hết") 
                }
                array.put(obj)
            }
            put("mealData", array)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/teacher/attendance/meals")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Đã lưu nhật ký sinh hoạt của lớp!", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Lỗi khi lưu dữ liệu", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) { }
        }.start()
    }

    private fun fetchStudentsForActivityLog(classId: Int, date: Long) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val url = "https://web-test.kindercare.app/api/v1/teacher/classes/$classId/students?date=$date"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get().build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val dataArray = jsonResponse.optJSONArray("data")
                        val temp = mutableListOf<TeacherStudentResponse>()
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                temp.add(parseStudent(it.getJSONObject(i)))
                            }
                        }
                        runOnUiThread {
                            studentList.clear()
                            studentList.addAll(temp)
                            adapter.updateData(studentList)
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    private fun parseStudent(obj: JSONObject): TeacherStudentResponse {
        return TeacherStudentResponse(
            studentId = obj.optInt("studentId"),
            fullName = obj.optString("fullName"),
            avatarUrl = obj.optString("avatarUrl"),
            status = obj.optString("status"),
            checkInTime = if (obj.isNull("checkInTime")) null else obj.optLong("checkInTime"),
            checkOutTime = if (obj.isNull("checkOutTime")) null else obj.optLong("checkOutTime"),
            pickedUpBy = if (obj.isNull("pickedUpBy")) null else obj.optString("pickedUpBy"),
            healthNote = obj.optString("healthNote"),
            leaveRequest = null
        )
    }
}
