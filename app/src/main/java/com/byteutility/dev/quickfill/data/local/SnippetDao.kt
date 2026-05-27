package com.byteutility.dev.quickfill.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SnippetDao {

    @Query("SELECT * FROM snippets ORDER BY label ASC")
    fun getSnippetsStream(): Flow<List<Snippet>>

    @Query("SELECT * FROM snippets WHERE category = :category ORDER BY label ASC")
    fun getSnippetsByCategoryStream(category: String): Flow<List<Snippet>>

    @Query("SELECT * FROM snippets WHERE targetPackage = :packageName ORDER BY label ASC")
    fun getSnippetsForPackageStream(packageName: String): Flow<List<Snippet>>

    @Query(
        """
        SELECT * FROM snippets
        WHERE targetPackage IS NULL
        AND (category = :category OR category = 'GENERAL')
        ORDER BY label ASC
        """
    )
    fun getGlobalSnippetsForCategoryStream(category: String): Flow<List<Snippet>>

    /**
     * PERFORMANCE OPTIMIZATION: Unified query for Autofill requests.
     * Fetches snippets and joins AppMetadata in a single transaction.
     * Results are ordered so that package-specific matches come first.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM snippets
        WHERE targetPackage = :packageName
           OR (targetPackage IS NULL AND (category = :category OR category = 'GENERAL'))
        ORDER BY (CASE WHEN targetPackage = :packageName THEN 0 ELSE 1 END), label ASC
        """
    )
    fun getSnippetsForAutofillStream(packageName: String, category: String): Flow<List<SnippetWithMetadata>>

    @Transaction
    @Query(
        """
        SELECT * FROM snippets
        WHERE targetPackage = :packageName
           OR (targetPackage IS NULL AND (category = :category OR category = 'GENERAL'))
        ORDER BY (CASE WHEN targetPackage = :packageName THEN 0 ELSE 1 END), label ASC
        """
    )
    suspend fun getSnippetsForAutofill(packageName: String, category: String): List<SnippetWithMetadata>

    /**
     * PERFORMANCE DECISION: Using DISTINCT in the database is significantly more 
     * efficient than fetching all snippets and filtering in memory (Kotlin).
     */
    @Query("SELECT DISTINCT targetPackage FROM snippets WHERE targetPackage IS NOT NULL")
    fun getKnownPackagesStream(): Flow<List<String>>

    @Query("SELECT * FROM snippets WHERE id = :id")
    suspend fun getSnippetById(id: Int): Snippet?

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
