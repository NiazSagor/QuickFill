package com.byteutility.dev.quickfill.data.repository

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import com.byteutility.dev.quickfill.data.local.AppMetadata
import com.byteutility.dev.quickfill.data.local.Snippet
import com.byteutility.dev.quickfill.data.local.SnippetDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

interface SnippetRepository {
    fun getSnippetsStream(): Flow<List<Snippet>>
    fun getSnippetsByCategoryStream(category: String): Flow<List<Snippet>>
    fun getSnippetsForPackageStream(packageName: String): Flow<List<Snippet>>
    fun getKnownPackagesStream(): Flow<List<String>>
    fun getAppMetadataStream(packageName: String): Flow<AppMetadata?>
    suspend fun insertSnippet(snippet: Snippet)
    suspend fun deleteSnippet(snippet: Snippet)
    suspend fun saveAppMetadataFromSystem(packageName: String)
}

@Singleton
class DefaultSnippetRepository @Inject constructor(
    private val snippetDao: SnippetDao,
    @ApplicationContext private val context: Context
) : SnippetRepository {

    override fun getSnippetsStream(): Flow<List<Snippet>> = snippetDao.getSnippetsStream()

    override fun getSnippetsByCategoryStream(category: String): Flow<List<Snippet>> = 
        snippetDao.getSnippetsByCategoryStream(category)

    override fun getSnippetsForPackageStream(packageName: String): Flow<List<Snippet>> = 
        snippetDao.getSnippetsForPackageStream(packageName)

    override fun getKnownPackagesStream(): Flow<List<String>> = 
        snippetDao.getKnownPackagesStream()

    override fun getAppMetadataStream(packageName: String): Flow<AppMetadata?> =
        snippetDao.getAppMetadataStream(packageName)

    override suspend fun insertSnippet(snippet: Snippet) {
        snippetDao.insertSnippet(snippet)
    }

    override suspend fun deleteSnippet(snippet: Snippet) {
        snippetDao.deleteSnippet(snippet)
    }

    /**
     * ARCHITECTURAL DECISION: Fetch and store system metadata locally.
     * This bypasses Android 11+ Package Visibility restrictions for future lookups
     * and ensures the app works without QUERY_ALL_PACKAGES.
     */
    override suspend fun saveAppMetadataFromSystem(packageName: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                val pm = context.packageManager
                val info = pm.getApplicationInfo(packageName, 0)
                val label = pm.getApplicationLabel(info).toString()
                val icon = pm.getApplicationIcon(info).toBitmap()
                
                // PERFORMANCE DECISION: Downsample the icon before saving.
                // Icons can be 192x192 (or larger with adaptive icons). 
                // We shrink it to 128x128 for a good balance of quality vs DB size.
                val scaledIcon = Bitmap.createScaledBitmap(icon, 128, 128, true)
                val stream = ByteArrayOutputStream()
                scaledIcon.compress(Bitmap.CompressFormat.PNG, 100, stream)
                
                val metadata = AppMetadata(
                    packageName = packageName,
                    label = label,
                    iconBlob = stream.toByteArray()
                )
                snippetDao.insertAppMetadata(metadata)
            }
        }
    }
}
