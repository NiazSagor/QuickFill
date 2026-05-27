package com.byteutility.dev.quickfill.data.local

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Optimized POJO for Autofill requests.
 * Includes both the snippet and its associated app metadata (label, icon) in one object.
 */
data class SnippetWithMetadata(
    @Embedded val snippet: Snippet,
    @Relation(
        parentColumn = "targetPackage",
        entityColumn = "packageName"
    )
    val metadata: AppMetadata?
)
