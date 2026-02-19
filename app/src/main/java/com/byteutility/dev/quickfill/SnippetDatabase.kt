package com.byteutility.dev.quickfill

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Snippet::class], version = 1, exportSchema = false)
abstract class SnippetDatabase : RoomDatabase() {
    abstract fun snippetDao(): SnippetDao

    companion object {
        const val DATABASE_NAME = "quickfill_db"
    }
}