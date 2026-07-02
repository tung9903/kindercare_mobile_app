package com.example.myapplication.View.GiaoVien

import android.os.Bundle
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ManHinhChinhSuaThongTinCaNhan : AppCompatActivity() {
    
    private lateinit var edtName: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtDob: EditText
    private lateinit var edtGender: EditText
    private lateinit var edtIdCard: EditText
    private lateinit var edtPosition: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtAddress: EditText
    private lateinit var edtBio: EditText

    private var selectedDobTimestamp: Long = 0

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
        setupDatePicker()
        loadCurrentData()

        findViewById<MaterialButton>(R.id.btnSubmit).setOnClickListener { saveData() }
        findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener { finish() }
    }

    private fun initViews() {
        edtName = findViewById(R.id.edtParentName)
        edtPhone = findViewById(R.id.edtPhone)
        edtDob = findViewById(R.id.edtDob)
        edtGender = findViewById(R.id.edtGender)
        edtIdCard = findViewById(R.id.edtIdCard)
        edtPosition = findViewById(R.id.edtPosition)
        edtEmail = findViewById(R.id.edtEmail)
        edtAddress = findViewById(R.id.edtAddress)
        edtBio = findViewById(R.id.edtBio)
    }

    private fun setupDatePicker() {
        edtDob.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            if (selectedDobTimestamp > 0) calendar.timeInMillis = selectedDobTimestamp * 1000
            
            android.app.DatePickerDialog(this, { _, year, month, day ->
                val selectedCal = java.util.Calendar.getInstance()
                selectedCal.set(year, month, day)
                selectedDobTimestamp = selectedCal.timeInMillis / 1000
                edtDob.setText(com.example.myapplication.Model.DateHelper.formatLongToDate(selectedCal.timeInMillis))
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun loadCurrentData() {
        val teacher = DataManager.currentTeacher
        edtName.setText(teacher.fullName)
        edtPhone.setText(teacher.phoneNumber)
        
        teacher.dateOfBirth?.let {
            selectedDobTimestamp = it
            edtDob.setText(com.example.myapplication.Model.DateHelper.formatLongToDate(it * 1000))
        }
        
        edtGender.setText(teacher.gender ?: "")
        edtIdCard.setText(teacher.idCard ?: "")
        edtPosition.setText(teacher.professionalRank)
        edtEmail.setText(teacher.email)
        edtAddress.setText(teacher.address)
        edtBio.setText(teacher.bio ?: "")
    }

    private fun saveData() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val json = JSONObject().apply {
            put("fullName", edtName.text.toString())
            put("phoneNumber", edtPhone.text.toString())
            put("email", edtEmail.text.toString())
            put("dateOfBirth", selectedDobTimestamp)
            put("gender", edtGender.text.toString())
            put("idCard", edtIdCard.text.toString())
            put("address", edtAddress.text.toString())
            put("professionalRank", edtPosition.text.toString())
            put("bio", edtBio.text.toString())
            put("avatarUrl", DataManager.currentTeacher.avatarUrl ?: "")
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val url = "https://web-test.kindercare.app/api/v1/teacher/profile"
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .put(body)
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                            
                            DataManager.currentTeacher.apply {
                                fullName = edtName.text.toString()
                                phoneNumber = edtPhone.text.toString()
                                email = edtEmail.text.toString()
                                dateOfBirth = selectedDobTimestamp
                                gender = edtGender.text.toString()
                                idCard = edtIdCard.text.toString()
                                address = edtAddress.text.toString()
                                professionalRank = edtPosition.text.toString()
                                bio = edtBio.text.toString()
                            }

                            finish()
                        } else {
                            android.util.Log.e("UPDATE_PROFILE", "Error: $responseBody")
                            Toast.makeText(this, "Lỗi cập nhật: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) { 
                e.printStackTrace()
                runOnUiThread { Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }
}
