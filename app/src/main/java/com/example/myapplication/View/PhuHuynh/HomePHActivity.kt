package com.example.myapplication.View.PhuHuynh

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.HomeItem
import com.example.myapplication.Model.MomentModel
import com.example.myapplication.R
import com.example.myapplication.Utils.NavigationUtils
import com.example.myapplication.View.Adapter.PhuHuynhAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomePHActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_phactivity)
        
        val rootView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindStudentCard()
        setupUtilities()
        setupMomentsTimeline()
        setupBottomNavigation()
        setupHeaderActions()
    }

    private fun bindStudentCard() {
        val student = DataManager.studentList.firstOrNull() ?: return
        findViewById<TextView>(R.id.tvStudentName)?.text = student.FullName
        findViewById<TextView>(R.id.tvClassName)?.text = "Lớp: Mầm Non 1"
        findViewById<ImageView>(R.id.imgAvatar)?.setImageResource(student.avatarResId)
        
        findViewById<View>(R.id.cardStudent).setOnClickListener {
            startActivity(Intent(this, ChildProfileActivity::class.java))
        }
    }

    private fun setupUtilities() {
        // Group 1: Sức khỏe & An toàn
        findViewById<LinearLayout>(R.id.btnFeatureMedication).setOnClickListener {
             startActivity(Intent(this, MedicationRequestPHActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeaturePickUp).setOnClickListener {
             startActivity(Intent(this, AttendanceCalendarActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeatureBMI).setOnClickListener {
             startActivity(Intent(this, HealthActivity::class.java))
        }

        // Group 2: Hành chính & Sinh hoạt
        findViewById<LinearLayout>(R.id.btnFeatureLeave).setOnClickListener {
             startActivity(Intent(this, LeaveRequestActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeatureMenu).setOnClickListener {
             startActivity(Intent(this, MealDetailActivity::class.java))
        }

        // Group 3: Tài chính & Hồ sơ
        findViewById<LinearLayout>(R.id.btnFeatureFee).setOnClickListener {
             startActivity(Intent(this, FeeActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeatureProfile).setOnClickListener {
             startActivity(Intent(this, ChildProfileActivity::class.java))
        }
    }

    private fun setupMomentsTimeline() {
        val rvMoments = findViewById<RecyclerView>(R.id.rvMoments)
        val momentItems = listOf(
            HomeItem.MomentItem(MomentModel("Hôm nay, 18/05", true, listOf(R.drawable.logo, R.drawable.logo))),
            HomeItem.MomentItem(MomentModel("Hôm qua, 17/05", false, listOf(R.drawable.logo)))
        )
        rvMoments.layoutManager = LinearLayoutManager(this)
        rvMoments.adapter = PhuHuynhAdapter(momentItems) { }
    }

    private fun setupHeaderActions() {
        findViewById<View>(R.id.btn_thongbao).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
        findViewById<View>(R.id.btn_profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationPH(this, "OVERVIEW")
    }
}
