package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.LeaveRequestModel
import com.example.myapplication.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ManHinhChiTietDonXinNghi : AppCompatActivity() {

    private var requestId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chi_tiet_don_xin_nghi)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requestId = intent.getIntExtra("REQUEST_ID", -1)
        
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        fetchRequestDetail()
    }

    private fun fetchRequestDetail() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty() || requestId == -1) return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/teacher/leave-requests/$requestId")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    Log.d("LEAVE_DETAIL_API", "Kết quả: $body")
                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val dataObj = jsonResponse.optJSONObject("data")
                        
                        if (dataObj != null) {
                            val leaveRequest = parseLeaveRequest(dataObj)
                            runOnUiThread {
                                bindDataToUI(leaveRequest)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LEAVE_DETAIL_API", "Lỗi: ${e.message}")
            }
        }.start()
    }

    private fun parseLeaveRequest(obj: JSONObject): LeaveRequestModel {
        return LeaveRequestModel(
            requestId = obj.optInt("requestId"),
            studentId = obj.optInt("studentId"),
            studentName = obj.optString("studentName"),
            studentAvatar = obj.optString("studentAvatar"),
            className = obj.optString("className"),
            parentId = obj.optInt("parentId"),
            parentName = obj.optString("parentName"),
            parentPhone = obj.optString("parentPhone"),
            fromDate = obj.optLong("fromDate"),
            toDate = obj.optLong("toDate"),
            reason = obj.optString("reason"),
            evidenceUrl = obj.optString("evidenceUrl"),
            status = obj.optString("status"),
            isMealFeeDeducted = obj.optInt("isMealFeeDeducted") == 1,
            parentNotes = obj.optString("parentNotes"),
            createdAt = obj.optLong("createdAt")
        )
    }

    private fun bindDataToUI(request: LeaveRequestModel) {
        findViewById<TextView>(R.id.txtTitle).text = "Chi tiết đơn xin nghỉ – Bé ${request.studentName}"
        findViewById<TextView>(R.id.tvStudentName).text = request.studentName
        findViewById<TextView>(R.id.tvParentInfo).text = "PH: ${request.parentName}"
        findViewById<TextView>(R.id.tvParentPhone).text = "SĐT: ${request.parentPhone ?: "Chưa có"}"
        
        val imgAvatar = findViewById<ImageView>(R.id.imgAvatar)
        if (!request.studentAvatar.isNullOrEmpty() && request.studentAvatar != "null") {
            Glide.with(this).load(request.studentAvatar).placeholder(R.drawable.avatar).into(imgAvatar)
        } else {
            imgAvatar.setImageResource(R.drawable.avatar)
        }

        findViewById<TextView>(R.id.txtReason).text = request.reason

        // Hiển thị ghi chú phụ huynh
        val tvLabelNotes = findViewById<TextView>(R.id.tvLabelParentNotes)
        val tvParentNotes = findViewById<TextView>(R.id.txtParentNotes)
        if (!request.parentNotes.isNullOrEmpty()) {
            tvLabelNotes.visibility = View.VISIBLE
            tvParentNotes.visibility = View.VISIBLE
            tvParentNotes.text = request.parentNotes
        } else {
            tvLabelNotes.visibility = View.GONE
            tvParentNotes.visibility = View.GONE
        }

        // Hiển thị thông tin hoàn tiền ăn
        findViewById<View>(R.id.layoutRefundInfo).visibility = if (request.isMealFeeDeducted) View.VISIBLE else View.GONE

        // Xử lý ảnh minh chứng
        val container = findViewById<GridLayout>(R.id.layoutEvidenceContainer)
        container.removeAllViews()

        if (!request.evidenceUrl.isNullOrEmpty()) {
            val imageView = ShapeableImageView(this)
            val displayMetrics = resources.displayMetrics
            val imageHeight = (180 * displayMetrics.density).toInt()
            
            val params = GridLayout.LayoutParams()
            params.width = GridLayout.LayoutParams.MATCH_PARENT
            params.height = imageHeight
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            
            val radius = 12 * displayMetrics.density
            imageView.shapeAppearanceModel = imageView.shapeAppearanceModel.toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius)
                .build()

            Glide.with(this).load(request.evidenceUrl).placeholder(R.drawable.logo).into(imageView)
            container.addView(imageView)
        }

        val btnApprove = findViewById<MaterialButton>(R.id.btnApprove)
        val btnReject = findViewById<MaterialButton>(R.id.btnReject)

        if (request.status != "Pending") {
            findViewById<View>(R.id.layoutFooter).visibility = View.GONE
        }

        btnApprove.setOnClickListener { handleAction(request.requestId, "APPROVE") }
        btnReject.setOnClickListener { handleAction(request.requestId, "REJECT") }
    }

    private fun handleAction(requestId: Int, action: String) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val newStatus = if (action == "APPROVE") "Approved" else "Rejected"
        
        Toast.makeText(this, "Đang xử lý...", Toast.LENGTH_SHORT).show()

        val json = JSONObject().apply {
            put("status", newStatus)
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/teacher/leave-requests/$requestId/status")
            .addHeader("Authorization", "Bearer $token")
            .put(body)
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Thao tác thành công!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Lỗi khi cập nhật trạng thái", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }
}
