package com.example.myapplication.View.PhuHuynh

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class UpdateInfoActivity : AppCompatActivity() {

    private lateinit var btnUpload: LinearLayout
    private lateinit var tvParentName: TextView
    private lateinit var edtPhone: EditText
    private lateinit var edtJob: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtAddress: EditText
    private lateinit var btnUpdateInfo: Button
    private lateinit var btnCancel: Button
    
    private var selectedImageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            Toast.makeText(this, "Đã chọn ảnh đại diện mới", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update_info)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        loadCurrentProfile()
        setupListeners()
    }

    private fun initViews() {
        btnUpload = findViewById(R.id.btnUpload)
        tvParentName = findViewById(R.id.tvParentName)
        edtPhone = findViewById(R.id.edtPhone)
        edtJob = findViewById(R.id.edtJob)
        edtEmail = findViewById(R.id.edtEmail)
        edtAddress = findViewById(R.id.edtAddress)
        btnUpdateInfo = findViewById(R.id.btnUpdateInfo)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun setupListeners() {
        btnUpload.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnUpdateInfo.setOnClickListener {
            handleUpdate()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun loadCurrentProfile() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/profile")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val data = JSONObject(body).optJSONObject("data")
                        runOnUiThread {
                            if (data != null) {
                                tvParentName.text = data.optString("fullName")
                                edtPhone.setText(data.optString("phoneNumber"))
                                edtJob.setText(data.optString("job"))
                                edtEmail.setText(data.optString("email"))
                                edtAddress.setText(data.optString("address"))
                            }
                        }
                    }
                    return@use
                }
            } catch (e: Exception) { }
        }.start()
    }

    private fun handleUpdate() {
        val name = tvParentName.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val job = edtJob.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val address = edtAddress.text.toString().trim()

        if (phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập SĐT và địa chỉ!", Toast.LENGTH_SHORT).show()
            return
        }

        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        // Sử dụng 'occupation' thay cho 'job' theo Model ParentProfileResponse
        val json = JSONObject().apply {
            put("fullName", name)
            put("phoneNumber", phone)
            put("occupation", job)
            put("email", email)
            put("address", address)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/profile")
            .addHeader("Authorization", "Bearer $token")
            .put(body)
            .build()

        btnUpdateInfo.isEnabled = false
        btnUpdateInfo.text = "Đang cập nhật..."

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    runOnUiThread {
                        btnUpdateInfo.isEnabled = true
                        btnUpdateInfo.text = "▸ Cập nhật thông tin"
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Lỗi khi cập nhật dữ liệu", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return@use
                }
            } catch (e: Exception) {
                runOnUiThread { 
                    btnUpdateInfo.isEnabled = true
                    Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show() 
                }
            }
        }.start()
    }
}
