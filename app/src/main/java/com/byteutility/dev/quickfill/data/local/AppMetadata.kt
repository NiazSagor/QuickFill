package com.byteutility.dev.quickfill.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ARCHITECTURAL DECISION: Separate table for App Metadata.
 * We store icons here instead of the Snippet table to:
 * 1. Avoid duplication (multiple snippets for one app).
 * 2. Keep the Snippet table query extremely fast and under the 2MB CursorWindow limit.
 */
@Entity(tableName = "app_metadata")
data class AppMetadata(
    @PrimaryKey
    val packageName: String,
    val label: String,
    /**
     * PERFORMANCE DECISION: Storing icon as ByteArray (BLOB).
     * Icons should be downsampled before insertion to keep the DB size manageable.
     */
    val iconBlob: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AppMetadata
        if (packageName != other.packageName) return false
        return true
    }

    override fun hashCode(): Int {
        return packageName.hashCode()
    }
}
