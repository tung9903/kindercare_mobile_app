package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.R
import okhttp3.Request
import org.json.JSONObject

class ManHinhChucNangHoSoHocSinh : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chuc_nang_ho_so_hoc_sinh)
        
        val root = findViewById<android.view.View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val studentId = intent.getIntExtra("STUDENT_ID", -1)
        
        findViewById<TextView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        if (studentId != -1) {
            fetchStudentDetail(studentId)
        }
        
        setupButtons(studentId)
    }

    private fun fetchStudentDetail(studentId: Int) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val url = "https://web-test.kindercare.app/api/v1/teacher/students/$studentId"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    Log.d("STUDENT_DETAIL", "Response: $body")
                    if (response.isSuccessful && body != null) {
                        val json = JSONObject(body)
                        val data = json.optJSONObject("data")
                        if (data != null) {
                            runOnUiThread { bindData(data) }
                        }
                    } else {
                        Log.e("STUDENT_DETAIL", "Failed: ${response.code}")
                    }
                    return@use
                }
            } catch (e: Exception) {
                Log.e("STUDENT_DETAIL", "Error: ${e.message}")
            }
        }.start()
    }

    private fun bindData(data: JSONObject) {
        findViewById<ImageView>(R.id.ivAvatarProfile).setImageResource(R.drawable.avatar)
        
        findViewById<TextView>(R.id.tvTenProfile).text = data.optString("fullName", "N/A")
        findViewById<TextView>(R.id.tvStudentIdProfile).text = "ID: KC-" + data.optInt("studentId")
        findViewById<TextView>(R.id.tvClassProfile).text = data.optString("className", "Lớp học")
        
        val dob = data.optLong("dateOfBirth", 0)
        val dobText = if (dob > 0) DateHelper.formatLongToDate(dob) else "N/A"
        findViewById<TextView>(R.id.tvNgaySinhProfile).text = dobText
        
        findViewById<TextView>(R.id.tvThongTinPhuProfile).text = data.optString("gender", "N/A")
        
        findViewById<TextView>(R.id.tvTenPhuHuynhProfile).text = data.optString("parentName", "Chưa cập nhật")
        findViewById<TextView>(R.id.tvSdtPhuHuynhProfile).text = data.optString("parentPhone", "Chưa có SĐT")
        
        val allergies = data.optString("allergies", "Không có")
        findViewById<TextView>(R.id.tvDiUngProfile).text = allergies

        val status = data.optString("enrollmentStatus", "Active")
        val statusText = if (status == "Active") "Đang học" else "Ngưng học"
        findViewById<TextView>(R.id.tvStatusProfile).text = statusText
    }

    private fun setupButtons(studentId: Int) {
        findViewById<AppCompatButton>(R.id.btn_general_info).setOnClickListener {
            val intent = Intent(this, ManHinhThongTinChungCuaHocSinh::class.java)
            intent.putExtra("STUDENT_ID", studentId)
            startActivity(intent)
        }

        findViewById<AppCompatButton>(R.id.btn_health_height).setOnClickListener {
            val intent = Intent(this, ManHinhChucNangSucKhoeVaChieuCao::class.java)
            intent.putExtra("STUDENT_ID", studentId)
            startActivity(intent)
        }

        findViewById<AppCompatButton>(R.id.btn_reviews).setOnClickListener {
            val intent = Intent(this, ManHinhNhanXetVaDanhGia::class.java)
            intent.putExtra("STUDENT_ID", studentId)
            startActivity(intent)
        }

        findViewById<AppCompatButton>(R.id.btn_tuition).setOnClickListener {
            val intent = Intent(this, ManHinhChucNangHocPhi::class.java)
            intent.putExtra("STUDENT_ID", studentId)
            startActivity(intent)
        }
    }
}
