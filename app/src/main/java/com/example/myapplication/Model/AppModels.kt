package com.example.myapplication.Model

import com.example.myapplication.R

// --- Authentication & Account Models ---
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val role: String?,
    val accountId: Int?
)

data class TeacherProfile(
    val teacherId: Int,
    var fullName: String,
    var phoneNumber: String?,
    var email: String?,
    var address: String?,
    var professionalRank: String?,
    var dateOfBirth: Long? = null,
    var gender: String? = null,
    var idCard: String? = null,
    var bio: String? = null,
    var avatarUrl: String? = null,
    val avatarResId: Int = R.drawable.avatar
)

// --- Student & Parent Models ---
data class Student(
    val StudentID: Int,
    val FullName: String,
    val DateOfBirth: Long,
    val Gender: String,
    val Allergies: String?,
    val BloodType: String?,
    val EnrollmentDate: Long?,
    val EnrollmentStatus: String,
    var attendanceStatus: String? = null,
    val ClassID: Int? = null,
    val parentName: String = "Phụ huynh",
    val parentPhone: String = "0900000000",
    val avatarResId: Int = R.drawable.avatar,
    val nickname: String? = null
) {
    fun getFormattedDob(): String = DateHelper.formatLongToDate(DateOfBirth)
}

// --- Leave Requests Models ---
data class LeaveRequestModel(
    val requestId: Int,
    val studentId: Int,
    val studentName: String,
    val studentAvatar: String? = null,
    val className: String? = null,
    val parentId: Int? = null,
    val parentName: String? = null,
    val parentPhone: String? = null,
    val fromDate: Long,
    val toDate: Long,
    val reason: String,
    val evidenceUrl: String? = null,
    var status: String = "Pending",
    val isMealFeeDeducted: Boolean = false,
    val parentNotes: String? = null,
    val createdAt: Long = (System.currentTimeMillis() / 1000),
    val avatarResId: Int = R.drawable.avatar,
    val approverId: Int? = null
) {
    fun getDurationText(): String {
        return if (fromDate == toDate) DateHelper.formatLongToDate(fromDate)
        else "${DateHelper.formatLongToDate(fromDate)} - ${DateHelper.formatLongToDate(toDate)}"
    }
}

// --- Notifications Table ---
data class NotificationModel(
    val notifId: Int,
    val userId: Int?,
    val title: String,
    val message: String,
    val type: String?,
    var isRead: Int = 0, // Theo Swagger: 0 hoặc 1
    val isCritical: Int = 0,
    val dataPayload: String? = null, // Dạng JSON string
    val createdAt: Long,
    val updatedAt: Long
) {
    fun getFormattedDate(): String = DateHelper.formatLongToDate(createdAt)
    fun isReadBool(): Boolean = isRead == 1
}

// --- DailyActivities Table ---
data class DailyActivity(
    val activityId: Int,
    val studentId: Int,
    val logDate: String,
    var breakfastStatus: String?,
    var lunchStatus: String?,
    var napStatus: String?,
    var snackStatus: String?,
    var hygieneStatus: String?,
    var teacherNote: String?,
    val recordedBy: Int?,
    val updatedAt: Long
)

// --- HealthRecords Table ---
data class HealthRecord(
    val recordId: Int,
    val studentId: Int,
    val termPeriod: String,
    var height: Double,
    var weight: Double,
    var bmi: Double
)

// --- StudentAssessments Table ---
data class StudentAssessment(
    val assessmentId: Int,
    val studentId: Int,
    val assessmentMonth: String,
    var physicalScore: Int,
    var cognitiveScore: Int,
    var languageScore: Int,
    var socioEmotionalScore: Int,
    var aestheticScore: Int,
    var teacherComment: String?,
    val createdAt: Long
)

// --- Invoices Table ---
data class InvoiceModel(
    val invoiceId: Int,
    val studentId: Int,
    val billingMonth: String,
    val tuitionFee: Double,
    val expectedMealFee: Double,
    val extracurricularFee: Double,
    val surcharge: Double,
    val refundAmount: Double,
    val discountAmount: Double,
    val totalAmount: Double,
    val paymentStatus: String 
)

// --- DailySchedules Table ---
data class DailySchedule(
    val dailyScheduleId: Int,
    val classId: Int,
    val scheduleDate: Long,
    val startTime: Long,
    val endTime: Long,
    val activityName: String,
    val details: String?,
    val location: String?,
    val activityType: String, 
    val status: String
) {
    fun getTimeRange(): String = "${DateHelper.formatLongToTime(startTime)} - ${DateHelper.formatLongToTime(endTime)}"
}

// --- DailyLessons Table ---
data class DailyLesson(
    val lessonLogId: Int,
    val classId: Int,
    val lessonDate: Long,
    val subjectName: String,
    val lessonTitle: String,
    val details: String,
    val iconType: String?,
    val createdAt: Long,
    val updatedAt: Long
)

