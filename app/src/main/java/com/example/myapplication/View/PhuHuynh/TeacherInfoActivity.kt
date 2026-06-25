package com.example.myapplication.View.PhuHuynh

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityTeacherInfoBinding

class TeacherInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Khởi tạo View Binding
        binding = ActivityTeacherInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupActionListeners()
    }

    private fun setupActionListeners() {
        // Sự kiện click nút quay lại (Back arrow trên Header)
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Sự kiện copy số điện thoại
        binding.btnCopyPhone.setOnClickListener {
            val phoneNumber = binding.tvPhoneValue.text.toString()
            copyToClipboard(phoneNumber)
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Teacher_Phone", text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Đã sao chép số điện thoại: $text", Toast.LENGTH_SHORT).show()
    }
}