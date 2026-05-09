package com.byteutility.dev.quickfill.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byteutility.dev.quickfill.data.local.SnippetDao
import com.byteutility.dev.quickfill.data.local.SnippetDatabase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SnippetRepositoryTest {

    private lateinit var database: SnippetDatabase
    private lateinit var snippetDao: SnippetDao
    private lateinit var repository: DefaultSnippetRepository
    private lateinit var mockContext: Context
    private lateinit var mockPackageManager: PackageManager

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SnippetDatabase::class.java
        ).allowMainThreadQueries().build()
        snippetDao = database.snippetDao()
        
        mockContext = mockk()
        mockPackageManager = mockk()
        
        every { mockContext.packageManager } returns mockPackageManager
        
        repository = DefaultSnippetRepository(snippetDao, mockContext)
    }

    @After
    fun cleanup() {
        database.close()
    }

    @Test
    fun saveAppMetadataFromSystem_Success() = runTest {
        val packageName = "com.example.test"
        val appLabel = "Test App"
        val iconDrawable = ColorDrawable(Color.RED)
        
        val appInfo = mockk<ApplicationInfo>()
        every { mockPackageManager.getApplicationInfo(packageName, 0) } returns appInfo
        every { mockPackageManager.getApplicationLabel(appInfo) } returns appLabel
        every { mockPackageManager.getApplicationIcon(appInfo) } returns iconDrawable
        
        repository.saveAppMetadataFromSystem(packageName)
        
        val metadata = snippetDao.getAppMetadata(packageName)
        assertNotNull(metadata)
        assertEquals(packageName, metadata?.packageName)
        assertEquals(appLabel, metadata?.label)
        assertNotNull(metadata?.iconBlob)
    }
}
