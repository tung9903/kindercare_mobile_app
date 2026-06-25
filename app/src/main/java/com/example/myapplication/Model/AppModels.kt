package com.example.myapplication.Model

import com.example.myapplication.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Utility for Date Conversion
object DateHelper {
    fun formatLongToDate(timestamp: Long): String {
        val date = Date(timestamp * 1000L) 
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(date)
    }

    fun formatLongToTime(timestamp: Long): String {
        val date = Date(timestamp * 1000L)
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(date)
    }
}

// --- Base API Response Wrapper ---
data class ApiResponse<T>(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: T? = null
)

// --- Teachers Table ---
data class TeacherProfile(
    val TeacherID: Int = 0,
    var FullName: String = "",
    var PhoneNumber: String? = null,
    var Email: String? = null,
    var DateOfBirth: Long? = null,
    var Gender: String? = null,
    var IDCard: String? = null,
    var Address: String? = null,
    var ProfessionalRank: String? = null,
    var WorkStatus: String? = "Active",
    var bio: String? = null, // UI specific
    var AvatarURL: String? = null,
    var avatarResId: Int = R.drawable.avatar
)

// --- Students Table ---
data class Student(
    val StudentID: Int,
    val FullName: String,
    val DateOfBirth: Long,
    val Gender: String? = null,
    val Allergies: String? = null,
    val BloodType: String? = null,
    val AdmissionDate: Long? = null,
    val EnrollmentStatus: String? = "Active",
    val AvatarURL: String? = null,
    val ClassID: Int? = null,
    
    // UI Extended properties
    var parentName: String = "Phụ huynh",
    var parentPhone: String = "090xxx",
    var avatarResId: Int = R.drawable.avatar,
    var nickname: String? = null,
    var attendanceStatus: String = "Chưa có mặt",
    var mealStatus: String = "FULL",
    var height: Double = 0.0,
    var weight: Double = 0.0,
    var bmi: Double = 0.0,
    var teacherComment: String = "",
    var skillPhysical: Int = 0,
    var skillCognitive: Int = 0,
    var skillLanguage: Int = 0,
    var skillAesthetic: Int = 0,
    var skillEmotional: Int = 0
) {
    fun getFormattedDob() = DateHelper.formatLongToDate(DateOfBirth)
}

// --- MedicationRequests Table ---
data class MedicationRequestModel(
    val MedRequestID: Int,
    val StudentID: Int,
    val studentName: String = "",
    val ParentID: Int?,
    val RequestDate: Long,
    val MedicineDetails: String,
    val Dosage: String,
    val Frequency: String? = null,
    val TimeToTake: String? = null,
    val ParentNote: String? = null,
    val MedicineImageURL: String? = null,
    var Status: String = "Pending", 
    var TeacherNote: String? = null,
    var avatarResId: Int = R.drawable.avatar
)

// --- DailySchedules Table ---
data class DailySchedule(
    val DailyScheduleID: Int,
    val ClassID: Int,
    val ScheduleDate: Long,
    val StartTime: Long,
    val EndTime: Long,
    val ActivityName: String,
    val Details: String?,
    val Location: String?,
    val ActivityType: String, 
    val Status: String 
) {
    fun getTimeRange() = "${DateHelper.formatLongToTime(StartTime)} - ${DateHelper.formatLongToTime(EndTime)}"
}

// --- LeaveRequests Table ---
data class LeaveRequestModel(
    val RequestID: Int = 0,
    val StudentID: Int = 0,
    val studentName: String = "",
    val parentName: String = "",
    val className: String? = null, // UI/Join
    val ParentID: Int? = null,
    val FromDate: Long = 0,
    val ToDate: Long = 0,
    val Reason: String? = null,
    val EvidenceURL: String? = null,
    var Status: String = "Pending",
    val ApproverID: Int? = null,
    val IsMealFeeDeducted: Boolean = false,
    val ParentNotes: String? = null,
    val CreatedAt: Long? = null,
    var avatarResId: Int = R.drawable.avatar,
    val evidenceImages: List<Int> = emptyList(),
    // PhuHuynh specific
    val isMorningOff: Boolean = false,
    val isAfternoonOff: Boolean = false,
    val reasonCategory: String = ""
) {
    fun getDurationText(): String {
        return if (FromDate == ToDate) DateHelper.formatLongToDate(FromDate)
        else "${DateHelper.formatLongToDate(FromDate)} - ${DateHelper.formatLongToDate(ToDate)}"
    }
}

// --- Notifications Table ---
data class NotificationModel(
    val NotifID: Int,
    val UserID: Int?,
    val Title: String,
    val Message: String,
    val Type: String?,
    val ActionLink: String? = null,
    var IsRead: Boolean = false,
    val IsCritical: Boolean = false,
    val CreatedAt: Long = (System.currentTimeMillis() / 1000),
    // PhuHuynh specific
    val isUrgent: Boolean = false,
    val hasAction: Boolean = false
)

// --- DailyActivities Table ---
data class DailyActivity(
    val ActivityID: Int,
    val StudentID: Int,
    val ActivityDate: Long,
    var EatingStatus: String?,
    var SleepingStatus: String?,
    var HygieneStatus: String?,
    var TeacherNote: String?
)

