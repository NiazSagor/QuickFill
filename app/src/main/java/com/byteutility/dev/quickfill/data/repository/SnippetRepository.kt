package com.byteutility.dev.quickfill.data.repository

import com.byteutility.dev.quickfill.data.local.Snippet
import com.byteutility.dev.quickfill.data.local.SnippetDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface SnippetRepository {
    fun getSnippetsStream(): Flow<List<Snippet>>
    fun getSnippetsByCategoryStream(category: String): Flow<List<Snippet>>
    fun getSnippetsForPackageStream(packageName: String): Flow<List<Snippet>>
    fun getKnownPackagesStream(): Flow<List<String>>
    suspend fun insertSnippet(snippet: Snippet)
    suspend fun deleteSnippet(snippet: Snippet)
}

@Singleton
class DefaultSnippetRepository @Inject constructor(
    private val snippetDao: SnippetDao
) : SnippetRepository {

    override fun getSnippetsStream(): Flow<List<Snippet>> = snippetDao.getSnippetsStream()

    override fun getSnippetsByCategoryStream(category: String): Flow<List<Snippet>> = 
        snippetDao.getSnippetsByCategoryStream(category)

    override fun getSnippetsForPackageStream(packageName: String): Flow<List<Snippet>> = 
        snippetDao.getSnippetsForPackageStream(packageName)

    override fun getKnownPackagesStream(): Flow<List<String>> = 
        snippetDao.getKnownPackagesStream()

    override suspend fun insertSnippet(snippet: Snippet) {
        snippetDao.insertSnippet(snippet)
    }

    override suspend fun deleteSnippet(snippet: Snippet) {
        snippetDao.deleteSnippet(snippet)
    }
}
