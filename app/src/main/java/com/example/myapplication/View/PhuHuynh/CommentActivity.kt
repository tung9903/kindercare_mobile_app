package com.example.myapplication.View.PhuHuynh

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityCommentBinding

class CommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUI()
        setupActionListeners()
    }

    private fun setupUI() {
        binding.tvTeacherComment.text = "“Bé hơi lười ăn thịt, chỉ ăn canh và cơm. Cô đã cố dỗ nhưng bé ngậm.”"
        binding.tvTeacherInfoTime.text = "CÔ MINH THƯ • 10:45"
    }

    private fun setupActionListeners() {

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSendFeedback.setOnClickListener {
            val feedbackText = binding.edtFeedbackInput.text.toString().trim()

            if (feedbackText.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung góp ý!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Đã gửi phản hồi thành công đến giáo viên!", Toast.LENGTH_SHORT).show()
                binding.edtFeedbackInput.text.clear()
            }
        }
    }
}