package com.example.myapplication.View.PhuHuynh

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.google.android.material.progressindicator.LinearProgressIndicator
import okhttp3.Request
import org.json.JSONObject

class AssessmentPHActivity : AppCompatActivity() {

    private var currentStudentId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assessment_ph)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        // Lấy studentId từ DataManager
        currentStudentId = DataManager.selectedChild?.optInt("studentId") ?: -1
        
        if (currentStudentId != -1) {
            fetchAssessments()
        }
    }

    private fun fetchAssessments() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val url = "https://web-test.kindercare.app/api/v1/parent/children/$currentStudentId/assessments"
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
                        val json = JSONObject(body)
                        val dataArray = json.optJSONArray("data")
                        if (dataArray != null && dataArray.length() > 0) {
                            val assessment = dataArray.getJSONObject(0) // Lấy đánh giá mới nhất
                            runOnUiThread {
                                bindData(assessment)
                            }
                        }
                    }
                    return@use
                }
            } catch (e: Exception) {
                Log.e("ASSESSMENT_API", "Error: ${e.message}")
            }
        }.start()
    }

    private fun bindData(data: JSONObject) {
        findViewById<TextView>(R.id.tvMonth).text = "Tháng " + data.optString("assessmentMonth")
        findViewById<TextView>(R.id.tvTeacherComment).text = data.optString("teacherComment", "Chưa có nhận xét.")

        // Bind scores
        setupScoreRow(R.id.rowPhysical, "Thể chất", data.optInt("physicalScore"))
        setupScoreRow(R.id.rowCognitive, "Nhận thức", data.optInt("cognitiveScore"))
        setupScoreRow(R.id.rowLanguage, "Ngôn ngữ", data.optInt("languageScore"))
        setupScoreRow(R.id.rowAesthetic, "Thẩm mỹ", data.optInt("aestheticScore"))
        setupScoreRow(R.id.rowEmotional, "Tình cảm - Xã hội", data.optInt("socioEmotionalScore"))
    }

    private fun setupScoreRow(rowId: Int, label: String, score: Int) {
        val row = findViewById<View>(rowId) ?: return
        row.findViewById<TextView>(R.id.tvScoreLabel).text = label
        row.findViewById<TextView>(R.id.tvScoreValue).text = "$score/10"
        row.findViewById<LinearProgressIndicator>(R.id.progressScore).progress = score * 10
    }
}
