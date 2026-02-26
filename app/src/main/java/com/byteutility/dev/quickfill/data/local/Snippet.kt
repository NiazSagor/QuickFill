package com.byteutility.dev.quickfill.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snippets")
data class Snippet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val label: String,
    val value: String,
    val category: String
)