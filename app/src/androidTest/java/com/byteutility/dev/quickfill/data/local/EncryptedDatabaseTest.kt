package com.byteutility.dev.quickfill.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byteutility.dev.quickfill.util.SecurityManager
import kotlinx.coroutines.test.runTest
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncryptedDatabaseTest {

    private lateinit var securityManager: SecurityManager

    @Before
    fun setup() {
        securityManager = SecurityManager(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testEncryptedRoomDatabase() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        
        // Load SQLCipher libraries
        SQLiteDatabase.loadLibs(context)
        
        val key = securityManager.getDatabaseEncryptionKey()
        val factory = SupportFactory(key)
        
        // Build encrypted in-memory database
        val db = Room.inMemoryDatabaseBuilder(context, SnippetDatabase::class.java)
            .openHelperFactory(factory)
            .allowMainThreadQueries()
            .build()
            
        val dao = db.snippetDao()
        
        // Test basic operations
        val snippet = Snippet(
            id = 1,
            label = "Encrypted Test",
            value = "Sensitive Data",
            category = "WORK"
        )
        
        dao.insertSnippet(snippet)
        val loaded = dao.getSnippetById(1)
        
        assertEquals(snippet, loaded)
        
        db.close()
    }
}
