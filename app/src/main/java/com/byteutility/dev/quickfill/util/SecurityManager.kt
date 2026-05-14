package com.byteutility.dev.quickfill.util

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        ENCRYPTED_PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getDatabaseEncryptionKey(): ByteArray {
        val encodedKey = sharedPreferences.getString(DB_ENCRYPTION_KEY, null)
        return if (encodedKey == null) {
            val newKey = generateRandomKey()
            val newEncodedKey = Base64.encodeToString(newKey, Base64.DEFAULT)
            sharedPreferences.edit().putString(DB_ENCRYPTION_KEY, newEncodedKey).apply()
            newKey
        } else {
            Base64.decode(encodedKey, Base64.DEFAULT)
        }
    }

    private fun generateRandomKey(): ByteArray {
        val key = ByteArray(32) // 256 bits
        SecureRandom().nextBytes(key)
        return key
    }

    companion object {
        private const val ENCRYPTED_PREFS_NAME = "quick_fill_encrypted_prefs"
        private const val DB_ENCRYPTION_KEY = "db_encryption_key"
    }
}
