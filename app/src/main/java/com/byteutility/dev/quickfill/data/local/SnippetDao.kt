package com.byteutility.dev.quickfill.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SnippetDao {

    @Query("SELECT * FROM snippets ORDER BY label ASC")
    fun getSnippetsStream(): Flow<List<Snippet>>

    @Query("SELECT * FROM snippets WHERE category = :category ORDER BY label ASC")
    fun getSnippetsByCategoryStream(category: String): Flow<List<Snippet>>

    @Query("SELECT * FROM snippets WHERE targetPackage = :packageName ORDER BY label ASC")
    fun getSnippetsForPackageStream(packageName: String): Flow<List<Snippet>>

    /**
     * PERFORMANCE DECISION: Using DISTINCT in the database is significantly more 
     * efficient than fetching all snippets and filtering in memory (Kotlin).
     */
    @Query("SELECT DISTINCT targetPackage FROM snippets WHERE targetPackage IS NOT NULL")
    fun getKnownPackagesStream(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: Snippet)

    @Delete
    suspend fun deleteSnippet(snippet: Snippet)

    // --- App Metadata ---

    @Query("SELECT * FROM app_metadata WHERE packageName = :packageName")
    suspend fun getAppMetadata(packageName: String): AppMetadata?

    @Query("SELECT * FROM app_metadata WHERE packageName = :packageName")
    fun getAppMetadataStream(packageName: String): Flow<AppMetadata?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppMetadata(metadata: AppMetadata)
}
