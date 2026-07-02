package com.example.myapplication.View.PhuHuynh

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.example.myapplication.View.LoginActivity
import android.util.Log
import com.bumptech.glide.Glide
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvFullname: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvIdCard: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvStudentName: TextView
    private lateinit var tvStudentClass: TextView
    private lateinit var tvStudentSchool: TextView
    private lateinit var imgAvatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupClickListeners()
        fetchParentProfile()
        fetchStudentInfo() 
    }

    private fun initViews() {
        tvFullname = findViewById(R.id.txt_fullname_profile)
        tvPhone = findViewById(R.id.tvPhone)
        tvEmail = findViewById(R.id.tvEmail)
        tvAddress = findViewById(R.id.tvAddress)
        tvIdCard = findViewById(R.id.tvIdCard)
        tvRole = findViewById(R.id.tvRole)
        tvStatus = findViewById(R.id.tvStatus)
        tvStudentName = findViewById(R.id.tvStudentName)
        tvStudentClass = findViewById(R.id.tvStudentClass)
        tvStudentSchool = findViewById(R.id.tvStudentSchool)
        imgAvatar = findViewById(R.id.imgAvatar)
    }

    private fun setupClickListeners() {
        val btnBack = findViewById<ImageView>(R.id.btnBack_profile)
        val btnUpdateInfo = findViewById<Button>(R.id.btnUpdateInfo)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnBack.setOnClickListener {
            finish()
        }

        btnUpdateInfo.setOnClickListener {
            startActivity(Intent(this, UpdateInfoActivity::class.java))
        }

        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        btnLogout.setOnClickListener {
            handleLogout()
        }
    }

    private fun fetchParentProfile() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)

        if (token.isNullOrEmpty()) return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/profile")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    Log.d("PROFILE_API", "Response: $body")
                    
                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val data = jsonResponse.optJSONObject("data")
                        
                        runOnUiThread {
                            if (data != null) {
                                tvFullname.text = data.optString("fullName", "Phụ huynh")
                                tvPhone.text = data.optString("phoneNumber", "N/A")
                                tvEmail.text = data.optString("email", "Chưa cập nhật")
                                tvAddress.text = data.optString("address", "Chưa cập nhật")
                                
                                val idCard = data.optString("idCard", "null")
                                tvIdCard.text = "CCCD: " + if (idCard == "null") "Chưa cập nhật" else idCard
                                
                                val job = data.optString("job", "N/A")
                                tvRole.text = "Nghề nghiệp: $job"
                                
                                // Mặc định role là phụ huynh nếu ko có roleName trả về
                                val role = data.optString("roleName", "Phụ huynh")
                                tvStatus.text = "Vai trò: $role"

                                val avatarUrl = data.optString("avatarUrl", "")
                                if (avatarUrl.isNotEmpty() && avatarUrl != "null") {
                                    Glide.with(this)
                                        .load(avatarUrl)
                                        .placeholder(R.drawable.avatar)
                                        .into(imgAvatar)
                                }
                            }
                        }
                    }
                    return@use
                }
            } catch (e: Exception) {
                Log.e("PROFILE_API", "Error: ${e.message}")
            }
        }.start()
    }

    private fun fetchStudentInfo() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children")
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
                        
                        if (dataArray != null && dataArray.length() > 0) {
                            val firstChild = dataArray.getJSONObject(0)
                            runOnUiThread {
                                tvStudentName.text = firstChild.optString("fullName", "N/A")
                                tvStudentClass.text = "Lớp: " + firstChild.optString("className", "Chưa xếp lớp")
                                tvStudentSchool.text = firstChild.optString("campusName", "KinderCare Campus")
                            }
                        }
                    }
                    return@use
                }
            } catch (e: Exception) {
                Log.e("STUDENT_API", "Error: ${e.message}")
            }
        }.start()
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
                } catch (e: Exception) { }
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
}
