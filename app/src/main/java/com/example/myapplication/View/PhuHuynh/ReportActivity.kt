package com.example.myapplication.View.PhuHuynh

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.ReportModel
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.ReportAdapter

class ReportActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var reportAdapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report)

        // Ánh xạ các View và sử dụng dấu ? (safe call) phòng trường hợp ID sai lệch
        val mainView = findViewById<android.view.View>(R.id.main)
        val layoutHeader = findViewById<android.view.View>(R.id.viewHeaderBg)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        recyclerView = findViewById(R.id.recyclerViewReports)

        // Xử lý Edge-to-Edge an toàn (Thêm dấu ? sau layoutHeader và recyclerView)
        mainView?.let { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                // Kiểm tra nếu layoutHeader khác null thì mới set padding
                layoutHeader?.let { header ->
                    header.setPadding(
                        header.paddingLeft,
                        systemBars.top,
                        header.paddingRight,
                        header.paddingBottom
                    )
                }

                // Kiểm tra nếu recyclerView khác null thì mới set padding
                recyclerView?.let { list ->
                    list.setPadding(
                        list.paddingLeft,
                        list.paddingTop,
                        list.paddingRight,
                        systemBars.bottom
                    )
                }

                insets
            }
        }

        // Sự kiện nút quay lại
        btnBack?.setOnClickListener {
            finish()
        }

        // Khởi tạo hiển thị cho danh sách
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Dữ liệu mẫu hiển thị
        val reportList = listOf(
            ReportModel(
                "Xác nhận dặn thuốc",
                "Nhà trường đã tiếp nhận thông tin dặn thuốc cho bé Ngọc Châu từ phụ huynh lúc 07:45 AM.",
                "Hôm qua"
            ),
            ReportModel(
                "Xác nhận dặn thuốc",
                "Nhà trường đã tiếp nhận thông tin dặn thuốc cho bé Ngọc Châu từ phụ huynh lúc 07:45 AM.",
                "Hôm qua"
            ),
            ReportModel(
                "Xác nhận dặn thuốc",
                "Nhà trường đã tiếp nhận thông tin dặn thuốc cho bé Ngọc Châu từ phụ huynh lúc 07:45 AM.",
                "Hôm qua"
            )
        )

        // Gán adapter vào list
        reportAdapter = ReportAdapter(reportList)
        recyclerView.adapter = reportAdapter
    }
}