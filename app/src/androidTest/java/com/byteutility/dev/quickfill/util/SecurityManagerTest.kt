package com.byteutility.dev.quickfill.util

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityManagerTest {

    private lateinit var securityManager: SecurityManager

    @Before
    fun setup() {
        securityManager = SecurityManager(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testKeyPersistence() {
        // Get key first time
        val key1 = securityManager.getDatabaseEncryptionKey()
        assertNotNull(key1)
        
        // Get key second time (should be same)
        val key2 = securityManager.getDatabaseEncryptionKey()
        assertArrayEquals("Key should be persistent across calls", key1, key2)
    }

    @Test
    fun testKeyGeneration() {
        val key = securityManager.getDatabaseEncryptionKey()
        assertNotNull(key)
        assert(key.size == 32) // 256 bits
    }
}
