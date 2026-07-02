package com.example.myapplication.View.PhuHuynh

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.R
import com.example.myapplication.Utils.NavigationUtils
import okhttp3.Request
import org.json.JSONObject

class ChildProfileActivity : AppCompatActivity() {

    private lateinit var imgChildAvatar: ImageView
    private lateinit var tvStudentName: TextView
    private lateinit var tvEnrollmentStatus: TextView
    private lateinit var tvNickname: TextView
    private lateinit var tvSchoolName: TextView
    private lateinit var tvSchoolAddress: TextView
    private lateinit var tvBuildingInfo: TextView
    private lateinit var layoutTeachers: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_child_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()

        // Luôn ưu tiên hiển thị bé đang được chọn từ DataManager
        val selectedChild = DataManager.selectedChild
        if (selectedChild != null) {
            bindChildData(selectedChild)
        } else {
            fetchChildData()
        }

        setupBottomNavigation()
    }

    private fun initViews() {
        imgChildAvatar = findViewById(R.id.imgChildAvatar)
        tvStudentName = findViewById(R.id.tvStudentName)
        tvEnrollmentStatus = findViewById(R.id.tvEnrollmentStatus)
        tvNickname = findViewById(R.id.tvNickname)
        tvSchoolName = findViewById(R.id.tvSchoolName)
        tvSchoolAddress = findViewById(R.id.tvSchoolAddress)
        tvBuildingInfo = findViewById(R.id.tvBuildingInfo)
        layoutTeachers = findViewById(R.id.layoutTeachers)
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnNotification).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
        findViewById<View>(R.id.imgAvatarHeader).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<View>(R.id.btnRequestUpdate).setOnClickListener {
            startActivity(Intent(this, RequestChangeActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationPH(this, "PROFILE")
    }

    private fun fetchChildData() {
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
                            runOnUiThread {
                                DataManager.selectedChild = child
                                bindChildData(child)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CHILD_PROFILE", "Error: ${e.message}")
            }
        }.start()
    }

    private fun bindChildData(obj: JSONObject) {
        tvStudentName.text = obj.optString("fullName", "Học sinh")
        tvNickname.text = "Mối quan hệ: " + obj.optString("relationship", "Bé")
        
        val status = obj.optString("enrollmentStatus", "Active")
        tvEnrollmentStatus.text = if (status == "Active") "ĐANG HỌC" else "NGỪNG HỌC"
        tvEnrollmentStatus.setBackgroundResource(if (status == "Active") R.drawable.bg_badge_status_green else R.drawable.bg_badge_gray)

        val avatarUrl = obj.optString("avatarUrl")
        if (avatarUrl.isNotEmpty() && avatarUrl != "null") {
            Glide.with(this).load(avatarUrl).placeholder(R.drawable.logo).into(imgChildAvatar)
        }

        // Detail Grid Boxes
        val dob = obj.optLong("dateOfBirth", 0)
        setupBox(R.id.boxNgaySinh, "NGÀY SINH", if (dob > 0) DateHelper.formatLongToDate(dob) else "N/A")
        setupBox(R.id.boxGioiTinh, "GIỚI TÍNH", obj.optString("gender", "N/A"))
        setupBox(R.id.boxMaHocSinh, "MÃ HỌC SINH", "KC-" + obj.optInt("studentId"))
        
        setupBox(R.id.boxLop, "LỚP", obj.optString("className", "Chưa xếp lớp"))
        setupBox(R.id.boxCoSo, "CƠ SỞ", obj.optString("campusName", "N/A"))
        setupBox(R.id.boxDiUng, "DỊ ỨNG", obj.optString("allergies", "Không có"))
        setupBox(R.id.boxNienKhoa, "NIÊN KHÓA", obj.optString("academicYearName", "N/A"))

        // School Section
        tvSchoolName.text = "Trường mầm non KinderCare"
        tvSchoolAddress.text = obj.optString("campusAddress", "Địa chỉ trường học")
        tvBuildingInfo.text = "Tòa nhà: " + obj.optString("buildingName", "N/A")

        // Teachers Section
        layoutTeachers.removeAllViews()
        val teachers = obj.optJSONArray("teachers")
        if (teachers != null) {
            val inflater = LayoutInflater.from(this)
            for (i in 0 until teachers.length()) {
                val t = teachers.getJSONObject(i)
                val tView = inflater.inflate(R.layout.item_user_directory, layoutTeachers, false)
                
                tView.findViewById<TextView>(R.id.tvUserFullName).text = t.optString("fullName")
                tView.findViewById<TextView>(R.id.tvUserRole).text = "Giáo viên " + t.optString("roleInClass", "Phụ trách")
                val phone = t.optString("phoneNumber")
                tView.findViewById<TextView>(R.id.tvUserContact).text = phone
                
                tView.findViewById<View>(R.id.btnCall).setOnClickListener {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    startActivity(intent)
                }
                
                layoutTeachers.addView(tView)
            }
        }
    }

    private fun setupBox(layoutId: Int, label: String, value: String) {
        val view = findViewById<View>(layoutId) ?: return
        view.findViewById<TextView>(R.id.tvBoxLabel).text = label
        view.findViewById<TextView>(R.id.tvBoxValue).text = value
    }
}
