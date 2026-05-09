package com.byteutility.dev.quickfill.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SnippetDaoTest {

    private lateinit var database: SnippetDatabase
    private lateinit var snippetDao: SnippetDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SnippetDatabase::class.java
        ).allowMainThreadQueries().build()
        snippetDao = database.snippetDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetSnippet() = runTest {
        val snippet = Snippet(
            id = 1,
            label = "Test Label",
            value = "Test Value",
            category = "GENERAL"
        )
        snippetDao.insertSnippet(snippet)
        val loaded = snippetDao.getSnippetById(1)
        assertEquals(snippet, loaded)
    }

    @Test
    fun getSnippetsForPackageStream() = runTest {
        val snippet1 = Snippet(id = 1, label = "L1", value = "V1", category = "G1", targetPackage = "p1")
        val snippet2 = Snippet(id = 2, label = "L2", value = "V2", category = "G2", targetPackage = "p2")
        
        snippetDao.insertSnippet(snippet1)
        snippetDao.insertSnippet(snippet2)

        snippetDao.getSnippetsForPackageStream("p1").test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(snippet1, list[0])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getKnownPackagesStream() = runTest {
        val snippet1 = Snippet(id = 1, label = "L1", value = "V1", category = "G1", targetPackage = "p1")
        val snippet2 = Snippet(id = 2, label = "L2", value = "V2", category = "G2", targetPackage = "p1")
        val snippet3 = Snippet(id = 3, label = "L3", value = "V3", category = "G3", targetPackage = null)
        
        snippetDao.insertSnippet(snippet1)
        snippetDao.insertSnippet(snippet2)
        snippetDao.insertSnippet(snippet3)

        snippetDao.getKnownPackagesStream().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("p1", list[0])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertAndGetAppMetadata() = runTest {
        val metadata = AppMetadata(
            packageName = "com.test.app",
            label = "Test App",
            iconBlob = byteArrayOf(1, 2, 3)
        )
        snippetDao.insertAppMetadata(metadata)
        val loaded = snippetDao.getAppMetadata("com.test.app")
        assertEquals(metadata, loaded)
    }
}
