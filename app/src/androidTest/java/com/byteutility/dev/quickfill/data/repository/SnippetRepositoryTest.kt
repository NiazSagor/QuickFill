package com.byteutility.dev.quickfill.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byteutility.dev.quickfill.data.local.Snippet
import com.byteutility.dev.quickfill.data.local.SnippetDao
import com.byteutility.dev.quickfill.data.local.SnippetDatabase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SnippetDatabase::class.java
        ).allowMainThreadQueries().build()
        snippetDao = spyk(database.snippetDao())
        
        mockContext = mockk()
        mockPackageManager = mockk()
        
        every { mockContext.packageManager } returns mockPackageManager
        
        // Inject the test dispatcher
        repository = DefaultSnippetRepository(snippetDao, mockContext, testDispatcher)

        // Mock Bitmap.createScaledBitmap
        mockkStatic(Bitmap::class)
        val mockBitmap = mockk<Bitmap>(relaxed = true)
        every { Bitmap.createScaledBitmap(any(), any(), any(), any()) } returns mockBitmap
        every { mockBitmap.compress(any(), any(), any()) } returns true
    }

    @After
    fun cleanup() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun insertAndGetSnippet() = runTest {
        val snippet = Snippet(label = "Label", value = "Value", category = "CAT")
        repository.insertSnippet(snippet)
        
        val allSnippets = repository.getSnippetsStream().first()
        assertEquals(1, allSnippets.size)
        assertEquals("Label", allSnippets[0].label)
    }

    @Test
    fun deleteSnippet() = runTest {
        val snippet = Snippet(id = 1, label = "Label", value = "Value", category = "CAT")
        repository.insertSnippet(snippet)
        repository.deleteSnippet(snippet)
        
        val allSnippets = repository.getSnippetsStream().first()
        assertTrue(allSnippets.isEmpty())
    }

    @Test
    fun getSnippetById() = runTest {
        val snippet = Snippet(id = 10, label = "Label", value = "Value", category = "CAT")
        repository.insertSnippet(snippet)
        
        val found = repository.getSnippetById(10)
        assertNotNull(found)
        assertEquals("Label", found?.label)
    }

    @Test
    fun getSnippetsByCategoryStream() = runTest {
        repository.insertSnippet(Snippet(label = "S1", value = "V1", category = "CAT1"))
        repository.insertSnippet(Snippet(label = "S2", value = "V2", category = "CAT2"))
        
        val cat1Snippets = repository.getSnippetsByCategoryStream("CAT1").first()
        assertEquals(1, cat1Snippets.size)
        assertEquals("S1", cat1Snippets[0].label)
    }

    @Test
    fun getSnippetsForPackageStream() = runTest {
        repository.insertSnippet(Snippet(label = "S1", value = "V1", category = "CAT", targetPackage = "pkg.a"))
        repository.insertSnippet(Snippet(label = "S2", value = "V2", category = "CAT", targetPackage = "pkg.b"))
        
        val pkgASnippets = repository.getSnippetsForPackageStream("pkg.a").first()
        assertEquals(1, pkgASnippets.size)
        assertEquals("pkg.a", pkgASnippets[0].targetPackage)
    }

    @Test
    fun getGlobalSnippetsForCategoryStream() = runTest {
        repository.insertSnippet(Snippet(label = "General", value = "V1", category = "GENERAL"))
        repository.insertSnippet(Snippet(label = "Social", value = "V2", category = "SOCIAL"))
        repository.insertSnippet(Snippet(label = "Finance", value = "V3", category = "FINANCE"))
        repository.insertSnippet(
            Snippet(
                label = "Pinned",
                value = "V4",
                category = "SOCIAL",
                targetPackage = "pkg.social"
            )
        )

        val snippets = repository.getGlobalSnippetsForCategoryStream("SOCIAL").first()

        assertEquals(2, snippets.size)
        assertEquals(listOf("General", "Social"), snippets.map { it.label })
        assertTrue(snippets.all { it.targetPackage == null })
    }

    @Test
    fun getKnownPackagesStream() = runTest {
        repository.insertSnippet(Snippet(label = "S1", value = "V1", category = "CAT", targetPackage = "pkg.a"))
        repository.insertSnippet(Snippet(label = "S2", value = "V2", category = "CAT", targetPackage = "pkg.b"))
        repository.insertSnippet(Snippet(label = "S3", value = "V3", category = "CAT", targetPackage = "pkg.a"))
        
        val packages = repository.getKnownPackagesStream().first()
        assertEquals(2, packages.size)
        assertTrue(packages.contains("pkg.a"))
        assertTrue(packages.contains("pkg.b"))
    }

    @Test
    fun saveAppMetadataFromSystem_Success() = runTest {
        val packageName = "com.example.test"
        val appLabel = "Test App"
        
        val mockDrawable = mockk<Drawable>(relaxed = true)
        every { mockDrawable.intrinsicWidth } returns 128
        every { mockDrawable.intrinsicHeight } returns 128
        every { mockDrawable.bounds } returns android.graphics.Rect(0, 0, 128, 128)
        every { mockDrawable.draw(any<Canvas>()) } returns Unit
        
        val appInfo = mockk<ApplicationInfo>()
        every { mockPackageManager.getApplicationInfo(packageName, 0) } returns appInfo
        every { mockPackageManager.getApplicationLabel(appInfo) } returns appLabel
        every { mockPackageManager.getApplicationIcon(appInfo) } returns mockDrawable
        
        repository.saveAppMetadataFromSystem(packageName)
        
        // Since we use UnconfinedTestDispatcher and inject it, this should now be synchronous or at least tracked
        val metadata = snippetDao.getAppMetadata(packageName)
        
        assertNotNull("Metadata was null in DB", metadata)
        coVerify { snippetDao.insertAppMetadata(any()) }
        assertEquals(packageName, metadata?.packageName)
        assertEquals(appLabel, metadata?.label)
    }

    @Test
    fun saveAppMetadataFromSystem_Failure_PackageNotFound() = runTest {
        val packageName = "com.nonexistent"
        
        every { mockPackageManager.getApplicationInfo(packageName, 0) } throws PackageManager.NameNotFoundException()
        
        repository.saveAppMetadataFromSystem(packageName)
        
        val metadata = snippetDao.getAppMetadata(packageName)
        assertNull(metadata)
    }
}
