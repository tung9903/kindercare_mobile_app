package com.example.myapplication.View.PhuHuynh

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R

class UpdateInfoActivity : AppCompatActivity() {

    private lateinit var btnUpload: LinearLayout
    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var btnUpdateInfo: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update_info)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnUpload = findViewById(R.id.btnUpload)
        edtPhone = findViewById(R.id.edtPhone)
        edtAddress = findViewById(R.id.edtAddress)
        btnUpdateInfo = findViewById(R.id.btnUpdateInfo)
        btnCancel = findViewById(R.id.btnCancel)

        // Xử lý sự kiện click vùng Upload ảnh
        btnUpload.setOnClickListener {
            // Thực hiện logic mở Bộ sưu tập chọn ảnh / File
            Toast.makeText(this, "Mở trình chọn tệp tin...", Toast.LENGTH_SHORT).show()
        }

        // Xử lý nút Cập nhật thông tin
        btnUpdateInfo.setOnClickListener {
            val phone = edtPhone.text.toString().trim()
            val address = edtAddress.text.toString().trim()

            if (phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
            } else {
                // Logic xử lý gửi API/Lưu dữ liệu
                Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Xử lý nút Hủy
        btnCancel.setOnClickListener {
            // Đóng màn hình quay về trang cũ
            finish()
        }
    }
}