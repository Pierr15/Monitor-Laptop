package com.aetherglass.data.repository

import android.content.Context
import com.aetherglass.core.security.SecurityConfig
import com.aetherglass.core.util.Result
import com.aetherglass.domain.repository.SecureStorageRepository

/**
 * EncryptedSharedPreferences implementation of SecureStorageRepository.
 */
class SecureStorageRepositoryImpl(
    private val context: Context
) : SecureStorageRepository {

    private val prefs = SecurityConfig.encryptedPrefs

    override fun putString(key: String, value: String): Result<Unit> {
        return com.aetherglass.util.runCatching {
            prefs.edit().putString(key, value).apply()
        }
    }

    override fun getString(key: String): Result<String?> {
        return com.aetherglass.util.runCatching {
            prefs.getString(key, null)
        }
    }

    override fun putByteArray(key: String, value: ByteArray): Result<Unit> {
        return com.aetherglass.util.runCatching {
            val encoded = android.util.Base64.encodeToString(value, android.util.Base64.DEFAULT)
            prefs.edit().putString(key, encoded).apply()
        }
    }

    override fun getByteArray(key: String): Result<ByteArray?> {
        return com.aetherglass.util.runCatching {
            val encoded = prefs.getString(key, null)
            encoded?.let { android.util.Base64.decode(it, android.util.Base64.DEFAULT) }
        }
    }

    override fun remove(key: String): Result<Unit> {
        return com.aetherglass.util.runCatching {
            prefs.edit().remove(key).apply()
        }
    }

    override fun clear(): Result<Unit> {
        return com.aetherglass.util.runCatching {
            prefs.edit().clear().apply()
        }
    }
}