package com.example.myapplication.View.PhuHuynh

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
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
import com.example.myapplication.Model.LeaveRequestModel
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.LeaveHistoryAdapter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LeaveRequestActivity : AppCompatActivity() {

    private lateinit var tvFromDate: TextView
    private lateinit var tvToDate: TextView
    private lateinit var layoutDaySessions: LinearLayout
    private lateinit var spReasonCategory: Spinner
    private lateinit var edtReasonDetail: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnBack: ImageView
    private lateinit var btnUploadImage: LinearLayout
    private lateinit var btnUploadFile: LinearLayout
    
    // Preview minh chứng
    private lateinit var layoutEvidencePreview: RelativeLayout
    private lateinit var imgEvidencePreview: ImageView
    private lateinit var btnRemoveEvidence: ImageView
    
    private lateinit var rvLeaveHistory: RecyclerView
    private lateinit var leaveHistoryAdapter: LeaveHistoryAdapter
    private val leaveHistoryList = mutableListOf<LeaveRequestModel>()

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    private var fromDateLong: Long = 0
    private var toDateLong: Long = 0
    private var currentStudentId: Int = -1
    private var selectedImageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            layoutEvidencePreview.visibility = android.view.View.VISIBLE
            imgEvidencePreview.setImageURI(uri)
            Toast.makeText(this, "Đã chọn ảnh minh chứng", Toast.LENGTH_SHORT).show()
        }
    }

    // Danh sách lưu các checkbox buổi nghỉ động
    private val dynamicSessionList = mutableListOf<DaySessionView>()

    data class DaySessionView(val date: Long, val cbMorning: CheckBox, val cbAfternoon: CheckBox)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_leave_request)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupReasonSpinner()
        setupHistoryRecyclerView()

        // Ưu tiên lấy bé đang chọn
        val selectedChild = DataManager.selectedChild
        if (selectedChild != null) {
            currentStudentId = selectedChild.getInt("studentId")
            findViewById<TextView>(R.id.tvStudentName).text = selectedChild.optString("fullName")
            findViewById<TextView>(R.id.tvClassName).text = selectedChild.optString("className", "N/A")
            fetchLeaveHistory(currentStudentId)
        } else {
            fetchChildAndLoadLeave()
        }

        btnBack.setOnClickListener { finish() }

        tvFromDate.setOnClickListener {
            showDatePickerDialog(tvFromDate, "Từ ngày") { timestamp -> 
                fromDateLong = timestamp
                updateDynamicSessions()
            }
        }
        tvToDate.setOnClickListener {
            showDatePickerDialog(tvToDate, "Đến ngày") { timestamp -> 
                toDateLong = timestamp
                updateDynamicSessions()
            }
        }

        btnUploadImage.setOnClickListener { 
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        btnUploadFile.setOnClickListener { 
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }
        btnRemoveEvidence.setOnClickListener { 
            selectedImageUri = null
            layoutEvidencePreview.visibility = android.view.View.GONE
        }

        btnSubmit.setOnClickListener {
            handleFormSubmit()
        }
        
        setDefaultDates()
    }

    private fun setDefaultDates() {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        fromDateLong = today.timeInMillis / 1000
        toDateLong = fromDateLong
        
        tvFromDate.text = dateFormat.format(today.time)
        tvToDate.text = dateFormat.format(today.time)
        
        updateDynamicSessions()
    }

    private fun initViews() {
        tvFromDate = findViewById(R.id.tvFromDate)
        tvToDate = findViewById(R.id.tvToDate)
        layoutDaySessions = findViewById(R.id.layoutDaySessions)
        spReasonCategory = findViewById(R.id.spReasonCategory)
        edtReasonDetail = findViewById(R.id.edtReasonDetail)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnBack = findViewById(R.id.btnBack)
        btnUploadImage = findViewById(R.id.btnUploadImage)
        btnUploadFile = findViewById(R.id.btnUploadFile)
        
        layoutEvidencePreview = findViewById(R.id.layoutEvidencePreview)
        imgEvidencePreview = findViewById(R.id.imgEvidencePreview)
        btnRemoveEvidence = findViewById(R.id.btnRemoveEvidence)
        
        rvLeaveHistory = findViewById(R.id.rvLeaveHistory)
    }

    private fun updateDynamicSessions() {
        val titleView = layoutDaySessions.getChildAt(0)
        layoutDaySessions.removeAllViews()
        if (titleView != null) layoutDaySessions.addView(titleView)
        dynamicSessionList.clear()

        if (fromDateLong == 0L || toDateLong == 0L || fromDateLong > toDateLong) {
            return
        }

        val startCal = Calendar.getInstance()
        startCal.timeInMillis = fromDateLong * 1000
        
        val endCal = Calendar.getInstance()
        endCal.timeInMillis = toDateLong * 1000

        val inflater = LayoutInflater.from(this)

        while (!startCal.after(endCal)) {
            val itemDate = startCal.timeInMillis / 1000
            val sessionView = inflater.inflate(R.layout.item_leave_day_session, layoutDaySessions, false)
            
            val tvDateLabel = sessionView.findViewById<TextView>(R.id.tvDateLabel)
            val cbMorning = sessionView.findViewById<CheckBox>(R.id.cbMorning)
            val cbAfternoon = sessionView.findViewById<CheckBox>(R.id.cbAfternoon)

            tvDateLabel.text = "Ngày ${dateFormat.format(startCal.time)}"
            
            layoutDaySessions.addView(sessionView)
            dynamicSessionList.add(DaySessionView(itemDate, cbMorning, cbAfternoon))
            
            startCal.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun setupReasonSpinner() {
        val reasons = listOf("Chọn lý do xin nghỉ", "Bị ốm/Sốt", "Đi du lịch cùng gia đình", "Có việc gia đình riêng", "Khác")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reasons)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spReasonCategory.adapter = adapter

        spReasonCategory.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedReason = reasons[position]
                if (selectedReason == "Khác") {
                    edtReasonDetail.visibility = android.view.View.VISIBLE
                } else {
                    edtReasonDetail.visibility = android.view.View.GONE
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupHistoryRecyclerView() {
        rvLeaveHistory.layoutManager = LinearLayoutManager(this)
        leaveHistoryAdapter = LeaveHistoryAdapter(leaveHistoryList) { requestId ->
            confirmCancelRequest(requestId)
        }
        rvLeaveHistory.adapter = leaveHistoryAdapter
    }

    private fun confirmCancelRequest(requestId: Int) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận hủy đơn")
            .setMessage("Bạn có chắc chắn muốn hủy đơn xin nghỉ này không?")
            .setPositiveButton("Hủy đơn") { _, _ ->
                cancelLeaveRequest(requestId)
            }
            .setNegativeButton("Quay lại", null)
            .show()
    }

    private fun cancelLeaveRequest(requestId: Int) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/leave-requests/$requestId/cancel")
            .addHeader("Authorization", "Bearer $token")
            .patch("{}".toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val resBody = response.body?.string()
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Hủy đơn thành công!", Toast.LENGTH_SHORT).show()
                            fetchLeaveHistory(currentStudentId)
                        } else {
                            val msg = JSONObject(resBody ?: "{}").optString("message", "Lỗi khi hủy đơn")
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }

    private fun fetchChildAndLoadLeave() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val dataArray = jsonResponse.optJSONArray("data")
                        if (dataArray != null && dataArray.length() > 0) {
                            val child = dataArray.getJSONObject(0)
                            currentStudentId = child.getInt("studentId")
                            
                            runOnUiThread {
                                findViewById<TextView>(R.id.tvStudentName).text = child.optString("fullName")
                                findViewById<TextView>(R.id.tvClassName).text = child.optString("className", "N/A")
                            }
                            
                            fetchLeaveHistory(currentStudentId)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LEAVE_API", "Lỗi lấy ID bé: ${e.message}")
            }
        }.start()
    }

    private fun fetchLeaveHistory(studentId: Int) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val client = DataManager.okHttpClient
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children/$studentId/leave-requests")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val dataArray = jsonResponse.optJSONArray("data")
                        val tempHistory = mutableListOf<LeaveRequestModel>()
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                tempHistory.add(parseLeaveRequest(it.getJSONObject(i)))
                            }
                        }
                        runOnUiThread {
                            leaveHistoryList.clear()
                            leaveHistoryList.addAll(tempHistory)
                            leaveHistoryAdapter.notifyDataSetChanged()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LEAVE_API", "Lỗi tải lịch sử: ${e.message}")
            }
        }.start()
    }

    private fun parseLeaveRequest(obj: JSONObject): LeaveRequestModel {
        return LeaveRequestModel(
            requestId = obj.optInt("requestId"),
            studentId = obj.optInt("studentId"),
            studentName = obj.optString("studentName", ""),
            studentAvatar = obj.optString("studentAvatar", ""),
            className = obj.optString("className", ""),
            parentId = obj.optInt("parentId"),
            parentName = obj.optString("parentName", ""),
            fromDate = obj.optLong("fromDate"),
            toDate = obj.optLong("toDate"),
            reason = obj.optString("reason", ""),
            evidenceUrl = obj.optString("evidenceUrl", ""),
            status = obj.optString("status", "Pending"),
            isMealFeeDeducted = obj.optInt("isMealFeeDeducted") == 1,
            parentNotes = obj.optString("parentNotes", ""),
            createdAt = obj.optLong("createdAt")
        )
    }

    private fun handleFormSubmit() {
        val selectedReason = spReasonCategory.selectedItem.toString()
        if (selectedReason == "Chọn lý do xin nghỉ") {
            Toast.makeText(this, "Vui lòng chọn lý do nghỉ!", Toast.LENGTH_SHORT).show()
            return
        }

        if (fromDateLong == 0L || toDateLong == 0L) {
            Toast.makeText(this, "Vui lòng chọn thời gian nghỉ!", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentStudentId == -1) return
        
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        btnSubmit.isEnabled = false
        btnSubmit.text = "Đang gửi đơn..."

        val reasonText = if (selectedReason == "Khác") edtReasonDetail.text.toString() else selectedReason
        val notes = if (selectedReason == "Khác") "" else edtReasonDetail.text.toString()

        // 1. Tạo JSON body chuẩn Swagger (application/json)
        val json = JSONObject().apply {
            put("studentId", currentStudentId)
            put("fromDate", fromDateLong)
            put("toDate", toDateLong)
            put("reason", reasonText)
            put("evidenceUrl", null) // Hiện tại để null theo ví dụ Swagger
            put("parentNotes", notes) // Khớp chính xác với trường 'parentNotes'
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/leave-requests")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val resBody = response.body?.string()
                    runOnUiThread {
                        btnSubmit.isEnabled = true
                        btnSubmit.text = "Gửi đơn xin nghỉ"
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Gửi đơn thành công!", Toast.LENGTH_SHORT).show()
                            fetchLeaveHistory(currentStudentId)
                            resetForm()
                        } else {
                            Log.e("LEAVE_POST", "Error: ${response.code}, Body: $resBody")
                            val msg = JSONObject(resBody ?: "{}").optString("message", "Lỗi gửi đơn")
                            Toast.makeText(this, "Thất bại (${response.code}): $msg", Toast.LENGTH_LONG).show()
                        }
                    }
                    return@use
                }
            } catch (e: Exception) {
                runOnUiThread {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Gửi đơn xin nghỉ"
                    Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun resetForm() {
        setDefaultDates()
        spReasonCategory.setSelection(0)
        edtReasonDetail.setText("")
        layoutEvidencePreview.visibility = android.view.View.GONE
    }

    private fun showDatePickerDialog(textView: TextView, titlePrefix: String, onDateSelected: (Long) -> Unit) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, y, m, d ->
            val sel = Calendar.getInstance()
            sel.set(y, m, d)
            val ts = sel.timeInMillis / 1000
            onDateSelected(ts)
            textView.text = dateFormat.format(sel.time)
        }, year, month, day)
        datePickerDialog.show()
    }
}
