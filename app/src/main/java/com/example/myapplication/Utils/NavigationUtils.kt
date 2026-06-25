package com.example.myapplication.Utils

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.View.GiaoVien.*
import com.example.myapplication.View.PhuHuynh.*

object NavigationUtils {
    
    private val ACTIVE_COLOR = Color.parseColor("#00A86B")
    private val INACTIVE_COLOR = Color.parseColor("#333333")

    fun setupBottomNavigationPH(activity: Activity, activeTab: String) {
        val btnProfile = activity.findViewById<LinearLayout>(R.id.btnProfile)
        val btnFee = activity.findViewById<LinearLayout>(R.id.btnFee)
        val btnOverview = activity.findViewById<LinearLayout>(R.id.btnOverview)
        val btnActivity = activity.findViewById<LinearLayout>(R.id.btnActivity)
        val btnContact = activity.findViewById<LinearLayout>(R.id.btnContact)

        fun setItemActive(imgId: Int, txtId: Int, isActive: Boolean) {
            val img = activity.findViewById<ImageView>(imgId)
            val txt = activity.findViewById<TextView>(txtId)
            if (isActive) {
                img.setColorFilter(ACTIVE_COLOR)
                txt.setTextColor(ACTIVE_COLOR)
                txt.setTypeface(null, Typeface.BOLD)
            } else {
                img.setColorFilter(INACTIVE_COLOR)
                txt.setTextColor(INACTIVE_COLOR)
                txt.setTypeface(null, Typeface.NORMAL)
            }
        }

        setItemActive(R.id.ivNavProfile, R.id.tvNavProfile, activeTab == "PROFILE")
        setItemActive(R.id.ivNavFee, R.id.tvNavFee, activeTab == "FEE")
        setItemActive(R.id.ivNavOverview, R.id.tvNavOverview, activeTab == "OVERVIEW")
        setItemActive(R.id.ivNavActivity, R.id.tvNavActivity, activeTab == "ACTIVITY")
        setItemActive(R.id.ivNavContact, R.id.tvNavContact, activeTab == "CONTACT")

        btnProfile.setOnClickListener {
            if (activeTab != "PROFILE") {
                activity.startActivity(Intent(activity, ChildProfileActivity::class.java))
                activity.overridePendingTransition(0, 0)
                if (activeTab != "OVERVIEW") activity.finish()
            }
        }
        btnFee.setOnClickListener {
            if (activeTab != "FEE") {
                activity.startActivity(Intent(activity, FeeActivity::class.java))
                activity.overridePendingTransition(0, 0)
                if (activeTab != "OVERVIEW") activity.finish()
            }
        }
        btnOverview.setOnClickListener {
            if (activeTab != "OVERVIEW") {
                activity.startActivity(Intent(activity, HomePHActivity::class.java))
                activity.overridePendingTransition(0, 0)
                activity.finish()
            }
        }
        btnActivity.setOnClickListener {
            if (activeTab != "ACTIVITY") {
                activity.startActivity(Intent(activity, ActivityActivity::class.java))
                activity.overridePendingTransition(0, 0)
                if (activeTab != "OVERVIEW") activity.finish()
            }
        }
        btnContact.setOnClickListener {
            if (activeTab != "CONTACT") {
                activity.startActivity(Intent(activity, ListChatActivity::class.java))
                activity.overridePendingTransition(0, 0)
                if (activeTab != "OVERVIEW") activity.finish()
            }
        }
    }

    fun setupBottomNavigationGV(activity: Activity, activeTab: String) {
        val btnHoatDong = activity.findViewById<LinearLayout>(R.id.btnHoatDong)
        val btnDanhSachLop = activity.findViewById<LinearLayout>(R.id.btnDanhSachLop)
        val btnBangDieuKhien = activity.findViewById<LinearLayout>(R.id.btnBangDieuKhien)
        val btnDiemDanh = activity.findViewById<LinearLayout>(R.id.btnDiemDanh)
        val btnLienLac = activity.findViewById<LinearLayout>(R.id.btnLienLac)

        fun setItemActive(imgId: Int, txtId: Int, isActive: Boolean) {
            val img = activity.findViewById<ImageView>(imgId)
            val txt = activity.findViewById<TextView>(txtId)
            if (isActive) {
                img.setColorFilter(ACTIVE_COLOR)
                txt.setTextColor(ACTIVE_COLOR)
                txt.setTypeface(null, Typeface.BOLD)
            } else {
                img.setColorFilter(INACTIVE_COLOR)
                txt.setTextColor(INACTIVE_COLOR)
                txt.setTypeface(null, Typeface.NORMAL)
            }
        }

        setItemActive(R.id.ivNavHoatDong, R.id.tvNavHoatDong, activeTab == "ACTIVITY")
        setItemActive(R.id.ivNavDanhSachLop, R.id.tvNavDanhSachLop, activeTab == "CLASS_LIST")
        setItemActive(R.id.ivNavBangDieuKhien, R.id.tvNavBangDieuKhien, activeTab == "DASHBOARD")
        setItemActive(R.id.ivNavDiemDanh, R.id.tvNavDiemDanh, activeTab == "ATTENDANCE")
        setItemActive(R.id.ivNavLienLac, R.id.tvNavLienLac, activeTab == "CONTACT")

        btnHoatDong.setOnClickListener {
            if (activeTab != "ACTIVITY") {
                activity.startActivity(Intent(activity, ManHinhHoatDong::class.java))
                activity.overridePendingTransition(0, 0)
                if (activeTab != "DASHBOARD") activity.finish()
            }
        }
        btnDanhSachLop.setOnClickListener {
            if (activeTab != "CLASS_LIST") {
                activity.startActivity(Intent(activity, ManHinhDanhSachLop::class.java))
                activity.overridePendingTransition(0, 0)
                if (activeTab != "DASHBOARD") activity.finish()
            }
        }
        btnBangDieuKhien.setOnClickListener {
            if (activeTab != "DASHBOARD") {
                activity.startActivity(Intent(activity, ManHinhBangDieuKhien::class.java))
                activity.overridePendingTransition(0, 0)
                activity.finish()
            }
        }
        btnDiemDanh.setOnClickListener {
            if (activeTab != "ATTENDANCE") {
                activity.startActivity(Intent(activity, ManHinhDiemDanh::class.java))
                activity.overridePendingTransition(0, 0)
                if (activeTab != "DASHBOARD") activity.finish()
            }
        }
        btnLienLac.setOnClickListener {
            if (activeTab != "CONTACT") {
                activity.startActivity(Intent(activity, ManHinhChatGiaoVien::class.java))
                activity.overridePendingTransition(0, 0)
                if (activeTab != "DASHBOARD") activity.finish()
            }
        }
    }
}
