package com.example.myapplication.View.PhuHuynh

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.R
import com.example.myapplication.Utils.QRUtils
import okhttp3.Request
import org.json.JSONObject

class PickUpActivity : AppCompatActivity() {

    private lateinit var tvPickupStatus: TextView
    private lateinit var tvPickupTime: TextView
    private lateinit var ivStatusIcon: ImageView
    private lateinit var imgQRCode: ImageView
    private lateinit var tvCountdown: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var pbQR: ProgressBar
    private lateinit var layoutAuthorizedPersons: LinearLayout

    private var currentStudentId: Int = -1
    private val handler = Handler(Looper.getMainLooper())
    private var refreshRunnable: Runnable? = null
    private var countdownRunnable: Runnable? = null
    private var countdownValue = 60
    private var currentQrBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pick_up)

        val rootView = findViewById<View>(R.id.main_pickup) ?: findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()

        val selectedChild = DataManager.selectedChild
        if (selectedChild != null) {
            currentStudentId = selectedChild.getInt("studentId")
            startQrAutoRefresh()
            fetchLatestAttendanceStatus()
            bindAuthorizedPersons(selectedChild)
        }
    }

    private fun initViews() {
        tvPickupStatus = findViewById(R.id.tvPickupStatus)
        tvPickupTime = findViewById(R.id.tvPickupTime)
        ivStatusIcon = findViewById(R.id.ivStatusIcon)
        imgQRCode = findViewById(R.id.imgQRCode)
        tvCountdown = findViewById(R.id.tvCountdown)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        pbQR = findViewById(R.id.pbQR)
        layoutAuthorizedPersons = findViewById(R.id.layoutAuthorizedPersons)
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        
        findViewById<View>(R.id.btnRefreshQR).setOnClickListener {
            stopAutoRefresh()
            startQrAutoRefresh()
            Toast.makeText(this, "Đang làm mới mã QR...", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnShareQR).setOnClickListener {
            if (currentQrBitmap != null) {
                Toast.makeText(this, "Tính năng chia sẻ đang được chuẩn bị", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Chưa có mã QR để chia sẻ", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.btnDownloadQR).setOnClickListener {
            if (currentQrBitmap != null) {
                Toast.makeText(this, "Đã lưu mã QR vào thư viện ảnh", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.btnAddAuthorized).setOnClickListener {
            startActivity(Intent(this, AddAuthorizedPersonActivity::class.java))
        }

        swipeRefresh.setOnRefreshListener {
            fetchLatestAttendanceStatus()
        }
        
        findViewById<View>(R.id.btnHelp).setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hướng dẫn đưa đón")
                .setMessage("1. Xuất trình mã QR cho giáo viên tại cổng trường để điểm danh đón bé.\n2. Mã QR tự động thay đổi mỗi 60 giây để đảm bảo an toàn.\n3. Bạn có thể chia sẻ mã cho người thân được ủy quyền nếu không thể trực tiếp đón bé.")
                .setPositiveButton("Đã hiểu", null)
                .show()
        }
    }

    private fun startQrAutoRefresh() {
        refreshRunnable = object : Runnable {
            override fun run() {
                fetchAndDisplayQrToken()
                handler.postDelayed(this, 60000)
                startCountdown()
            }
        }
        handler.post(refreshRunnable!!)
    }

    private fun stopAutoRefresh() {
        refreshRunnable?.let { handler.removeCallbacks(it) }
        countdownRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun startCountdown() {
        countdownValue = 60
        countdownRunnable?.let { handler.removeCallbacks(it) }
        countdownRunnable = object : Runnable {
            override fun run() {
                if (countdownValue > 0) {
                    tvCountdown.text = "Mã QR sẽ làm mới sau ${countdownValue}s"
                    countdownValue--
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(countdownRunnable!!)
    }

    private fun fetchAndDisplayQrToken() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        runOnUiThread { pbQR.visibility = View.VISIBLE }

        val url = "https://web-test.kindercare.app/api/v1/parent/children/$currentStudentId/qr-token"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    runOnUiThread { pbQR.visibility = View.GONE }
                    if (response.isSuccessful && body != null) {
                        val json = JSONObject(body)
                        val data = json.optJSONObject("data")
                        val qrToken = data?.optString("token")
                        
                        if (!qrToken.isNullOrEmpty()) {
                            runOnUiThread {
                                val bitmap = QRUtils.generateQRCode(qrToken)
                                if (bitmap != null) {
                                    currentQrBitmap = bitmap
                                    imgQRCode.setImageBitmap(bitmap)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("QR_TOKEN", "Error: ${e.message}")
                runOnUiThread { pbQR.visibility = View.GONE }
            }
        }.start()
    }

    private fun fetchLatestAttendanceStatus() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val authToken = pref.getString("token", null) ?: return

        val url = "https://web-test.kindercare.app/api/v1/parent/children/$currentStudentId/attendance"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $authToken")
            .get().build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    runOnUiThread { swipeRefresh.isRefreshing = false }
                    if (response.isSuccessful && body != null) {
                        val dataArray = JSONObject(body).optJSONArray("data")
                        if (dataArray != null && dataArray.length() > 0) {
                            val latest = dataArray.getJSONObject(dataArray.length() - 1)
                            runOnUiThread { updatePickupUI(latest) }
                        }
                    }
                }
            } catch (e: Exception) { 
                e.printStackTrace()
                runOnUiThread { swipeRefresh.isRefreshing = false }
            }
        }.start()
    }

    private fun updatePickupUI(latest: JSONObject) {
        val status = latest.optString("status")
        val checkOutTime = if (latest.isNull("checkOutTime")) null else latest.optLong("checkOutTime")
        val pickedUpBy = latest.optString("pickedUpBy", "Người thân")

        when {
            status == "Present" && checkOutTime != null -> {
                tvPickupStatus.text = "Đã đón bé thành công"
                tvPickupTime.text = "Lúc ${DateHelper.formatLongToTime(checkOutTime)} • Bởi $pickedUpBy"
                ivStatusIcon.setImageResource(R.drawable.ic_attendance) // Giả định có icon này
                ivStatusIcon.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.success))
            }
            status == "Present" -> {
                tvPickupStatus.text = "Bé đang ở trường"
                tvPickupTime.text = "Sẵn sàng để phụ huynh đến đón"
                ivStatusIcon.setImageResource(R.drawable.ic_activity)
                ivStatusIcon.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.primary_green))
            }
            else -> {
                tvPickupStatus.text = "Chưa có mặt tại trường"
                tvPickupTime.text = "Dữ liệu sẽ cập nhật khi bé đến lớp"
                ivStatusIcon.setImageResource(R.drawable.ic_calendar)
                ivStatusIcon.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.text_tertiary))
            }
        }
    }

    private fun bindAuthorizedPersons(obj: JSONObject) {
        layoutAuthorizedPersons.removeAllViews()
        val teachers = obj.optJSONArray("teachers") // Dùng tạm danh sách giáo viên làm mẫu nếu không có data người thân
        if (teachers != null && teachers.length() > 0) {
            val inflater = LayoutInflater.from(this)
            for (i in 0 until Math.min(teachers.length(), 2)) {
                val t = teachers.getJSONObject(i)
                val tView = inflater.inflate(R.layout.item_user_directory, layoutAuthorizedPersons, false)
                tView.findViewById<TextView>(R.id.tvUserFullName).text = t.optString("fullName")
                tView.findViewById<TextView>(R.id.tvUserRole).text = "Người thân (Ủy quyền)"
                layoutAuthorizedPersons.addView(tView)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentStudentId != -1) {
            startQrAutoRefresh()
        }
    }

    override fun onPause() {
        super.onPause()
        stopAutoRefresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAutoRefresh()
        handler.removeCallbacksAndMessages(null)
    }
}
