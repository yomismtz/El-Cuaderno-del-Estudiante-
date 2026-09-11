package com.cuadernoestudiante.app.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cuadernoestudiante.app.core.model.StudentDataSnapshot
import com.cuadernoestudiante.app.network.CentralBackend
import com.cuadernoestudiante.app.network.JoinClassRequest
import com.cuadernoestudiante.app.network.ParticipationReportRequest
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.concurrent.TimeUnit

private data class PendingParticipationReport(
    val activityId: Int,
    val targetStudentId: Int,
    val severity: String,
    val comment: String,
)

enum class OfflineWriteResult { SENT, QUEUED }

class StudentOfflineStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("student_offline_sync", Context.MODE_PRIVATE)
    private val cacheFile = File(appContext.filesDir, "student_snapshot.cache")
    private val gson = Gson()

    @Synchronized
    fun loadSnapshot(): StudentDataSnapshot? = runCatching {
        if (!cacheFile.exists()) return null
        ObjectInputStream(cacheFile.inputStream().buffered()).use { it.readObject() as StudentDataSnapshot }
    }.getOrNull()

    @Synchronized
    fun saveSnapshot(snapshot: StudentDataSnapshot) {
        runCatching {
            val temp = File(cacheFile.parentFile, "${cacheFile.name}.tmp")
            ObjectOutputStream(temp.outputStream().buffered()).use { it.writeObject(snapshot) }
            if (cacheFile.exists()) cacheFile.delete()
            temp.renameTo(cacheFile)
        }
    }

    suspend fun joinClassOrQueue(backend: CentralBackend, code: String): OfflineWriteResult {
        return try {
            backend.api.joinClass(JoinClassRequest(code))
            OfflineWriteResult.SENT
        } catch (_: IOException) {
            queueClassCode(code)
            StudentSyncScheduler.enqueueNow(appContext)
            OfflineWriteResult.QUEUED
        }
    }

    suspend fun reportParticipationOrQueue(
        backend: CentralBackend,
        activityId: Int,
        targetStudentId: Int,
        severity: String,
        comment: String,
    ): OfflineWriteResult {
        return try {
            backend.api.reportParticipation(
                activityId,
                ParticipationReportRequest(targetStudentId, severity, comment),
            )
            OfflineWriteResult.SENT
        } catch (_: IOException) {
            queueReport(PendingParticipationReport(activityId, targetStudentId, severity, comment))
            StudentSyncScheduler.enqueueNow(appContext)
            OfflineWriteResult.QUEUED
        }
    }

    fun pendingCount(): Int = pendingClassCodes().size + pendingReports().size

    suspend fun flush(backend: CentralBackend): Boolean {
        if (backend.tokenStore.accessToken.isNullOrBlank()) return true
        var retryLater = false

        val remainingCodes = pendingClassCodes().toMutableSet()
        pendingClassCodes().forEach { code ->
            try {
                backend.api.joinClass(JoinClassRequest(code))
                remainingCodes.remove(code)
            } catch (_: IOException) {
                retryLater = true
            } catch (error: HttpException) {
                if (error.code() in 400..499 && error.code() != 401 && error.code() != 429) {
                    remainingCodes.remove(code)
                } else {
                    retryLater = true
                }
            } catch (_: Throwable) {
                retryLater = true
            }
        }
        saveClassCodes(remainingCodes)

        val remainingReports = pendingReports().toMutableSet()
        pendingReports().forEach { encoded ->
            val item = runCatching { gson.fromJson(encoded, PendingParticipationReport::class.java) }.getOrNull()
            if (item == null) {
                remainingReports.remove(encoded)
                return@forEach
            }
            try {
                backend.api.reportParticipation(
                    item.activityId,
                    ParticipationReportRequest(item.targetStudentId, item.severity, item.comment),
                )
                remainingReports.remove(encoded)
            } catch (_: IOException) {
                retryLater = true
            } catch (error: HttpException) {
                if (error.code() in 400..499 && error.code() != 401 && error.code() != 429) {
                    remainingReports.remove(encoded)
                } else {
                    retryLater = true
                }
            } catch (_: Throwable) {
                retryLater = true
            }
        }
        saveReports(remainingReports)
        return !retryLater
    }

    private fun queueClassCode(code: String) {
        val next = pendingClassCodes().toMutableSet().apply { add(code.trim().uppercase()) }
        saveClassCodes(next)
    }

    private fun queueReport(item: PendingParticipationReport) {
        val next = pendingReports().toMutableSet().apply { add(gson.toJson(item)) }
        saveReports(next)
    }

    private fun pendingClassCodes(): Set<String> =
        prefs.getStringSet("class_codes", emptySet())?.toSet().orEmpty()

    private fun pendingReports(): Set<String> =
        prefs.getStringSet("participation_reports", emptySet())?.toSet().orEmpty()

    private fun saveClassCodes(values: Set<String>) {
        prefs.edit().putStringSet("class_codes", values.toSet()).apply()
    }

    private fun saveReports(values: Set<String>) {
        prefs.edit().putStringSet("participation_reports", values.toSet()).apply()
    }
}

class StudentPendingSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val backend = CentralBackend(applicationContext)
        val store = StudentOfflineStore(applicationContext)
        return if (store.flush(backend)) Result.success() else Result.retry()
    }
}

object StudentSyncScheduler {
    private const val IMMEDIATE = "student-offline-pending-sync"
    private const val PERIODIC = "student-offline-periodic-sync"

    private fun constraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun ensurePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<StudentPendingSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun enqueueNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<StudentPendingSyncWorker>()
            .setConstraints(constraints())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}
