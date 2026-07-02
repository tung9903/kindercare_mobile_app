package com.example.myapplication.View.PhuHuynh

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.Model.MedicationRequestModel
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.MedicationHistoryPHAdapter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class MedicationRequestPHActivity : AppCompatActivity() {

    private lateinit var edtMedicineDetails: EditText
    private lateinit var edtDosage: EditText
    private lateinit var edtFrequency: EditText
    private lateinit var edtTimeToTake: EditText
    private lateinit var edtParentNote: EditText
    private lateinit var tvMedRequestDate: TextView
    
    // Media Views
    private lateinit var btnPickMedia: LinearLayout
    private lateinit var layoutImagePreview: View
    private lateinit var imgMedPreview: ImageView
    private lateinit var btnRemoveImage: View
    
    private lateinit var btnSubmit: Button
    private lateinit var btnBack: ImageView
    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: MedicationHistoryPHAdapter
    private val historyList = mutableListOf<MedicationRequestModel>()
    
    private var currentStudentId: Int = -1
    private var selectedDateMillis: Long = System.currentTimeMillis() / 1000
    private var selectedImageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            imgMedPreview.setImageURI(uri)
            layoutImagePreview.visibility = View.VISIBLE
            btnPickMedia.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_medication_request_ph)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        setupListeners()

        val selectedChild = DataManager.selectedChild
        if (selectedChild != null) {
            currentStudentId = selectedChild.getInt("studentId")
            findViewById<TextView>(R.id.tvStudentName)?.text = selectedChild.optString("fullName")
            findViewById<TextView>(R.id.tvClassName)?.text = selectedChild.optString("className", "N/A")
            fetchMedicationHistory(currentStudentId)
        } else {
            fetchChildAndLoadMedication()
        }
    }

    private fun initViews() {
        edtMedicineDetails = findViewById(R.id.edtMedicineDetails)
        edtDosage = findViewById(R.id.edtDosage)
        edtFrequency = findViewById(R.id.edtFrequency)
        edtTimeToTake = findViewById(R.id.edtTimeToTake)
        edtParentNote = findViewById(R.id.edtParentNote)
        tvMedRequestDate = findViewById(R.id.tvMedRequestDate)
        
        btnPickMedia = findViewById(R.id.btnPickMedia)
        layoutImagePreview = findViewById(R.id.layoutImagePreview)
        imgMedPreview = findViewById(R.id.imgMedPreview)
        btnRemoveImage = findViewById(R.id.btnRemoveImage)
        
        btnSubmit = findViewById(R.id.btnSubmitMedRequest)
        btnBack = findViewById(R.id.btnBack)
        rvHistory = findViewById(R.id.rvMedicationHistory)

        tvMedRequestDate.text = DateHelper.formatLongToDate(selectedDateMillis)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnSubmit.setOnClickListener { handleFormSubmit() }
        tvMedRequestDate.setOnClickListener { showDatePicker() }
        
        btnPickMedia.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnRemoveImage.setOnClickListener {
            selectedImageUri = null
            layoutImagePreview.visibility = View.GONE
            btnPickMedia.visibility = View.VISIBLE
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            selectedDateMillis = selectedCalendar.timeInMillis / 1000
            tvMedRequestDate.text = DateHelper.formatLongToDate(selectedDateMillis)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private fun setupRecyclerView() {
        rvHistory.layoutManager = LinearLayoutManager(this)
        adapter = MedicationHistoryPHAdapter(historyList) { medRequestId ->
            confirmCancelMedRequest(medRequestId)
        }
        rvHistory.adapter = adapter
    }

    private fun confirmCancelMedRequest(medRequestId: Int) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận hủy dặn thuốc")
            .setMessage("Bạn có chắc chắn muốn hủy yêu cầu dặn thuốc này không?")
            .setPositiveButton("Hủy yêu cầu") { _, _ ->
                cancelMedicationRequest(medRequestId)
            }
            .setNegativeButton("Quay lại", null)
            .show()
    }

    private fun cancelMedicationRequest(medRequestId: Int) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/medication-requests/$medRequestId/cancel")
            .addHeader("Authorization", "Bearer $token")
            .patch("{}".toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val resBody = response.body?.string()
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Hủy yêu cầu thành công!", Toast.LENGTH_SHORT).show()
                            fetchMedicationHistory(currentStudentId)
                        } else {
                            val msg = JSONObject(resBody ?: "{}").optString("message", "Lỗi khi hủy yêu cầu")
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                    return@use
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }

    private fun fetchChildAndLoadMedication() {
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
                            runOnUiThread {
                                findViewById<TextView>(R.id.tvStudentName)?.text = child.optString("fullName")
                                findViewById<TextView>(R.id.tvClassName)?.text = child.optString("className", "N/A")
                            }
                            fetchMedicationHistory(currentStudentId)
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    private fun fetchMedicationHistory(studentId: Int) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children/$studentId/medication-requests")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val dataArray = JSONObject(body).optJSONArray("data")
                        val temp = mutableListOf<MedicationRequestModel>()
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                val obj = it.getJSONObject(i)
                                temp.add(MedicationRequestModel(
                                    medRequestId = obj.optInt("medRequestId"),
                                    studentId = obj.optInt("studentId"),
                                    studentName = obj.optString("studentName"),
                                    parentId = obj.optInt("parentId"),
                                    requestDate = obj.optLong("requestDate"),
                                    medicineDetails = obj.optString("medicineDetails"),
                                    dosage = obj.optString("dosage"),
                                    frequency = obj.optString("frequency"),
                                    timeToTake = obj.optString("timeToTake"),
                                    status = obj.optString("status"),
                                    teacherNote = obj.optString("teacherNote", ""),
                                    medicineImageUrl = if (obj.isNull("medicineImageUrl")) null else obj.optString("medicineImageUrl")
                                ))
                            }
                        }
                        runOnUiThread {
                            historyList.clear()
                            historyList.addAll(temp.reversed())
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    private fun handleFormSubmit() {
        val medName = edtMedicineDetails.text.toString().trim()
        val dosage = edtDosage.text.toString().trim()
        val frequency = edtFrequency.text.toString().trim()
        val timeToTake = edtTimeToTake.text.toString().trim()
        val parentNote = edtParentNote.text.toString().trim()

        if (medName.isEmpty() || dosage.isEmpty() || currentStudentId == -1) {
            Toast.makeText(this, "Vui lòng nhập đủ tên thuốc và liều lượng", Toast.LENGTH_SHORT).show()
            return
        }

        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        btnSubmit.isEnabled = false
        Toast.makeText(this, "Đang gửi yêu cầu...", Toast.LENGTH_SHORT).show()

        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("studentId", currentStudentId.toString())
            .addFormDataPart("requestDate", selectedDateMillis.toString())
            .addFormDataPart("medicineDetails", medName)
            .addFormDataPart("dosage", dosage)
            .addFormDataPart("frequency", frequency)
            .addFormDataPart("timeToTake", timeToTake)
            .addFormDataPart("parentNote", parentNote) // Đã sửa về 'parentNote' số ít theo Swagger

        selectedImageUri?.let { uri ->
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    val fileName = "medication_${System.currentTimeMillis()}.jpg"
                    val requestBody = okhttp3.RequestBody.create("image/jpeg".toMediaType(), bytes)
                    builder.addFormDataPart("medicineImage", fileName, requestBody)
                }
            } catch (e: Exception) {
                Log.e("MED_SUBMIT", "Error reading image file: ${e.message}")
            }
        }

        // Đã cập nhật URL về đường dẫn chuẩn theo Swagger POST
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/medication-requests")
            .addHeader("Authorization", "Bearer $token")
            .post(builder.build()).build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val resBody = response.body?.string()
                    runOnUiThread {
                        btnSubmit.isEnabled = true
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Gửi yêu cầu thành công!", Toast.LENGTH_SHORT).show()
                            resetForm()
                            fetchMedicationHistory(currentStudentId)
                        } else {
                            // Hiển thị chi tiết lỗi từ Server
                            val errorBody = resBody ?: "{}"
                            val errorMessage = try {
                                JSONObject(errorBody).optString("message", "Lỗi server (${response.code})")
                            } catch (e: Exception) {
                                "Lỗi hệ thống (${response.code})"
                            }
                            Log.e("MED_SUBMIT", "Error Code: ${response.code}, Body: $resBody")
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) { 
                runOnUiThread { 
                    btnSubmit.isEnabled = true
                    Toast.makeText(this, "Lỗi kết nối mạng: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun resetForm() {
        edtMedicineDetails.setText("")
        edtDosage.setText("")
        edtFrequency.setText("")
        edtTimeToTake.setText("")
        edtParentNote.setText("")
        selectedImageUri = null
        layoutImagePreview.visibility = View.GONE
        btnPickMedia.visibility = View.VISIBLE
    }
}
