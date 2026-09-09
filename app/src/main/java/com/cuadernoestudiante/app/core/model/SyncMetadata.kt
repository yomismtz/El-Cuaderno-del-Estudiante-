package com.cuadernoestudiante.app.core.model

enum class SyncStatus {
    LOCAL_ONLY,
    SYNCED,
    PENDING_UPLOAD,
    PENDING_UPDATE,
    PENDING_DELETE,
    CONFLICT,
}

data class SyncMetadata(
    val localId: String,
    val syncId: String? = null,
    val accountId: String,
    val studentId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
)
