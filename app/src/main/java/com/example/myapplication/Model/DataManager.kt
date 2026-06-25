package com.example.myapplication.Model

import com.example.myapplication.R
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.Calendar
import java.util.concurrent.TimeUnit

object DataManager {
    // Singleton OkHttpClient tối ưu
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS) // Giảm xuống 5s như yêu cầu
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .retryOnConnectionFailure(true)
            .build()
    }

    var currentTeacher = TeacherProfile(
        TeacherID = 5,
        FullName = "Lê Quang Huy",
        PhoneNumber = "0912345678",
        Email = "huy.le@kindercare.edu.vn",
        Address = "123 Lê Lợi, Phường Bến Nghé, Quận 1, TP.HCM",
        ProfessionalRank = "Hạng II",
        avatarResId = R.drawable.avatar
    )

    val studentList = mutableListOf(
        Student(1, "Nguyễn Minh Khang", 1684108800, "Nam", "Dị ứng lạc", "O+", null, "Active", null, 1, "Nguyễn Anh Tuấn", "0911.222.333", R.drawable.avatar, "Khang"),
        Student(19, "Nguyễn Minh Chánh", 1464739200, "Nam", "Sài gòn bạc", "A+", 1781082000, "Active", null, 1, "Hồ Công Danh", "0988.777.666", R.drawable.avatar, "Chánh"),
        Student(20, "Trần Gia Bảo", 1673740800, "Nam", "Không", "B+", 1757030400, "Active", null, 2, "Trần Văn Nam", "0905.111.222", R.drawable.avatar, "Bảo"),
        Student(21, "Lê Nhã Uyên", 1679443200, "Nữ", "Dị ứng sữa bò", "O-", 1757030400, "Active", null, 2, "Lê Thị Lan", "0933.444.555", R.drawable.avatar, "Uyên"),
        Student(22, "Phạm Tuấn Kiệt", 1683676800, "Nam", "Không", "AB+", 1757030400, "Active", null, 2, "Phạm Văn Hùng", "0912.333.444", R.drawable.avatar, "Kiệt"),
        Student(23, "Vũ Ngọc Diệp", 1688774400, "Nữ", "Dị ứng lạc", "O+", 1757030400, "Active", null, 2, "Vũ Văn Cường", "0977.888.999", R.drawable.avatar, "Diệp")
    )

    val leaveRequests = mutableListOf(
        LeaveRequestModel(
            RequestID = 1, StudentID = 22, studentName = "Phạm Tuấn Kiệt", parentName = "Phạm Văn Hùng",
            className = "Mầm 2", FromDate = 1778803200, ToDate = 1778889600, Reason = "Bệnh/Ốm",
            Status = "Pending", ParentNotes = "Bé Kiệt bị sốt xuất huyết.", avatarResId = R.drawable.avatar
        ),
        LeaveRequestModel(
            RequestID = 3, StudentID = 21, studentName = "Lê Nhã Uyên", parentName = "Lê Thị Lan",
            className = "Mầm 2", FromDate = 1781740800, ToDate = 1781740800, Reason = "Du lịch",
            Status = "Approved", ApproverID = 5, ParentNotes = "Bé đi du lịch cùng gia đình.", avatarResId = R.drawable.avatar
        )
    )

    val medicationRequests = mutableListOf(
        MedicationRequestModel(
            MedRequestID = 1, StudentID = 21, studentName = "Lê Nhã Uyên", ParentID = 4,
            RequestDate = 1778803200, MedicineDetails = "Siro ho Prospan", Dosage = "Uống 5ml sau khi ăn trưa",
            Status = "Completed", TeacherNote = "Cô đã cho bé uống lúc 11:30", avatarResId = R.drawable.avatar
        ),
        MedicationRequestModel(
            MedRequestID = 2, StudentID = 1, studentName = "Nguyễn Minh Khang", ParentID = 4,
            RequestDate = 1778803200, MedicineDetails = "Men tiêu hóa BioGaia", Dosage = "Nhỏ 5 giọt vào sữa xế",
            Status = "Pending", avatarResId = R.drawable.avatar
        )
    )

    val activityPosts = mutableListOf(
        ActivityPost(
            id = "1", teacherName = "Cô Lan Anh", role = "Giáo viên chủ nhiệm",
            dateText = "10:30 AM • Hôm nay",
            contentTitle = "Giờ hoạt động ngoài trời sáng nay của các con. Các bé rất hào hứng với trò chơi tìm kiếm lá cây 🍃",
            imageResId = R.drawable.avatar, avatarResId = R.drawable.avatar, postType = "Activity"
        ),
        ActivityPost(
            id = "2", teacherName = "Cô Lan Anh", role = "Giáo viên chủ nhiệm",
            dateText = "08:30 AM • Hôm nay",
            contentTitle = "Bé Phạm Trà My đã hoàn thành bữa sáng rất ngoan. Hôm nay bé ăn cháo sườn sụn.",
            imageResId = R.drawable.avatar, avatarResId = R.drawable.avatar, postType = "Meal"
        )
    )

    val allNotifications = mutableListOf<NotificationModel>()
    val parentChats = mutableListOf<ChatItem>()
    val attendanceHistory = mutableMapOf<String, List<Student>>()

    init {
        val now = System.currentTimeMillis() / 1000
        allNotifications.addAll(listOf(
            NotificationModel(1, 5, "Lịch họp chuyên môn tổ Mầm", "Chiều nay 15:00 tại Phòng Hội đồng...", "MANAGEMENT", null, false, false, now - 600),
            NotificationModel(2, 5, "Thông báo nghỉ lễ 2/9", "Nhà trường thông báo lịch nghỉ lễ...", "MANAGEMENT", null, true, false, now - 86400),
            NotificationModel(3, null, "Bé Nguyễn Minh Khang dặn thuốc", "Phụ huynh bé Khang vừa gửi yêu cầu...", "PARENTS", null, false, true, now - 1800),
            NotificationModel(4, null, "Cập nhật hệ thống v2.1", "Hệ thống vừa cập nhật tính năng...", "SYSTEM", null, true, false, now - 172800)
        ))

        parentChats.addAll(listOf(
            ChatItem(101, "Cô Lan Anh • Lớp Mầm 1", "Chào phụ huynh, hôm nay bé rất ngoan", now - 3600, R.drawable.avatar, true, false),
            ChatItem(102, "Thầy Minh • Thể dục", "Phụ huynh nhớ cho bé mang giày bata nhé", now - 86400, R.drawable.avatar, false, true)
        ))
    }

    val dailyMenu = DailyMenu(
        date = "18/05/2026",
        meals = listOf(
            Meal("Bữa sáng", listOf(MenuItem("Cháo sườn sụn", "Gạo tẻ, sườn heo", 250))),
            Meal("Bữa trưa", listOf(MenuItem("Cá hồi áp chảo", "Cá hồi, bơ", 350))),
            Meal("Bữa xế", listOf(MenuItem("Sữa chua trái cây", "Sữa chua, dâu tây", 150)))
        )
    )

    fun getMockSchedules(): List<DailySchedule> {
        val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis / 1000
        return listOf(
            DailySchedule(1, 1, today, today + 7*3600, today + 8*3600, "Đón bé", null, "Cổng", "pickup", "Xong"),
            DailySchedule(3, 1, today, today + 9*3600, today + 10*3600, "Tạo hình", null, "Phòng học", "study", "Đang diễn ra")
        )
    }

    fun getCurrentMeal(): Meal = if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 10) dailyMenu.meals[0] else dailyMenu.meals[1]

    fun approveRequest(requestId: Int) {
        leaveRequests.find { it.RequestID == requestId }?.let {
            it.Status = "Approved"
            studentList.find { s -> s.StudentID == it.StudentID }?.attendanceStatus = "Nghỉ"
        }
    }

    fun rejectRequest(requestId: Int) { leaveRequests.find { it.RequestID == requestId }?.Status = "Rejected" }

    fun isLeaveApproved(studentId: Int): Boolean = leaveRequests.any { it.StudentID == studentId && it.Status == "Approved" }

    fun initializeDailyAttendance() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        studentList.forEach { student ->
            if (isLeaveApproved(student.StudentID)) {
                student.attendanceStatus = "Nghỉ"
            } else if (hour < 8) {
                student.attendanceStatus = "Chưa có mặt"
            } else {
                if (student.attendanceStatus != "Hiện diện" && student.attendanceStatus != "Nghỉ") {
                    student.attendanceStatus = "Chưa có mặt"
                }
            }
        }
    }

    fun saveAttendanceForDate(date: String, list: List<Student>) {
        val copy = list.map { it.copy() }
        attendanceHistory[date] = copy
    }

    fun getAttendanceForDate(date: String): List<Student>? = attendanceHistory[date]

    fun getAttendanceStats(list: List<Student>): AttendanceStats {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isAfter9 = hour >= 9
        val present = list.count { it.attendanceStatus == "Hiện diện" }
        val markedAbsent = list.count { it.attendanceStatus == "Nghỉ" }
        val unmarked = list.count { it.attendanceStatus == "Chưa có mặt" }
        val totalAbsent = if (isAfter9) markedAbsent + unmarked else markedAbsent
        val excused = list.count { it.attendanceStatus == "Nghỉ" && isLeaveApproved(it.StudentID) }
        val unexcused = totalAbsent - excused
        return AttendanceStats(present, totalAbsent, excused, unexcused, list.size)
    }

    data class AttendanceStats(val present: Int, val absent: Int, val excused: Int, val unexcused: Int, val total: Int)
}
