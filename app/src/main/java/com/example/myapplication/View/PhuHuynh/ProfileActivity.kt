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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvFullname: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvRole: TextView
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
        fetchStudentInfo() // Gọi thêm API lấy thông tin con cái để hiển thị
    }

    private fun initViews() {
        tvFullname = findViewById(R.id.txt_fullname_profile)
        tvPhone = findViewById(R.id.tvPhone)
        tvEmail = findViewById(R.id.tvEmail)
        tvAddress = findViewById(R.id.tvAddress)
        tvStatus = findViewById(R.id.tvStatus)
        tvRole = findViewById(R.id.tvRole)
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

        // Sự kiện Quay lại
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Sự kiện Cập nhật thông tin
        btnUpdateInfo.setOnClickListener {
            startActivity(Intent(this, UpdateInfoActivity::class.java))
        }

        // Sự kiện Đổi mật khẩu
        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        // Sự kiện Đăng xuất khỏi hệ thống
        btnLogout.setOnClickListener {
            handleLogout()
        }
    }

    private fun fetchParentProfile() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)

        // Ghi log để kiểm tra Token
        Log.d("PROFILE_API", "Token hiện tại: $token")

        // Nếu không có token, không làm gì cả
        if (token.isNullOrEmpty()) {
            Log.e("PROFILE_API", "Lỗi: Không tìm thấy Token. Vui lòng đăng nhập lại.")
            return
        }

        val client = DataManager.okHttpClient

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/profile")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    Log.d("PROFILE_API", "Kết quả trả về: $body")
                    
                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val data = jsonResponse.optJSONObject("data")
                        
                        runOnUiThread {
                            if (data != null) {
                                Log.d("PROFILE_API", "Đang đổ dữ liệu lên giao diện...")
                                
                                // Khớp chính xác với các key từ API của bạn
                                tvFullname.text = data.optString("fullName", "N/A")
                                tvPhone.text = data.optString("phoneNumber", "N/A")
                                tvEmail.text = data.optString("email", "Chưa cập nhật email")
                                tvAddress.text = data.optString("address", "Chưa cập nhật địa chỉ")
                                
                                // Hiển thị thêm công việc nếu cần (tạm thời gộp vào vai trò hoặc log)
                                val job = data.optString("job", "N/A")
                                val role = data.optString("roleName", "Phụ huynh")
                                tvRole.text = "Vai trò: $role ($job)"

                                val status = data.optString("status", "Đang hoạt động")
                                tvStatus.text = "Trạng thái: $status"

                                // Xử lý Avatar URL
                                val avatarUrl = data.optString("avatarUrl", "")
                                if (avatarUrl.isNotEmpty()) {
                                    Log.d("PROFILE_API", "Tìm thấy URL ảnh: $avatarUrl")
                                    // Ghi chú: Cần thêm thư viện Glide hoặc Coil để tải ảnh từ URL này
                                }
                            } else {
                                Log.e("PROFILE_API", "Lỗi: Đối tượng 'data' trong JSON bị null")
                            }
                        }
                    } else {
                        Log.e("PROFILE_API", "Lỗi HTTP: ${response.code} - ${response.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("PROFILE_API", "Ngoại lệ (Exception): ${e.message}")
                e.printStackTrace()
            }
        }.start()
    }

    private fun fetchStudentInfo() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)

        if (token.isNullOrEmpty()) return

        val client = DataManager.okHttpClient

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    Log.d("STUDENT_API", "Kết quả trả về: $body")
                    
                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val dataArray = jsonResponse.optJSONArray("data")
                        
                        if (dataArray != null && dataArray.length() > 0) {
                            val firstChild = dataArray.getJSONObject(0) // Lấy thông tin bé đầu tiên
                            Log.d("STUDENT_API", "Đang đổ dữ liệu bé: ${firstChild.optString("fullName")}")
                            runOnUiThread {
                                tvStudentName.text = firstChild.optString("fullName", "N/A")
                                tvStudentClass.text = firstChild.optString("className", "Chưa xếp lớp")
                                tvStudentSchool.text = firstChild.optString("campusName", "Trường mầm non KinderCare")
                            }
                        } else {
                            Log.e("STUDENT_API", "Lỗi: Danh sách học sinh trống hoặc null")
                        }
                    } else {
                        Log.e("STUDENT_API", "Lỗi HTTP: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.e("STUDENT_API", "Ngoại lệ (Exception): ${e.message}")
                e.printStackTrace()
            }
        }.start()
    }

    private fun handleLogout() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)

        if (token.isNullOrEmpty()) {
            clearLocalDataAndGoToLogin()
            return
        }

        Toast.makeText(this, "Đang xử lý đăng xuất...", Toast.LENGTH_SHORT).show()

        val client = DataManager.okHttpClient
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/auth/logout")
            .post("{}".toRequestBody("application/json".toMediaType())) // POST không body
            .addHeader("Authorization", "Bearer $token")
            .addHeader("accept", "application/json")
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    // Dù thành công hay thất bại trên server, ta vẫn nên đăng xuất ở local
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show()
                        }
                        clearLocalDataAndGoToLogin()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    clearLocalDataAndGoToLogin()
                }
            }
        }.start()
    }

    private fun clearLocalDataAndGoToLogin() {
        // Xóa sạch dữ liệu đã lưu
        getSharedPreferences("KinderCarePref", MODE_PRIVATE).edit().clear().apply()

        // Chuyển về màn hình đăng nhập và xóa stack các màn hình trước đó
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
