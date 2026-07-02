package com.example.myapplication.View.PhuHuynh

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.*
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.TuitionAdapter
import com.example.myapplication.Utils.NavigationUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.DecimalFormat

class FeeActivity : AppCompatActivity() {

    private lateinit var rvTuitionList: RecyclerView
    private lateinit var adapter: TuitionAdapter
    private val invoiceList = mutableListOf<InvoiceModel>()
    
    private lateinit var tvAmountDue: TextView
    private lateinit var tvAmountPaid: TextView
    private lateinit var tvStudentName: TextView
    private lateinit var tvStudentId: TextView

    private val moneyFormatter = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fee)

        val rootView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()

        // Ưu tiên lấy bé đang chọn từ DataManager
        val selectedChild = DataManager.selectedChild
        if (selectedChild != null) {
            val studentId = selectedChild.optInt("studentId")
            tvStudentName.text = selectedChild.optString("fullName")
            tvStudentId.text = "Mã học sinh: KC-$studentId"
            
            val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
            val token = pref.getString("token", null)
            if (!token.isNullOrEmpty()) {
                fetchInvoices(studentId, token)
            }
        } else {
            fetchChildAndLoadInvoices()
        }

        findViewById<View>(R.id.ivNotification).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        findViewById<View>(R.id.ivAvatar).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        setupBottomNavigation()
    }

    private fun initViews() {
        rvTuitionList = findViewById(R.id.rvTuitionList)
        tvAmountDue = findViewById(R.id.tvAmountDue)
        tvAmountPaid = findViewById(R.id.tvAmountPaid)
        tvStudentName = findViewById(R.id.tvStudentName)
        tvStudentId = findViewById(R.id.tvStudentId)
    }

    private fun setupRecyclerView() {
        rvTuitionList.layoutManager = LinearLayoutManager(this)
        adapter = TuitionAdapter(invoiceList) { selectedInvoice ->
            handlePayment(selectedInvoice)
        }
        rvTuitionList.adapter = adapter
    }

    private fun fetchChildAndLoadInvoices() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children")
            .addHeader("Authorization", "Bearer $token")
            .get().build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val dataArray = JSONObject(body).optJSONArray("data")
                        if (dataArray != null && dataArray.length() > 0) {
                            val child = dataArray.getJSONObject(0)
                            val realStudentId = child.getInt("studentId")
                            val fullName = child.getString("fullName")
                            
                            runOnUiThread {
                                tvStudentName.text = fullName
                                tvStudentId.text = "Mã học sinh: KC-$realStudentId"
                            }
                            fetchInvoices(realStudentId, token)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FEE_API", "Error: ${e.message}")
            }
        }.start()
    }

    private fun fetchInvoices(studentId: Int, token: String) {
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children/$studentId/invoices")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    Log.d("FEE_API", "Dữ liệu hóa đơn: $body")
                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val dataArray = jsonResponse.optJSONArray("data")
                        val tempInvoices = mutableListOf<InvoiceModel>()
                        var totalDue = 0.0
                        var totalPaid = 0.0

                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                val invoice = parseInvoice(it.getJSONObject(i))
                                tempInvoices.add(invoice)
                                if (invoice.paymentStatus == "Paid") totalPaid += invoice.totalAmount
                                else totalDue += invoice.totalAmount
                            }
                        }

                        runOnUiThread {
                            invoiceList.clear()
                            invoiceList.addAll(tempInvoices)
                            adapter.notifyDataSetChanged()
                            tvAmountDue.text = "${moneyFormatter.format(totalDue)}đ"
                            tvAmountPaid.text = "${moneyFormatter.format(totalPaid)}đ"
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    private fun handlePayment(invoice: InvoiceModel) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        Toast.makeText(this, "Đang thực hiện thanh toán...", Toast.LENGTH_SHORT).show()

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/invoices/${invoice.invoiceId}/pay")
            .addHeader("Authorization", "Bearer $token")
            .post("{}".toRequestBody("application/json".toMediaType()))
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    Log.d("FEE_PAY", "Kết quả thanh toán: $body")
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_LONG).show()
                            fetchInvoices(invoice.studentId, token) // Tải lại dữ liệu cho bé này
                        } else {
                            val msg = JSONObject(body ?: "{}").optString("message", "Thanh toán thất bại")
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    private fun parseInvoice(obj: JSONObject) = InvoiceModel(
        invoiceId = obj.optInt("invoiceId"),
        studentId = obj.optInt("studentId"),
        billingMonth = obj.optString("billingMonth"),
        tuitionFee = obj.optDouble("tuitionFee"),
        expectedMealFee = obj.optDouble("expectedMealFee"),
        extracurricularFee = obj.optDouble("extracurricularFee"),
        surcharge = obj.optDouble("surcharge"),
        refundAmount = obj.optDouble("refundAmount"),
        discountAmount = obj.optDouble("discountAmount"),
        totalAmount = obj.optDouble("totalAmount"),
        paymentStatus = obj.optString("paymentStatus")
    )

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationPH(this, "FEE")
    }
}
