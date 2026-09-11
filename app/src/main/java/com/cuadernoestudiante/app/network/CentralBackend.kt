package com.cuadernoestudiante.app.network

import android.content.Context
import com.cuadernoestudiante.app.BuildConfig
import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

class AuthTokenStore(context: Context) {
    private val prefs = context.getSharedPreferences("central_auth", Context.MODE_PRIVATE)
    var accessToken: String?
        get() = prefs.getString("access_token", null)
        set(value) { prefs.edit().apply { if (value.isNullOrBlank()) remove("access_token") else putString("access_token", value) }.apply() }
    fun clear() { prefs.edit().clear().apply() }
}

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String, @SerializedName("full_name") val fullName: String, val role: String = "student")

data class UserDto(val id: Int, val email: String, @SerializedName("full_name") val fullName: String, val role: String, @SerializedName("institution_id") val institutionId: Int?)
data class TokenResponse(@SerializedName("access_token") val accessToken: String, @SerializedName("token_type") val tokenType: String = "bearer", val user: UserDto)
data class InstitutionDto(val id: Int, val name: String)

data class ClassDto(
    val id: Int,
    val name: String,
    val subject: String,
    @SerializedName("period_name") val periodName: String,
    @SerializedName("class_code") val classCode: String,
    @SerializedName("teacher_id") val teacherId: Int,
    @SerializedName("institution_id") val institutionId: Int?,
    val active: Boolean
)

data class JoinClassRequest(@SerializedName("class_code") val classCode: String)
data class NoticeDto(val id: Int, val title: String, val body: String, @SerializedName("created_at") val createdAt: String?, @SerializedName("updated_at") val updatedAt: String?)
data class AttendanceDto(val date: String, val status: String, val note: String?)

data class AttendanceSessionDto(val date: String, val title: String, val worked: Boolean)

data class AttendancePolicyDto(
    @SerializedName("class_id") val classId: Int,
    @SerializedName("late_per_absence") val latePerAbsence: Int,
    @SerializedName("justified_effect") val justifiedEffect: String,
)

data class AttendanceSummaryDto(
    @SerializedName("student_id") val studentId: Int? = null,
    val records: Int,
    val counts: Map<String, Int>,
    @SerializedName("late_penalties") val latePenalties: Int,
    @SerializedName("effective_absences") val effectiveAbsences: Int,
    @SerializedName("attendance_percent") val attendancePercent: Double,
    val policy: AttendancePolicyDto,
)

data class GradeDto(
    val category: String,
    @SerializedName("activity_key") val activityKey: String,
    @SerializedName("activity_name") val activityName: String,
    val score: Double,
    @SerializedName("max_score") val maxScore: Double,
    val source: String
)

data class ScheduleDto(val id: Int, @SerializedName("teacher_id") val teacherId: Int, @SerializedName("class_id") val classId: Int?, val weekday: Int, @SerializedName("start_time") val startTime: String, @SerializedName("end_time") val endTime: String, val room: String)
data class StudentTeamDto(val name: String, @SerializedName("student_ids") val studentIds: List<Int>)
data class TeamMemberDto(val id: Int, @SerializedName("full_name") val fullName: String)

data class StudentTeamActivityDto(
    val id: Int,
    val name: String,
    @SerializedName("activity_type") val activityType: String,
    val category: String,
    @SerializedName("activity_key") val activityKey: String,
    val closed: Boolean,
    val team: StudentTeamDto?,
    @SerializedName("team_members") val teamMembers: List<TeamMemberDto> = emptyList(),
)

data class ParticipationReportRequest(@SerializedName("target_student_id") val targetStudentId: Int, val severity: String, val comment: String = "")

interface StudentCentralApi {
    @GET("health") suspend fun health(): Map<String, String>
    @POST("auth/login") suspend fun login(@Body request: LoginRequest): TokenResponse
    @POST("auth/register") suspend fun register(@Body request: RegisterRequest): TokenResponse
    @GET("me") suspend fun me(): UserDto
    @GET("institution") suspend fun institution(): InstitutionDto
    @GET("classes") suspend fun classes(): List<ClassDto>
    @POST("classes/join") suspend fun joinClass(@Body request: JoinClassRequest): ClassDto
    @GET("classes/{classId}/notices") suspend fun notices(@Path("classId") classId: Int): List<NoticeDto>
    @GET("classes/{classId}/attendance/me") suspend fun attendance(@Path("classId") classId: Int): List<AttendanceDto>
    @GET("classes/{classId}/attendance-sessions") suspend fun attendanceSessions(@Path("classId") classId: Int): List<AttendanceSessionDto>
    @GET("classes/{classId}/attendance-policy") suspend fun attendancePolicy(@Path("classId") classId: Int): AttendancePolicyDto
    @GET("classes/{classId}/attendance-summary/me") suspend fun attendanceSummary(@Path("classId") classId: Int): AttendanceSummaryDto
    @GET("classes/{classId}/grades/me") suspend fun grades(@Path("classId") classId: Int): List<GradeDto>
    @GET("schedule") suspend fun schedule(): List<ScheduleDto>
    @GET("classes/{classId}/team-activities") suspend fun teamActivities(@Path("classId") classId: Int): List<StudentTeamActivityDto>
    @POST("team-activities/{activityId}/participation-reports") suspend fun reportParticipation(@Path("activityId") activityId: Int, @Body request: ParticipationReportRequest): Map<String, String>
}

class CentralBackend(context: Context) {
    val tokenStore = AuthTokenStore(context.applicationContext)
    private val authInterceptor = Interceptor { chain ->
        val token = tokenStore.accessToken
        val request = if (token.isNullOrBlank()) chain.request() else chain.request().newBuilder().header("Authorization", "Bearer $token").build()
        chain.proceed(request)
    }
    private val client = OkHttpClient.Builder().addInterceptor(authInterceptor).build()
    val api: StudentCentralApi = Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL).client(client).addConverterFactory(GsonConverterFactory.create()).build().create(StudentCentralApi::class.java)
    val isConfigured: Boolean get() = !BuildConfig.API_BASE_URL.contains("example.invalid")
    fun saveSession(response: TokenResponse) { tokenStore.accessToken = response.accessToken }
    fun signOut() = tokenStore.clear()
}
