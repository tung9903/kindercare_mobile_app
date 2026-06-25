package com.example.myapplication.View.PhuHuynh

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.google.android.material.textfield.TextInputEditText

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var btnBack: Button
    private lateinit var edtCurrentPassword: TextInputEditText
    private lateinit var edtNewPassword: TextInputEditText
    private lateinit var edtConfirmPassword: TextInputEditText
    private lateinit var btnUpdatePassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBack = findViewById(R.id.btnback)
        edtCurrentPassword = findViewById(R.id.edtCurrentPassword)
        edtNewPassword = findViewById(R.id.edtNewPassword)
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword)
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword)

        // Xử lý sự kiện nút quay lại
        btnBack.setOnClickListener {
            finish()
        }

        // Xử lý sự kiện click nút Cập nhật mật khẩu
        btnUpdatePassword.setOnClickListener {
            val currentPassword = edtCurrentPassword.text.toString().trim()
            val newPassword = edtNewPassword.text.toString().trim()
            val confirmPassword = edtConfirmPassword.text.toString().trim()

            // 1. Kiểm tra rỗng
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Kiểm tra độ dài mật khẩu mới (Tối thiểu 8 ký tự)
            if (newPassword.length < 8) {
                Toast.makeText(this, "Mật khẩu mới phải có tối thiểu 8 ký tự!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Kiểm tra mật khẩu mới và xác nhận mật khẩu có trùng khớp không
            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không trùng khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 4. điều kiện hợp lệ ->  gọi API cập nhật mật khẩu tại đây
            executeChangePassword(currentPassword, newPassword)
        }
    }

    private fun executeChangePassword(oldPass: String, newPass: String) {
        // TODO: Viết code gọi tới Repository / ViewModel để update mật khẩu lên Database
        Toast.makeText(this, "Cập nhật mật khẩu thành công!", Toast.LENGTH_SHORT).show()
        finish()
    }
}