// --- HealthRecords Table ---
data class HealthRecord(
    val RecordID: Int,
    val StudentID: Int,
    val TermPeriod: String,
    val Height: Double,
    val Weight: Double,
    val BMI: Double
)

// --- StudentAssessments Table ---
data class StudentAssessment(
    val AssessmentID: Int,
    val StudentID: Int,
    val AssessmentMonth: String,
    val PhysicalScore: Int,
    val CognitiveScore: Int,
    val LanguageScore: Int,
    val SocioEmotionalScore: Int,
    val AestheticScore: Int,
    var TeacherComment: String?,
    val CreatedAt: Long
)

// --- Invoices Table ---
data class InvoiceModel(
    val InvoiceID: Int,
    val StudentID: Int,
    val BillingMonth: String,
    val TuitionFee: Double,
    val ExpectedMealFee: Double,
    val ExtracurricularFee: Double,
    val Surcharge: Double,
    val RefundAmount: Double,
    val DiscountAmount: Double,
    val TotalAmount: Double,
    val PaymentStatus: String 
)

// --- Menu Models ---
data class MenuItem(
    val DishName: String,
    val NutritionalDetails: String?,
    val Calories: Int? = null
)

data class Meal(
    val MealType: String,
    val dishes: List<MenuItem>
)

data class DailyMenu(
    val date: String,
    val meals: List<Meal>
)

// --- Newsfeed Model ---
data class ActivityPost(
    val id: String,
    val teacherName: String,
    val role: String,
    val dateText: String,
    val contentTitle: String,
    val imageResId: Int? = null,
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
    val hasNewMessage: Boolean = false, // UI
    val isUnread: Boolean = false,
    val isTyping: Boolean = false
) {
    fun getFormattedTime() = DateHelper.formatLongToTime(Time)
}

data class FeatureModel(val title: String, val iconRes: Int? = null)

data class MomentModel(val dateText: String, val isToday: Boolean, val imageResIds: List<Int>)

// --- Attendance Table ---
data class AttendanceRecord(
    val attendanceId: Int,
    val studentId: Int,
    val attendanceDate: Long, // timestamp giây
    val status: String, // "Present", "Absent", "Leave"
    val checkInTime: Long? = null, // timestamp giây
    val checkOutTime: Long? = null, // timestamp giây
    val pickedUpBy: String? = null
)

data class AttendanceDay(val dayNumber: String, val status: Int, val record: AttendanceRecord? = null)

data class UpcomingEvent(val day: String, val month: String, val title: String, val time: String, val location: String)

data class ChildInfoModel(val label: String, val value: String)

data class VaccineModel(val name: String, val status: String, val isVaccinated: Boolean)

data class ReportModel(val title: String, val content: String, val time: String)

data class TuitionItem(val title: String, val isPaid: Boolean)

sealed class HomeItem {
    data class HeaderTitle(val title: String) : HomeItem()
    
    data class FeaturesBlock(val list: List<FeatureModel>) : HomeItem()
    
    data class MomentItem(val moment: MomentModel) : HomeItem()
}

// --- Models cho API Get Users By Role ---
data class UserProfileResponse(
    val userId: Int,
    val username: String,
    val roleId: Int,
    val roleName: String,
    val status: String,
    val avatarUrl: String?,
    val fullName: String?,
    val email: String?,
    val phoneNumber: String?,
    // Parent specific
    val job: String? = null,
    val address: String? = null, 
    // Teacher specific
    val dateOfBirth: Long? = null,
    val gender: String? = null,
    val idCard: String? = null,
    val professionalRank: String? = null,
    val workStatus: String? = null
)

data class UsersByRoleData(
    val Admin: List<UserProfileResponse>? = null,
    val Principal: List<UserProfileResponse>? = null,
    val Teacher: List<UserProfileResponse>? = null,
    val Parent: List<UserProfileResponse>? = null
)

// --- Models cho API Get Children (Phụ huynh) ---
data class StudentTeacher(
    val teacherId: Int,
    val fullName: String,
    val phoneNumber: String?,
    val email: String?,
    val gender: String?,
    val roleInClass: String?
)

data class ChildResponse(
    val studentId: Int,
    val fullName: String,
    val dateOfBirth: String?, // Dạng ISO hoặc String từ server
    val gender: String?,
    val allergies: String?,
    val bloodType: String? = null,
    val admissionDate: String?,
    val enrollmentStatus: String?,
    val avatarUrl: String?,
    val classId: Int?,
    val className: String?,
    val gradeName: String?,
    val academicYearName: String?,
    val buildingId: Int?,
    val buildingName: String?,
    val campusId: Int?,
    val campusName: String?,
    val campusAddress: String?,
    val relationship: String?,
    val isPrimary: Boolean,
    val teachers: List<StudentTeacher>
)

// --- Model cho API Get Parent Profile ---
data class ParentProfileResponse(
    val parentId: Int,
    val fullName: String,
    val phoneNumber: String?,
    val email: String?,
    val idCard: String?,
    val job: String?,
    val address: String?,
    val avatarUrl: String?
)



