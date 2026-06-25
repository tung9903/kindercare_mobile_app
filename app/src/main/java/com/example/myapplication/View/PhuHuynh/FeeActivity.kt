package com.example.myapplication.View.PhuHuynh

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.Model.TuitionItem
import com.example.myapplication.View.Adapter.TuitionAdapter
import com.example.myapplication.Utils.NavigationUtils

class FeeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fee)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val rvTuitionList = findViewById<RecyclerView>(R.id.rvTuitionList)
        rvTuitionList.layoutManager = LinearLayoutManager(this)

        val tuitionData = listOf(
            TuitionItem("học phí định kỳ", isPaid = false)
        )

        val adapter = TuitionAdapter(tuitionData)
        rvTuitionList.adapter = adapter

        findViewById<View>(R.id.ivNotification).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        findViewById<View>(R.id.ivAvatar).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationPH(this, "FEE")
    }
}
