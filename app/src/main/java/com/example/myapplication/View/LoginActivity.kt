package com.example.myapplication.View

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.example.myapplication.View.GiaoVien.ManHinhBangDieuKhien
import com.example.myapplication.View.PhuHuynh.HomePHActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var edtPhoneNumber: EditText
    private lateinit var edtPassword: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var tvForgotPassword: TextView
    private lateinit var btnLogin: Button
    
    // Base URL của Backend (Server Test thực tế)
    private val BASE_URL = "https://web-test.kindercare.app/api/v1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Kiểm tra Auto Login trước khi hiện giao diện
        checkAutoLogin()

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRoleSpinner()
        setupClickListeners()
    }

    private fun checkAutoLogin() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        val role = pref.getString("role", null) // Đây là role từ server đã lưu (parent/teacher)

        if (!token.isNullOrEmpty() && !role.isNullOrEmpty()) {
            val intent = if (role.equals("parent", ignoreCase = true)) {
                Intent(this, HomePHActivity::class.java)
            } else {
                Intent(this, ManHinhBangDieuKhien::class.java)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun initViews() {
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber)
        edtPassword = findViewById(R.id.edtPassword)
        spinnerRole = findViewById(R.id.spinnerRole)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        btnLogin = findViewById(R.id.btnLogin)
    }

    private fun setupRoleSpinner() {
        val roles = arrayOf("Phụ huynh", "Giáo viên")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            handleLogin()
        }
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun handleLogin() {
        val identifier = edtPhoneNumber.text.toString().trim()
        val password = edtPassword.text.toString().trim()
        val selectedRole = spinnerRole.selectedItem.toString()

        if (identifier.isEmpty()) {
            edtPhoneNumber.error = "Vui lòng nhập SĐT hoặc Username"
            return
        }
        if (password.isEmpty()) {
            edtPassword.error = "Mật khẩu không được để trống"
            return
        }

        // Vô hiệu hóa nút để tránh bấm nhiều lần
        btnLogin.isEnabled = false
        btnLogin.text = "Đang xử lý..."

        // Sử dụng Singleton Client từ DataManager để chạy nhanh hơn
        val client = DataManager.okHttpClient

        Thread {
            try {
                // 1. Xác định URL dựa theo Role
                val endpoint = if (selectedRole == "Phụ huynh") "/auth/parent/login" else "/auth/teacher/login"
                
                // 2. Tạo JSON body gửi lên (identifier & password)
                val bodyJson = JSONObject()
                    .put("identifier", identifier)
                    .put("password", password)
                    .toString()

                val jsonMediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = bodyJson.toRequestBody(jsonMediaType)

                val req = Request.Builder()
                    .url(BASE_URL + endpoint)
                    .addHeader("accept", "application/json") // Bổ sung Header Accept giống Curl
                    .post(requestBody)
                    .build()

                Log.d("API_LOGIN", "Connecting to: ${BASE_URL + endpoint}")
                Log.d("API_LOGIN", "Payload: $bodyJson")
                Log.d("API_LOGIN", "Role Selected: $selectedRole")

                // 3. Thực thi request
                client.newCall(req).execute().use { resp ->
                    val body = resp.body?.string().orEmpty()
                    val code = resp.code
                    Log.d("API_LOGIN", "Response Code: $code")
                    Log.d("API_LOGIN", "Response Body: $body")

                    // Xử lý JSON phản hồi
                    val responseJson = try { JSONObject(body) } catch (e: Exception) { JSONObject() }
                    
                    // Cải tiến: Chấp nhận thành công nếu code là 200 hoặc có success=true
                    val isSuccessCode = resp.isSuccessful
                    val successField = responseJson.optBoolean("success", false)
                    val message = responseJson.optString("message", "Lỗi không xác định từ server")

                    runOnUiThread {
                        btnLogin.isEnabled = true
                        btnLogin.text = "Đăng nhập"

                        if (isSuccessCode || successField) {
                            Log.d("API_LOGIN", "Login Successful")
                            val data = responseJson.optJSONObject("data")
                            val token = data?.optString("token")
                            val userObj = data?.optJSONObject("user")
                            
                            // Lấy role từ server trả về (nếu có)
                            val roleFromServer = userObj?.optString("role")
                            
                            val welcomeName = userObj?.optString("fullName") ?: userObj?.optString("username") ?: identifier
                            Toast.makeText(this, "Chào mừng: $welcomeName", Toast.LENGTH_SHORT).show()

                            // Logic chuyển màn hình: Ưu tiên dựa trên lựa chọn Spinner của người dùng
                            val intent = if (selectedRole == "Phụ huynh") {
                                Intent(this, HomePHActivity::class.java)
                            } else {
                                Intent(this, ManHinhBangDieuKhien::class.java)
                            }
                            
                            // Lưu Role chuẩn để Auto-Login (Ưu tiên role server, nếu không có thì lưu theo spinner)
                            val finalRoleToSave = if (roleFromServer != null && roleFromServer.isNotEmpty()) {
                                roleFromServer
                            } else {
                                if (selectedRole == "Phụ huynh") "parent" else "teacher"
                            }
                            
                            getSharedPreferences("KinderCarePref", MODE_PRIVATE).edit()
                                .putString("token", token)
                                .putString("role", finalRoleToSave)
                                .putString("identifier", identifier)
                                .apply()

                            startActivity(intent)
                            finish()
                        } else {
                            // Hiển thị thông báo lỗi cụ thể từ Server hoặc mã lỗi HTTP
                            val errorMsg = if (message.isNotEmpty()) message else "Mã lỗi: $code"
                            Log.e("API_LOGIN", "Login Failed: $errorMsg")
                            Toast.makeText(this, "Đăng nhập thất bại: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("API_LOGIN", "Connection Error: ${e.message}")
                runOnUiThread {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Đăng nhập"
                    Toast.makeText(this, "Không thể kết nối server: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}
