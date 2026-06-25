package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R

class ManHinhNhanXetVaDanhGia : AppCompatActivity() {

    private lateinit var edtComment: EditText
    private lateinit var txtCharCount: TextView
    private lateinit var edtPhysical: EditText
    private lateinit var edtCognitive: EditText
    private lateinit var edtLanguage: EditText
    private lateinit var edtAesthetic: EditText
    private lateinit var edtEmotional: EditText
    
    private var studentId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_nhan_xet_va_danh_gia)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        studentId = intent.getIntExtra("STUDENT_ID", -1)
        loadStudentData()

        findViewById<LinearLayout>(R.id.btn_back).setOnClickListener {
            finish()
        }

        setupCharCounter()
        setupSkillEdits()
        setupActionButtons()
    }

    private fun initViews() {
        edtComment = findViewById(R.id.edt_teacher_comment)
        txtCharCount = findViewById(R.id.txt_char_count)
        edtPhysical = findViewById(R.id.edt_skill_physical)
        edtCognitive = findViewById(R.id.edt_skill_cognitive)
        edtLanguage = findViewById(R.id.edt_skill_language)
        edtAesthetic = findViewById(R.id.edt_skill_aesthetic)
        edtEmotional = findViewById(R.id.edt_skill_emotional)
    }

    private fun loadStudentData() {
        val student = DataManager.studentList.find { it.StudentID == studentId }
        if (student != null) {
            findViewById<TextView>(R.id.tvHeaderTitle).text = "Đánh giá: ${student.FullName}"
            edtComment.setText(student.teacherComment)
            edtPhysical.setText(student.skillPhysical.toString())
            edtCognitive.setText(student.skillCognitive.toString())
            edtLanguage.setText(student.skillLanguage.toString())
            edtAesthetic.setText(student.skillAesthetic.toString())
            edtEmotional.setText(student.skillEmotional.toString())
        }
    }

    private fun setupActionButtons() {
        findViewById<Button>(R.id.btn_submit_report).setOnClickListener {
            saveData(true)
        }
        
        findViewById<Button>(R.id.btn_save_draft).setOnClickListener {
            saveData(false)
        }
    }

    private fun saveData(isSubmit: Boolean) {
        val student = DataManager.studentList.find { it.StudentID == studentId }
        if (student != null) {
            student.teacherComment = edtComment.text.toString()
            student.skillPhysical = edtPhysical.text.toString().toIntOrNull() ?: student.skillPhysical
            student.skillCognitive = edtCognitive.text.toString().toIntOrNull() ?: student.skillCognitive
            student.skillLanguage = edtLanguage.text.toString().toIntOrNull() ?: student.skillLanguage
            student.skillAesthetic = edtAesthetic.text.toString().toIntOrNull() ?: student.skillAesthetic
            student.skillEmotional = edtEmotional.text.toString().toIntOrNull() ?: student.skillEmotional

            if (isSubmit) {
                Toast.makeText(this, "Đã gửi báo cáo đánh giá thành công!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Đã lưu bản nháp", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCharCounter() {
        edtComment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                txtCharCount.text = "$currentLength / 500 ký tự"
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSkillEdits() {
        val btnEditSkills = findViewById<TextView>(R.id.btn_edit_skills)
        val editTexts = listOf(edtPhysical, edtCognitive, edtLanguage, edtAesthetic, edtEmotional)

        var isEditing = false

        btnEditSkills.setOnClickListener {
            isEditing = !isEditing
            if (isEditing) {
                btnEditSkills.text = "Xong"
                editTexts.forEach { 
                    it.isEnabled = true
                    it.setBackgroundResource(android.R.drawable.edit_text) 
                }
                editTexts[0].requestFocus()
            } else {
                btnEditSkills.text = "Chỉnh sửa"
                editTexts.forEach { 
                    it.isEnabled = false
                    it.background = null 
                }
            }
        }
    }
}
