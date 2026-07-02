package com.example.myapplication.View.PhuHuynh

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.google.android.material.button.MaterialButton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AddAuthorizedPersonActivity : AppCompatActivity() {

    private lateinit var imgAvatar: ImageView
    private lateinit var edtFullName: EditText
    private lateinit var edtRelationship: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtIdCard: EditText
    private lateinit var btnSubmit: MaterialButton
    
    private var selectedImageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            imgAvatar.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_authorized_person)

        val rootView = findViewById<View>(R.id.main_add_authorized)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        imgAvatar = findViewById(R.id.imgAvatar)
        edtFullName = findViewById(R.id.edtFullName)
        edtRelationship = findViewById(R.id.edtRelationship)
        edtPhone = findViewById(R.id.edtPhone)
        edtIdCard = findViewById(R.id.edtIdCard)
        btnSubmit = findViewById(R.id.btnSubmit)
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        
        findViewById<View>(R.id.btnPickImage).setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnSubmit.setOnClickListener {
            handleFormSubmit()
        }
    }

    private fun handleFormSubmit() {
        val name = edtFullName.text.toString().trim()
        val relation = edtRelationship.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val idCard = edtIdCard.text.toString().trim()

        if (name.isEmpty() || relation.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ các trường bắt buộc", Toast.LENGTH_SHORT).show()
            return
        }

        // Logic gửi yêu cầu lên server
        btnSubmit.isEnabled = false
        Toast.makeText(this, "Đang gửi yêu cầu phê duyệt...", Toast.LENGTH_SHORT).show()

        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)

        if (token == null) {
            btnSubmit.isEnabled = true
            return
        }

        val studentId = DataManager.selectedChild?.optInt("studentId") ?: -1

        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("studentId", studentId.toString())
            .addFormDataPart("fullName", name)
            .addFormDataPart("relationship", relation)
            .addFormDataPart("phoneNumber", phone)
            .addFormDataPart("idCardNumber", idCard)

        selectedImageUri?.let { uri ->
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    builder.addFormDataPart(
                        "avatar",
                        "authorized_person.jpg",
                        bytes.toRequestBody("image/jpeg".toMediaType())
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/authorized-persons")
            .addHeader("Authorization", "Bearer $token")
            .post(builder.build())
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val resBody = response.body?.string()
                    runOnUiThread {
                        btnSubmit.isEnabled = true
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Gửi yêu cầu ủy quyền thành công! Đang chờ phê duyệt.", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            val msg = JSONObject(resBody ?: "{}").optString("message", "Lỗi gửi yêu cầu")
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    btnSubmit.isEnabled = true
                    Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
