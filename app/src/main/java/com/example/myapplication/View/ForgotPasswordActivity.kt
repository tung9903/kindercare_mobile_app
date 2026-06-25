package com.example.myapplication.View

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var edtEmail: EditText
    private lateinit var btnResetRequest: Button
    private lateinit var tvBackToLogin: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        edtEmail = findViewById(R.id.edtEmail)
        btnResetRequest = findViewById(R.id.btnResetRequest)
        tvBackToLogin = findViewById(R.id.btn_BackToLogin)
    }

    private fun setupClickListeners() {
        // bấm nút "Gửi yêu cầu reset"
        btnResetRequest.setOnClickListener {
            handleResetPassword()
        }

        // bấm nút "Quay lại đăng nhập"
        tvBackToLogin.setOnClickListener {
            // Hàm đóng Activity hiện tại để quay về màn hình trước đó (LoginActivity)
            finish()
        }
    }

    private fun handleResetPassword() {
        val email = edtEmail.text.toString().trim()

        // Kiểm tra xem trường dữ liệu có bị bỏ trống hay không
        if (email.isEmpty()) {
            edtEmail.error = "Vui lòng nhập địa chỉ Email"
            edtEmail.requestFocus()
            return
        }

        // kiểm tra xem chuỗi nhập vào có phải là Email hợp lệ không
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.error = "Định dạng Email không hợp lệ (Ví dụ: abc@gmail.com)"
            edtEmail.requestFocus()
            return
        }

        // Hiển thị thông báo
        Toast.makeText(this, "Yêu cầu khôi phục mật khẩu đã được gửi đến: $email", Toast.LENGTH_LONG).show()
    }
}