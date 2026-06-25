package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.google.android.material.button.MaterialButton

class ManHinhChinhSuaThongTinCaNhan : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtPosition: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtAddress: EditText
    private lateinit var edtBio: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chinh_sua_thong_tin_ca_nhan)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        loadCurrentData()

        findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnSubmit).setOnClickListener {
            saveData()
        }

        setupCharCounter()
    }

    private fun initViews() {
        edtName = findViewById(R.id.edtParentName)
        edtPhone = findViewById(R.id.edtPhone)
        edtPosition = findViewById(R.id.edtPosition)
        edtEmail = findViewById(R.id.edtEmail)
        edtAddress = findViewById(R.id.edtAddress)
        edtBio = findViewById(R.id.edtBio)
    }

    private fun loadCurrentData() {
        val teacher = DataManager.currentTeacher
        edtName.setText(teacher.FullName)
        edtPhone.setText(teacher.PhoneNumber)
        edtPosition.setText(teacher.ProfessionalRank)
        edtEmail.setText(teacher.Email)
        edtAddress.setText(teacher.Address)
        edtBio.setText(teacher.bio)
    }

    private fun saveData() {
        val name = edtName.text.toString()
        val phone = edtPhone.text.toString()
        val position = edtPosition.text.toString()
        val email = edtEmail.text.toString()
        val address = edtAddress.text.toString()
        val bio = edtBio.text.toString()

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ họ tên và số điện thoại", Toast.LENGTH_SHORT).show()
            return
        }

        DataManager.currentTeacher.apply {
            this.FullName = name
            this.PhoneNumber = phone
            this.ProfessionalRank = position
            this.Email = email
            this.Address = address
            this.bio = bio
        }

        Toast.makeText(this, "Đã cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun setupCharCounter() {
        val txtCharCounter = findViewById<TextView>(R.id.txtCharCounter)
        txtCharCounter.text = "${edtBio.text.length} / 500 ký tự"

        edtBio.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                txtCharCounter.text = "$length / 500 ký tự"
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
