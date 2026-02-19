package com.byteutility.dev.quickfill

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SnippetDao {
    @Insert
    suspend fun insertSnippet(snippet: Snippet)

    @Query("SELECT * FROM snippets WHERE category = :category")
    fun getSnippetsByCategory(category: String): Flow<List<Snippet>>

    @Query("SELECT * FROM snippets")
    fun getAllSnippets(): Flow<List<Snippet>>
}