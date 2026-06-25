package com.example.myapplication.View.PhuHuynh

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
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
import com.example.myapplication.View.Adapter.MedicationHistoryPHAdapter
import com.google.android.material.button.MaterialButton
import okhttp3.Request
import org.json.JSONObject

class MedicationRequestPHActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: MedicationHistoryPHAdapter
    private val medicationHistory = mutableListOf<MedicationRequestModel>()
    private lateinit var btnBack: ImageView
    private lateinit var btnAdd: MaterialButton

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

        val studentId = DataManager.studentList.firstOrNull()?.StudentID ?: -1
        if (studentId != -1) {
            fetchMedicationRequests(studentId)
        }

        btnBack.setOnClickListener { finish() }
        btnAdd.setOnClickListener {
            Toast.makeText(this, "Tính năng tạo yêu cầu mới đang phát triển", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        rvHistory = findViewById(R.id.rvMedicationHistory)
        btnBack = findViewById(R.id.btnBack)
        btnAdd = findViewById(R.id.btnAddNewRequest)
    }

    private fun setupRecyclerView() {
        rvHistory.layoutManager = LinearLayoutManager(this)
        adapter = MedicationHistoryPHAdapter(medicationHistory)
        rvHistory.adapter = adapter
    }

    private fun fetchMedicationRequests(studentId: Int) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val client = DataManager.okHttpClient
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children/$studentId/medication-requests")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    Log.d("MEDICATION_API", "Kết quả: $body")
                    
                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val dataArray = jsonResponse.optJSONArray("data")
                        
                        val tempHistory = mutableListOf<MedicationRequestModel>()
                        if (dataArray != null) {
                            for (i in 0 until dataArray.length()) {
                                val obj = dataArray.getJSONObject(i)
                                tempHistory.add(MedicationRequestModel(
                                    MedRequestID = obj.optInt("medRequestId"),
                                    StudentID = obj.optInt("studentId"),
                                    ParentID = obj.optInt("parentId"),
                                    RequestDate = obj.optLong("requestDate"),
                                    MedicineDetails = obj.optString("medicineDetails"),
                                    Dosage = obj.optString("dosage"),
                                    Frequency = obj.optString("frequency"),
                                    TimeToTake = obj.optString("timeToTake"),
                                    ParentNote = obj.optString("parentNote"),
                                    MedicineImageURL = obj.optString("medicineImageUrl"),
                                    Status = obj.optString("status", "Pending"),
                                    TeacherNote = obj.optString("teacherNote")
                                ))
                            }
                        }
                        
                        runOnUiThread {
                            medicationHistory.clear()
                            medicationHistory.addAll(tempHistory)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MEDICATION_API", "Lỗi: ${e.message}")
            }
        }.start()
    }
}
