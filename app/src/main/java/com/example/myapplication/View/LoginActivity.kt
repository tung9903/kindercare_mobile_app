package com.example.myapplication.View

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.example.myapplication.View.GiaoVien.ManHinhBangDieuKhien
import com.example.myapplication.View.PhuHuynh.HomePHActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
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
    
    private val BASE_URL = "https://web-test.kindercare.app/api/v1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
        val role = pref.getString("role", null)

        if (!token.isNullOrEmpty() && !role.isNullOrEmpty()) {
            Toast.makeText(this, "Tự động đăng nhập...", Toast.LENGTH_SHORT).show()
            registerFcmToken(token)
            navigateToDashboard(role)
        }
    }

    private fun initViews() {
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber)
        edtPassword = findViewById(R.id.edtPassword)
        spinnerRole = findViewById(R.id.spinnerRole)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        btnLogin = findViewById(R.id.btnLogin)
        
        // Hiện lại spinner để người dùng chọn cổng đăng nhập chính xác
        spinnerRole.visibility = View.VISIBLE
    }

    private fun setupRoleSpinner() {
        val roles = arrayOf("Phụ huynh", "Giáo viên")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener { handleLogin() }
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun handleLogin() {
        val identifier = edtPhoneNumber.text.toString().trim()
        val password = edtPassword.text.toString().trim()
        val selectedRole = spinnerRole.selectedItem.toString()

        // KIỂM TRA ĐIỀU KIỆN ĐẦU VÀO
        if (identifier.isEmpty()) {
            edtPhoneNumber.error = "Vui lòng nhập SĐT hoặc Username"
            edtPhoneNumber.requestFocus()
            return
        }
        if (password.isEmpty()) {
            edtPassword.error = "Vui lòng nhập mật khẩu"
            edtPassword.requestFocus()
            return
        }

        btnLogin.isEnabled = false
        btnLogin.text = "Đang xác thực..."

        Thread {
            try {
                // XÁC ĐỊNH ENDPOINT DỰA TRÊN SWAGGER (Chỉ còn Parent và Teacher)
                val endpoint = if (selectedRole == "Giáo viên") "/auth/teacher/login" else "/auth/parent/login"

                val bodyJson = JSONObject().put("identifier", identifier).put("password", password).toString()
                val requestBody = bodyJson.toRequestBody("application/json; charset=utf-8".toMediaType())

                val req = Request.Builder()
                    .url(BASE_URL + endpoint)
                    .post(requestBody)
                    .build()

                DataManager.okHttpClient.newCall(req).execute().use { resp ->
                    val body = resp.body?.string().orEmpty()
                    val responseJson = try { JSONObject(body) } catch (e: Exception) { JSONObject() }
                    
                    runOnUiThread {
                        btnLogin.isEnabled = true
                        btnLogin.text = "Đăng nhập"

                        if (resp.isSuccessful) {
                            val data = responseJson.optJSONObject("data")
                            val token = data?.optString("token")
                            val userObj = data?.optJSONObject("user")
                            
                            val roleName = userObj?.optString("roleName") ?: if (selectedRole == "Giáo viên") "Teacher" else "Parent"
                            val fullName = userObj?.optString("fullName") ?: identifier

                            getSharedPreferences("KinderCarePref", MODE_PRIVATE).edit()
                                .putString("token", token)
                                .putString("role", roleName)
                                .apply()

                            Toast.makeText(this@LoginActivity, "Đăng nhập thành công! Chào mừng $fullName", Toast.LENGTH_SHORT).show()
                            registerFcmToken(token)
                            navigateToDashboard(roleName)
                        } else {
                            val msg = responseJson.optString("message", "Sai tài khoản hoặc mật khẩu")
                            
                            // HIỂN THỊ LỖI VÀ YÊU CẦU NHẬP LẠI
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                            edtPassword.setText("") // Xóa mật khẩu sai
                            edtPassword.requestFocus() // Đưa con trỏ về ô mật khẩu
                        }
                    }
                    return@use
                }
            } catch (e: Exception) {
                runOnUiThread {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Đăng nhập"
                    Toast.makeText(this, "Lỗi kết nối Server", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun navigateToDashboard(role: String) {
        val intent = if (role.equals("Parent", ignoreCase = true)) {
            Intent(this, HomePHActivity::class.java)
        } else {
            Intent(this, ManHinhBangDieuKhien::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun registerFcmToken(authToken: String?) {
        if (authToken.isNullOrEmpty()) return

        val configRequest = Request.Builder()
            .url("$BASE_URL/notifications/firebase-config")
            .addHeader("Authorization", "Bearer $authToken")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(configRequest).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val data = JSONObject(body).optJSONObject("data")
                        if (data != null) {
                            runOnUiThread { initializeFirebaseDynamically(data, authToken) }
                        }
                    }
                    return@use
                }
            } catch (e: Exception) { }
        }.start()
    }

    private fun initializeFirebaseDynamically(data: JSONObject, authToken: String) {
        try {
            val options = FirebaseOptions.Builder()
                .setApiKey(data.getString("apiKey"))
                .setApplicationId(data.getString("appId"))
                .setProjectId(data.getString("projectId"))
                .setStorageBucket(data.getString("storageBucket"))
                .setGcmSenderId(data.getString("messagingSenderId"))
                .build()

            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this, options)
            }
            requestAndRegisterToken(authToken)
        } catch (e: Exception) { }
    }

    private fun requestAndRegisterToken(authToken: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val fcmToken = task.result
            val json = JSONObject().apply { put("token", fcmToken); put("deviceType", "android") }
            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL/notifications/register-token")
                .addHeader("Authorization", "Bearer $authToken")
                .post(body).build()

            Thread { try { DataManager.okHttpClient.newCall(request).execute() } catch (e: Exception) { } }.start()
        }
    }
}
