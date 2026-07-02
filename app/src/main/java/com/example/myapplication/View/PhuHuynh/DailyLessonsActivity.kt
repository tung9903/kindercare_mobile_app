package com.example.myapplication.View.PhuHuynh

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DailyLesson
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.LessonPHAdapter
import okhttp3.Request
import org.json.JSONObject
import java.util.Calendar

class DailyLessonsActivity : AppCompatActivity() {

    private lateinit var rvLessons: RecyclerView
    private lateinit var adapter: LessonPHAdapter
    private val lessonList = mutableListOf<DailyLesson>()
    private var currentStudentId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_daily_lessons)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        currentStudentId = DataManager.selectedChild?.optInt("studentId") ?: -1
        
        setupRecyclerView()
        
        if (currentStudentId != -1) {
            fetchDailyLessons()
        }
    }

    private fun setupRecyclerView() {
        rvLessons = findViewById(R.id.rvLessons)
        adapter = LessonPHAdapter(lessonList)
        rvLessons.layoutManager = LinearLayoutManager(this)
        rvLessons.adapter = adapter
    }

    private fun fetchDailyLessons() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis / 1000

        val url = "https://web-test.kindercare.app/api/v1/parent/children/$currentStudentId/daily-lessons?date=$today"
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
                        val temp = mutableListOf<DailyLesson>()
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                val obj = it.getJSONObject(i)
                                temp.add(DailyLesson(
                                    lessonLogId = obj.optInt("lessonLogId"),
                                    classId = obj.optInt("classId"),
                                    lessonDate = obj.optLong("lessonDate"),
                                    subjectName = obj.optString("subjectName"),
                                    lessonTitle = obj.optString("lessonTitle"),
                                    details = obj.optString("details"),
                                    iconType = if (obj.isNull("iconType")) null else obj.optString("iconType"),
                                    createdAt = obj.optLong("createdAt"),
                                    updatedAt = obj.optLong("updatedAt")
                                ))
                            }
                        }
                        runOnUiThread {
                            lessonList.clear()
                            lessonList.addAll(temp)
                            adapter.notifyDataSetChanged()
                        }
                    }
                    return@use
                }
            } catch (e: Exception) {
                Log.e("LESSON_API", "Error: ${e.message}")
            }
        }.start()
    }
}
