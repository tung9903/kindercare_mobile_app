package com.example.myapplication.View.PhuHuynh

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityRequestChangeBinding
import java.util.Calendar

class RequestChangeActivity : AppCompatActivity() {

    // Khởi tạo View Binding để quản lý các thành phần giao diện
    private lateinit var binding: ActivityRequestChangeBinding
    private var fileUri: Uri? = null

    private val selectFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            fileUri = result.data?.data
            Toast.makeText(this, "Tải tệp minh chứng thành công!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRequestChangeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Xử lý Window Insets tràn viền (Edge-to-Edge) tự động từ hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Khởi tạo các sự kiện click nút bấm và nhập liệu
        initControlListeners()
    }

    private fun initControlListeners() {
        // Mở hộp thoại chọn ngày sinh khi click vào ô edt_new_dob
        binding.edtNewDob.setOnClickListener {
            openDatePickerDialog()
        }

        // Sự kiện click vào vùng tải file đính kèm lên
        binding.layoutUpload.setOnClickListener {
            triggerFilePicker()
        }

        // Sự kiện nút Gửi yêu cầu
        binding.btnSubmit.setOnClickListener {
            processFormSubmission()
        }

        // Sự kiện các nút đóng / hủy bỏ biểu mẫu để quay lại màn hình trước
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnClose.setOnClickListener { finish() }
    }

    private fun openDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
            binding.edtNewDob.setText(formattedDate)
        }, year, month, day).show()
    }

    private fun triggerFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png", "application/pdf")
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        selectFileLauncher.launch(Intent.createChooser(intent, "Chọn giấy tờ tài liệu minh chứng"))
    }

    private fun processFormSubmission() {
        val updatedName = binding.edtNewName.text.toString().trim()
        val updatedDob = binding.edtNewDob.text.toString().trim()
        val updatedAddress = binding.edtNewAddress.text.toString().trim()

        // Kiểm tra xem người dùng đã nhập đầy đủ các thông tin bắt buộc chưa
        if (updatedName.isEmpty() || updatedDob.isEmpty() || updatedAddress.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin mới cần thay đổi!", Toast.LENGTH_SHORT).show()
            return
        }

        // Kiểm tra xem đã đính kèm giấy tờ chứng minh hay chưa
        if (fileUri == null) {
            Toast.makeText(this, "Vui lòng tải lên tài liệu minh chứng để xác minh!", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Viết thêm logic xử lý Call API truyền dữ liệu lên Server của bạn ở đây

        Toast.makeText(this, "Hệ thống đã ghi nhận yêu cầu thay đổi thông tin!", Toast.LENGTH_LONG).show()
        finish()
    }
}