// --- MedicationRequests Table ---
data class MedicationRequestModel(
    val medRequestId: Int,
    val studentId: Int,
    val studentName: String,
    val studentAvatar: String? = null,
    val parentId: Int,
    val requestDate: Long,
    val medicineDetails: String,
    val dosage: String,
    val frequency: String? = null,
    val timeToTake: String? = null,
    val parentNote: String? = null,
    val medicineImageUrl: String? = null,
    var status: String = "Pending",
    val teacherNote: String? = null,
    val avatarResId: Int = R.drawable.avatar
)

// --- News/Posts Table ---
data class ActivityPost(
    val id: String,
    val teacherName: String,
    val role: String,
    val dateText: String,
    val contentTitle: String,
    val imageResId: Int? = null,
    val imageUri: String? = null,
    val avatarResId: Int? = null,
    val postType: String = "Activity"
)

// --- Other UI Utility Models ---
data class ChatItem(
    val ConversationID: Int,
    val Name: String,
    val LastMessage: String,
    val Time: Long,
    var avatarResId: Int = R.drawable.avatar,
    val isOnline: Boolean = false,
    val hasNewMessage: Boolean = false,
    val isUnread: Boolean = false,
    val isTyping: Boolean = false
) {
    fun getFormattedTime() = DateHelper.formatLongToTime(Time)
}

data class FeatureModel(val title: String, val iconRes: Int? = null)

data class MomentModel(
    val dateText: String,
    val isToday: Boolean,
    val imageResIds: List<Int> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val caption: String? = null
)

// --- Attendance Table ---
data class AttendanceRecord(
    val attendanceId: Int,
    val studentId: Int,
    val attendanceDate: Long,
    val status: String,
    val checkInTime: Long? = null,
    val checkOutTime: Long? = null,
    val pickedUpBy: String? = null
)

data class AttendanceDay(val dayNumber: String, val status: Int, val record: AttendanceRecord? = null)

data class UpcomingEvent(val day: String, val month: String, val title: String, val time: String, val location: String)

data class ChildInfoModel(val label: String, val value: String)

data class ReportModel(
    val id: Int,
    val title: String,
    val date: String,
    val status: String,
    val description: String? = null
)

data class VaccineModel(
    val name: String,
    val status: String,
    val isDone: Boolean
)

data class TeacherMenuResponse(
    val date: String,
    val meals: Map<String, List<String>>
)

// --- Menu Models ---
data class DailyMenu(val date: String, val meals: List<Meal>)
data class Meal(val MealType: String, val dishes: List<MenuItem>)
data class MenuItem(val DishName: String, val NutritionalDetails: String?, val Calories: Int?)

// --- Request/Response API Models ---
data class QuickAttendanceRequest(
    val classId: Int,
    val date: Long,
    val attendanceData: List<AttendanceDataItem>
)

data class AttendanceDataItem(
    val studentId: Int,
    val status: String,
    val checkInTime: Long?,
    val checkOutTime: Long?,
    val pickedUpBy: String?
)

data class TeacherClassResponse(
    val classId: Int,
    val className: String,
    val studentCount: Int
)

data class LeaveRequestShort(
    val requestId: Int,
    val status: String,
    val reason: String?
)

data class TeacherStudentResponse(
    val studentId: Int,
    val fullName: String,
    val avatarUrl: String?,
    var status: String?, 
    var checkInTime: Long?,
    val checkOutTime: Long?,
    val pickedUpBy: String?,
    val healthNote: String?,
    val leaveRequest: LeaveRequestShort?
)

// --- Model cho API Get Parent Profile ---
data class ParentProfileResponse(
    val accountId: Int,
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val address: String,
    val occupation: String,
    val idCard: String,
    val relationship: String,
    val workStatus: String,
    val avatarUrl: String?,
    val bio: String?,
    val children: List<Student>
)

data class UserProfileResponse(
    val userId: Int,
    val fullName: String,
    val role: String,
    val phoneNumber: String?,
    val avatarUrl: String?,
    val email: String? = null,
    val job: String? = null,
    val address: String? = null,
    val professionalRank: String? = null,
    val workStatus: String? = null,
    val bio: String? = null
)

// --- Firebase Config Model ---
data class FirebaseConfig(
    val apiKey: String,
    val authDomain: String,
    val projectId: String,
    val storageBucket: String,
    val messagingSenderId: String,
    val appId: String
)

// --- DailyAlbums Table ---
data class DailyAlbum(
    val albumId: Int,
    val classId: Int,
    val teacherId: Int,
    val albumDate: Long,
    val caption: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val photos: List<AlbumPhoto>
)

data class AlbumPhoto(
    val photoId: Int,
    val albumId: Int,
    val photoUrl: String,
    val description: String?,
    val createdAt: Long
)

sealed class HomeItem {
    data class HeaderTitle(val title: String) : HomeItem()
    data class FeaturesBlock(val list: List<FeatureModel>) : HomeItem()
    data class MomentItem(val moment: MomentModel) : HomeItem()
}

data class CalendarDay(val day: Int, var status: String)
