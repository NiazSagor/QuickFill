package com.byteutility.dev.quickfill.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
@Database(entities = [Snippet::class, AppMetadata::class], version = 2, exportSchema = false)
abstract class SnippetDatabase : RoomDatabase() {

    abstract fun snippetDao(): SnippetDao

    companion object {
        const val DATABASE_NAME = "quick_fill_db"
    }
}