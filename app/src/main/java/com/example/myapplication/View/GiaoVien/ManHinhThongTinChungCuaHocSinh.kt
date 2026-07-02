package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.R
import okhttp3.Request
import org.json.JSONObject

class ManHinhThongTinChungCuaHocSinh : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_thong_tin_chung_cua_hoc_sinh)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        val studentId = intent.getIntExtra("STUDENT_ID", -1)
        if (studentId != -1) {
            fetchStudentDetail(studentId)
        }
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
                    if (response.isSuccessful && body != null) {
                        val json = JSONObject(body)
                        val data = json.optJSONObject("data")
                        if (data != null) {
                            runOnUiThread {
                                bindData(data)
                            }
                        }
                    }
                    return@use
                }
            } catch (e: Exception) {
                Log.e("STUDENT_DETAIL", "Error: ${e.message}")
            }
        }.start()
    }

    private fun bindData(data: JSONObject) {
        val fullName = data.optString("fullName", "N/A")
        val dob = data.optLong("dateOfBirth", 0)
        val formattedDob = if (dob > 0) DateHelper.formatLongToDate(dob) else "N/A"
        val nickname = data.optString("nickname", "Chưa có")
        val allergies = data.optString("allergies", "Không")
        val parentName = data.optString("parentName", "Chưa cập nhật")
        val parentPhone = data.optString("parentPhone", "Chưa có SĐT")
        val className = data.optString("className", "Lớp học")

        findViewById<ImageView>(R.id.ivStudentAvatarCard).setImageResource(R.drawable.avatar)
        findViewById<TextView>(R.id.tvStudentNameCard).text = fullName
        findViewById<TextView>(R.id.tvStudentNickNameCard).text = "Biệt danh: $nickname"
        findViewById<TextView>(R.id.tvStudentDobCard).text = "📅 $formattedDob"
        
        val tvAllergy = findViewById<TextView>(R.id.tvStudentAllergyCard)
        if (allergies != "Không" && allergies.isNotEmpty()) {
            tvAllergy.visibility = View.VISIBLE
            tvAllergy.text = "⚠️ $allergies"
        } else {
            tvAllergy.visibility = View.GONE
        }

        findViewById<TextView>(R.id.tvFullName).text = "Họ và tên: $fullName"
        findViewById<TextView>(R.id.tvClass).text = "Lớp: $className"
        findViewById<TextView>(R.id.tvDob).text = "Ngày sinh: $formattedDob"
        findViewById<TextView>(R.id.tvParentName).text = "Phụ huynh: $parentName"
        findViewById<TextView>(R.id.tvHeaderTitle).text = "Hồ sơ của $fullName"

        findViewById<TextView>(R.id.tvContactName).text = parentName
        findViewById<TextView>(R.id.tvContactPhone).text = parentPhone
    }
}
