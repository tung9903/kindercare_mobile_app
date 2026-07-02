package com.example.myapplication.View.PhuHuynh

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.R
import com.google.android.material.button.MaterialButton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Calendar

class RequestChangeActivity : AppCompatActivity() {

    private lateinit var tvOldName: TextView
    private lateinit var tvOldDob: TextView
    private lateinit var tvOldAddress: TextView
    private lateinit var tvOldAllergies: TextView

    private lateinit var edtNewName: EditText
    private lateinit var edtNewDob: EditText
    private lateinit var edtNewAddress: EditText
    private lateinit var edtNewAllergies: EditText

    private lateinit var layoutUpload: View
    private lateinit var layoutPreview: View
    private lateinit var imgPreview: ImageView
    private lateinit var btnRemoveFile: View

    private lateinit var btnSubmit: MaterialButton
    private lateinit var btnCancel: View
    private lateinit var btnClose: View

    private var fileUri: Uri? = null
    private var currentStudentId: Int = -1
    private var selectedDobTimestamp: Long = 0

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            fileUri = uri
            imgPreview.setImageURI(uri)
            layoutPreview.visibility = View.VISIBLE
            layoutUpload.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_request_change)

        val rootView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        initControlListeners()
        fetchCurrentChildInfo()
    }

    private fun initViews() {
        tvOldName = findViewById(R.id.tv_old_name)
        tvOldDob = findViewById(R.id.tv_old_dob)
        tvOldAddress = findViewById(R.id.tv_old_address)
        tvOldAllergies = findViewById(R.id.tv_old_allergies)

        edtNewName = findViewById(R.id.edt_new_name)
        edtNewDob = findViewById(R.id.edt_new_dob)
        edtNewAddress = findViewById(R.id.edt_new_address)
        edtNewAllergies = findViewById(R.id.edt_new_allergies)

        layoutUpload = findViewById(R.id.layout_upload)
        layoutPreview = findViewById(R.id.layout_preview)
        imgPreview = findViewById(R.id.img_evidence_preview)
        btnRemoveFile = findViewById(R.id.btn_remove_file)

        btnSubmit = findViewById(R.id.btn_submit)
        btnCancel = findViewById(R.id.btn_cancel)
        btnClose = findViewById(R.id.btnClose)
    }

    private fun initControlListeners() {
        edtNewDob.setOnClickListener {
            openDatePickerDialog()
        }

        layoutUpload.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnRemoveFile.setOnClickListener {
            fileUri = null
            layoutPreview.visibility = View.GONE
            layoutUpload.visibility = View.VISIBLE
        }

        btnSubmit.setOnClickListener {
            processFormSubmission()
        }

        btnCancel.setOnClickListener { finish() }
        btnClose.setOnClickListener { finish() }
    }

    private fun fetchCurrentChildInfo() {
        val child = DataManager.selectedChild
        if (child != null) {
            currentStudentId = child.getInt("studentId")
            bindChildData(child)
        } else {
            fetchChildFromApi()
        }
    }

    private fun bindChildData(child: JSONObject) {
        tvOldName.text = child.optString("fullName", "--")
        val dob = child.optLong("dateOfBirth", 0)
        if (dob > 0) {
            tvOldDob.text = DateHelper.formatLongToDate(dob)
        }
        tvOldAddress.text = child.optString("campusAddress", "--")
        tvOldAllergies.text = child.optString("allergies", "Không có")

        // Pre-fill fields for easier editing
        edtNewName.setText(child.optString("fullName"))
        edtNewAddress.setText(child.optString("campusAddress"))
        edtNewAllergies.setText(child.optString("allergies"))
        if (dob > 0) {
            selectedDobTimestamp = dob
            edtNewDob.setText(DateHelper.formatLongToDate(dob))
        }
    }

    private fun fetchChildFromApi() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children")
            .addHeader("Authorization", "Bearer $token")
            .get().build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val dataArray = JSONObject(body).optJSONArray("data")
                        if (dataArray != null && dataArray.length() > 0) {
                            val child = dataArray.getJSONObject(0)
                            currentStudentId = child.getInt("studentId")
                            runOnUiThread { bindChildData(child) }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("REQUEST_CHANGE", "Error: ${e.message}")
            }
        }.start()
    }

    private fun openDatePickerDialog() {
        val calendar = Calendar.getInstance()
        if (selectedDobTimestamp > 0) {
            calendar.timeInMillis = selectedDobTimestamp * 1000
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val cal = Calendar.getInstance()
            cal.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0)
            selectedDobTimestamp = cal.timeInMillis / 1000
            edtNewDob.setText(String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear))
        }, year, month, day).show()
    }

    private fun processFormSubmission() {
        val updatedName = edtNewName.text.toString().trim()
        val updatedAddress = edtNewAddress.text.toString().trim()
        val updatedAllergies = edtNewAllergies.text.toString().trim()

        if (updatedName.isEmpty() || updatedAddress.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền họ tên và địa chỉ!", Toast.LENGTH_SHORT).show()
            return
        }

        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        btnSubmit.isEnabled = false
        btnSubmit.text = "Đang gửi..."

        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("studentId", currentStudentId.toString())
            .addFormDataPart("newName", updatedName)
            .addFormDataPart("newDob", selectedDobTimestamp.toString())
            .addFormDataPart("newAddress", updatedAddress)
            .addFormDataPart("newAllergies", updatedAllergies)

        fileUri?.let { uri ->
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    builder.addFormDataPart(
                        "evidence",
                        "evidence_${System.currentTimeMillis()}.jpg",
                        bytes.toRequestBody("image/jpeg".toMediaType())
                    )
                }
            } catch (e: Exception) {
                Log.e("REQUEST_CHANGE", "Error reading file: ${e.message}")
            }
        }

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children/request-change")
            .addHeader("Authorization", "Bearer $token")
            .post(builder.build())
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    runOnUiThread {
                        btnSubmit.isEnabled = true
                        btnSubmit.text = "Gửi yêu cầu"
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Gửi yêu cầu thành công!", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            val msg = JSONObject(body ?: "{}").optString("message", "Lỗi gửi yêu cầu")
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Gửi yêu cầu"
                    Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
