package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.util.Log
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ManHinhNhanXetVaDanhGia : AppCompatActivity() {

    private lateinit var edtComment: EditText
    private lateinit var edtPhysical: EditText
    private lateinit var edtCognitive: EditText
    private lateinit var edtLanguage: EditText
    private lateinit var edtAesthetic: EditText
    private lateinit var edtEmotional: EditText
    private var studentId: Int = -1
    private var classId: Int = -1
    private var assessmentMonth: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_nhan_xet_va_danh_gia)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        studentId = intent.getIntExtra("STUDENT_ID", -1)
        classId = intent.getIntExtra("CLASS_ID", 1) // Mặc định là 1 nếu không truyền
        
        // Lấy tháng hiện tại làm mặc định MM-YYYY
        val cal = java.util.Calendar.getInstance()
        assessmentMonth = String.format("%02d-%d", cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.YEAR))
        
        loadStudentData()

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_submit_report).setOnClickListener { saveData() }
        
        findViewById<View>(R.id.btn_edit_skills).setOnClickListener {
            enableSkillEdits(true)
        }
    }

    private fun initViews() {
        edtComment = findViewById(R.id.edt_teacher_comment)
        edtPhysical = findViewById(R.id.edt_skill_physical)
        edtCognitive = findViewById(R.id.edt_skill_cognitive)
        edtLanguage = findViewById(R.id.edt_skill_language)
        edtAesthetic = findViewById(R.id.edt_skill_aesthetic)
        edtEmotional = findViewById(R.id.edt_skill_emotional)
    }

    private fun enableSkillEdits(enabled: Boolean) {
        edtPhysical.isEnabled = enabled
        edtCognitive.isEnabled = enabled
        edtLanguage.isEnabled = enabled
        edtAesthetic.isEnabled = enabled
        edtEmotional.isEnabled = enabled
        if (enabled) edtPhysical.requestFocus()
    }

    private fun loadStudentData() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        // Sử dụng API lấy đánh giá của cả lớp theo tháng: GET /teacher/classes/{classId}/assessments?month=MM-YYYY
        val url = "https://web-test.kindercare.app/api/v1/teacher/classes/$classId/assessments?month=$assessmentMonth"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get().build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val json = JSONObject(body)
                        val dataArray = json.optJSONArray("data")
                        
                        if (dataArray != null) {
                            // Tìm đánh giá của học sinh cụ thể trong mảng trả về
                            var found = false
                            for (i in 0 until dataArray.length()) {
                                val item = dataArray.getJSONObject(i)
                                if (item.optInt("studentId") == studentId) {
                                    runOnUiThread {
                                        edtComment.setText(item.optString("teacherComment"))
                                        edtPhysical.setText(item.optInt("physicalScore").toString())
                                        edtCognitive.setText(item.optInt("cognitiveScore").toString())
                                        edtLanguage.setText(item.optInt("languageScore").toString())
                                        edtAesthetic.setText(item.optInt("aestheticScore").toString())
                                        edtEmotional.setText(item.optInt("socioEmotionalScore").toString())
                                    }
                                    found = true
                                    break
                                }
                            }
                            
                            if (!found) {
                                runOnUiThread {
                                    // Reset fields if no assessment found for this student this month
                                    edtComment.setText("")
                                    edtPhysical.setText("5")
                                    edtCognitive.setText("5")
                                    edtLanguage.setText("5")
                                    edtAesthetic.setText("5")
                                    edtEmotional.setText("5")
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    private fun saveData() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        // Chuẩn bị item đánh giá cho học sinh hiện tại
        val assessmentObj = JSONObject().apply {
            put("studentId", studentId)
            put("physicalScore", edtPhysical.text.toString().toIntOrNull() ?: 5)
            put("cognitiveScore", edtCognitive.text.toString().toIntOrNull() ?: 5)
            put("languageScore", edtLanguage.text.toString().toIntOrNull() ?: 5)
            put("socioEmotionalScore", edtEmotional.text.toString().toIntOrNull() ?: 5)
            put("aestheticScore", edtAesthetic.text.toString().toIntOrNull() ?: 5)
            put("teacherComment", edtComment.text.toString())
        }

        // Đóng gói vào mảng assessments theo API POST /teacher/classes/{classId}/assessments
        val json = JSONObject().apply {
            put("month", assessmentMonth)
            val array = org.json.JSONArray()
            array.put(assessmentObj)
            put("assessments", array)
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val url = "https://web-test.kindercare.app/api/v1/teacher/classes/$classId/assessments"
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .post(body).build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val resBody = response.body?.string()
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Lưu đánh giá thành công!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Log.e("SAVE_ASSESSMENT", "Error: $resBody")
                            Toast.makeText(this, "Lỗi khi lưu: ${response.code}", Toast.LENGTH_SHORT).show()
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
