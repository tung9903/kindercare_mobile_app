package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily

class ManHinhChiTietDonXinNghi : AppCompatActivity() {

    private var requestId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chi_tiet_don_xin_nghi)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requestId = intent.getIntExtra("REQUEST_ID", -1)
        
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadRequestDetails()
    }

    private fun loadRequestDetails() {
        val request = DataManager.leaveRequests.find { it.RequestID == requestId }
        if (request == null) {
            Toast.makeText(this, "Không tìm thấy thông tin đơn", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<TextView>(R.id.txtTitle).text = "Chi tiết đơn xin nghỉ – Bé ${request.studentName}"
        findViewById<ImageView>(R.id.imgAvatar).setImageResource(request.avatarResId)
        
        val parentLayout = findViewById<ImageView>(R.id.imgAvatar).parent as LinearLayout
        val infoLayout = parentLayout.getChildAt(1) as LinearLayout
        (infoLayout.getChildAt(0) as TextView).text = request.studentName
        (infoLayout.getChildAt(1) as TextView).text = "PH: ${request.parentName}"

        findViewById<TextView>(R.id.txtReason).text = request.Reason

        val container = findViewById<GridLayout>(R.id.layoutEvidenceContainer)
        container.removeAllViews()

        val displayMetrics = resources.displayMetrics
        val imageHeight = (120 * displayMetrics.density).toInt()

        request.evidenceImages.forEach { resId ->
            val imageView = ShapeableImageView(this)
            val params = GridLayout.LayoutParams()
            
            params.width = 0
            params.height = imageHeight
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            
            val margin = (4 * displayMetrics.density).toInt()
            params.setMargins(margin, margin, margin, margin)
            imageView.layoutParams = params
            
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageResource(resId)
            
            val radius = 12 * displayMetrics.density
            val shapeAppearanceModel = imageView.shapeAppearanceModel.toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius)
                .build()
            imageView.shapeAppearanceModel = shapeAppearanceModel
            
            container.addView(imageView)
        }

        val btnApprove = findViewById<MaterialButton>(R.id.btnApprove)
        val btnReject = findViewById<MaterialButton>(R.id.btnReject)

        if (request.Status != "Pending") {
            findViewById<View>(R.id.layoutFooter).visibility = View.GONE
        }

        btnApprove.setOnClickListener {
            DataManager.approveRequest(request.RequestID)
            Toast.makeText(this, "Đã duyệt đơn của ${request.studentName}", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnReject.setOnClickListener {
            DataManager.rejectRequest(request.RequestID)
            Toast.makeText(this, "Đã từ chối đơn của ${request.studentName}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
