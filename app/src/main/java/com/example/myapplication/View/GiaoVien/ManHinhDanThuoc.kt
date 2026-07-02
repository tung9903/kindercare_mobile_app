package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.MedicationRequestModel
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.MedicationAdapter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Calendar

class ManHinhDanThuoc : AppCompatActivity() {
    
    private lateinit var rvMedication: RecyclerView
    private lateinit var adapter: MedicationAdapter
    private lateinit var tvRequestCount: TextView
    private var medicationList = mutableListOf<MedicationRequestModel>()
    private var classId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_dan_thuoc)

        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
        tvRequestCount = findViewById(R.id.tvRequestCount)
        rvMedication = findViewById(R.id.rvMedication)
        
        setupRecyclerView()
        
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }.timeInMillis / 1000
        
        fetchMedicationRequests(classId, today)
    }

    private fun setupRecyclerView() {
        adapter = MedicationAdapter(medicationList) { item, action, note ->
            handleAction(item, action, note)
        }
        rvMedication.layoutManager = LinearLayoutManager(this)
        rvMedication.adapter = adapter
    }

    private fun handleAction(item: MedicationRequestModel, action: String, note: String?) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val newStatus = when (action) {
            "ACCEPT" -> "Accepted"
            "REJECT" -> "Rejected"
            "COMPLETE" -> "Completed"
            else -> return
        }

        val json = JSONObject().apply {
            put("status", newStatus)
            if (note != null) put("teacherNote", note)
        }
        
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/teacher/medical-requests/${item.medRequestId}")
            .addHeader("Authorization", "Bearer $token")
            .put(body)
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Đã cập nhật trạng thái yêu cầu!", Toast.LENGTH_SHORT).show()
                            val today = Calendar.getInstance().apply { 
                                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) 
                            }.timeInMillis / 1000
                            fetchMedicationRequests(classId, today)
                        }
                    }
                }
            } catch (e: Exception) { }
        }.start()
    }

    private fun fetchMedicationRequests(classId: Int, date: Long) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val url = "https://web-test.kindercare.app/api/v1/teacher/classes/$classId/medical-requests?date=$date"
        val request = Request.Builder()
            .url(url)
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
                        
                        val tempItems = mutableListOf<MedicationRequestModel>()
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                tempItems.add(parseMedicationRequest(it.getJSONObject(i)))
                            }
                        }

                        runOnUiThread {
                            medicationList.clear()
                            medicationList.addAll(tempItems)
                            adapter.notifyDataSetChanged()
                            updateSummary()
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    private fun parseMedicationRequest(obj: JSONObject): MedicationRequestModel {
        return MedicationRequestModel(
            medRequestId = obj.optInt("medRequestId"),
            studentId = obj.optInt("studentId"),
            studentName = obj.optString("studentName", "Học sinh"),
            studentAvatar = obj.optString("studentAvatar"),
            parentId = obj.optInt("parentId"),
            requestDate = obj.optLong("requestDate"),
            medicineDetails = obj.optString("medicineDetails"),
            dosage = obj.optString("dosage"),
            frequency = obj.optString("frequency"),
            timeToTake = obj.optString("timeToTake"),
            parentNote = obj.optString("parentNote"),
            medicineImageUrl = obj.optString("medicineImageUrl"),
            status = obj.optString("status", "Pending"),
            teacherNote = obj.optString("teacherNote")
        )
    }

    private fun updateSummary() {
        val pendingCount = medicationList.count { it.status == "Pending" || it.status == "Accepted" }
        tvRequestCount.text = "$pendingCount Yêu cầu"
    }
}
