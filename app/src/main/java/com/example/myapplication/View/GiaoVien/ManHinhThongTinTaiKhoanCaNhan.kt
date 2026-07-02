package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.R
import com.example.myapplication.View.LoginActivity
import com.google.android.material.button.MaterialButton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ManHinhThongTinTaiKhoanCaNhan : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_thong_tin_tai_khoan_ca_nhan)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnUpdateProfile).setOnClickListener {
            startActivity(Intent(this, ManHinhChinhSuaThongTinCaNhan::class.java))
        }

        findViewById<MaterialButton>(R.id.btnChangePassword).setOnClickListener {
            startActivity(Intent(this, ManHinhChucNangThayDoiMatKhau::class.java))
        }

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            handleLogout()
        }

        fetchTeacherProfile()
    }

    private fun handleLogout() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)

        clearLocalDataAndGoToLogin()

        if (!token.isNullOrEmpty()) {
            val request = Request.Builder()
                .url("https://web-test.kindercare.app/api/v1/auth/logout")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $token")
                .build()

            Thread {
                try {
                    DataManager.okHttpClient.newCall(request).execute()
                } catch (e: Exception) {
                    Log.e("LOGOUT", "Lỗi logout ngầm: ${e.message}")
                }
            }.start()
        }
    }

    private fun clearLocalDataAndGoToLogin() {
        getSharedPreferences("KinderCarePref", MODE_PRIVATE).edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        fetchTeacherProfile()
    }

    private fun fetchTeacherProfile() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)

        if (token.isNullOrEmpty()) {
            Log.e("TEACHER_PROFILE_API", "Lỗi: Không tìm thấy Token.")
            return
        }

        val url = "https://web-test.kindercare.app/api/v1/teacher/profile"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    Log.d("TEACHER_PROFILE_API", "Kết quả Profile: $body")

                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val data = jsonResponse.optJSONObject("data")

                        runOnUiThread {
                            if (data != null) {
                                Log.d("TEACHER_PROFILE_API", "Đang cập nhật UI...")
                                
                                // Ánh xạ 12 trường dữ liệu từ API
                                findViewById<TextView>(R.id.tvProfileName).text = data.optString("fullName", "N/A")
                                findViewById<TextView>(R.id.tvProfilePositionSummary).text = data.optString("professionalRank", "Giáo viên")
                                findViewById<TextView>(R.id.tvProfilePhone).text = data.optString("phoneNumber", "N/A")
                                findViewById<TextView>(R.id.tvProfilePositionDetail).text = "Chức danh: " + data.optString("professionalRank", "N/A")
                                findViewById<TextView>(R.id.tvProfileEmail).text = data.optString("email", "N/A")
                                findViewById<TextView>(R.id.tvProfileAddress).text = data.optString("address", "N/A")
                                
                                val avatarUrl = data.optString("avatarUrl")
                                if (avatarUrl.isNotEmpty() && avatarUrl != "null") {
                                    Glide.with(this@ManHinhThongTinTaiKhoanCaNhan)
                                        .load(avatarUrl)
                                        .placeholder(R.drawable.avatar)
                                        .into(findViewById(R.id.imgProfileAvatar))
                                }

                                // Các trường mới bổ sung
                                val dob = data.optLong("dateOfBirth", 0)
                                findViewById<TextView>(R.id.tvProfileDob).text = "Ngày sinh: " + (if (dob > 0) DateHelper.formatLongToDate(dob) else "N/A")
                                
                                val gender = data.optString("gender", "N/A")
                                findViewById<TextView>(R.id.tvProfileGender).text = "Giới tính: " + (if (gender == "Female") "Nữ" else if (gender == "Male") "Nam" else gender)
                                
                                findViewById<TextView>(R.id.tvProfileIdCard).text = "Số CCCD: " + data.optString("idCard", "Chưa cập nhật")
                                
                                val status = data.optString("workStatus", "Active")
                                findViewById<TextView>(R.id.tvProfileStatus).text = "Trạng thái: " + (if (status == "Active") "Đang hoạt động" else status)
                                
                                findViewById<TextView>(R.id.tvProfileBio).text = data.optString("bio", "Chưa có tiểu sử")
                            }
                        }
                    } else {
                        Log.e("TEACHER_PROFILE_API", "Lỗi HTTP: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.e("TEACHER_PROFILE_API", "Ngoại lệ: ${e.message}")
            }
        }.start()
    }
}
