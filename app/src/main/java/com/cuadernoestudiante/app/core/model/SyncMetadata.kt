package com.cuadernoestudiante.app.core.model

import java.io.Serializable

enum class SyncStatus {
    LOCAL_ONLY,
    SYNCED,
    DIRTY,
    DELETED,
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
) : Serializable
