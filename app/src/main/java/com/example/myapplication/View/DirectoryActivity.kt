package com.example.myapplication.View

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.UserProfileResponse
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.UserDirectoryAdapter
import okhttp3.Request
import org.json.JSONObject

class DirectoryActivity : AppCompatActivity() {

    private lateinit var rvDirectory: RecyclerView
    private val fullUserList = mutableListOf<UserProfileResponse>()
    private lateinit var adapter: UserDirectoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_directory)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        rvDirectory = findViewById(R.id.rvDirectory)
        rvDirectory.layoutManager = LinearLayoutManager(this)
        adapter = UserDirectoryAdapter(fullUserList)
        rvDirectory.adapter = adapter

        fetchUsersByRole()
    }

    private fun fetchUsersByRole() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show()
            return
        }

        val client = DataManager.okHttpClient
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/users/by-role")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("accept", "application/json")
            .get()
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    Log.d("DIRECTORY_API", "Response: $body")
                    
                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val dataJson = jsonResponse.optJSONObject("data")
                        
                        val tempItems = mutableListOf<UserProfileResponse>()
                        
                        // Parse từng nhóm role
                        val roles = listOf("Admin", "Principal", "Teacher", "Parent")
                        roles.forEach { roleKey ->
                            val userArray = dataJson?.optJSONArray(roleKey)
                            if (userArray != null) {
                                for (i in 0 until userArray.length()) {
                                    val obj = userArray.getJSONObject(i)
                                    tempItems.add(parseUser(obj, roleKey))
                                }
                            }
                        }

                        runOnUiThread {
                            fullUserList.clear()
                            fullUserList.addAll(tempItems)
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "Lỗi lấy dữ liệu từ server", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DIRECTORY_API", "Error: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this, "Không thể kết nối mạng", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun parseUser(obj: JSONObject, roleName: String): UserProfileResponse {
        return UserProfileResponse(
            userId = obj.optInt("userId"),
            username = obj.optString("username"),
            roleId = obj.optInt("roleId"),
            roleName = obj.optString("roleName", roleName),
            status = obj.optString("status"),
            avatarUrl = obj.optString("avatarUrl", ""),
            fullName = obj.optString("fullName", ""),
            email = obj.optString("email", ""),
            phoneNumber = obj.optString("phoneNumber", ""),
            job = obj.optString("job", ""),
            address = obj.optString("address", ""),
            professionalRank = obj.optString("professionalRank", ""),
            workStatus = obj.optString("workStatus", ""),
        )
    }
}