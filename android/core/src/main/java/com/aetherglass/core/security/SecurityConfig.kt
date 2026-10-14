package com.aetherglass.core.security

import android.content.Context
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.aetherglass.core.util.runCatching
import timber.log.Timber

/**
 * Security configuration and initialization.
 * Sets up encrypted storage, keystore, and security defaults.
 */
object SecurityConfig {

    private const val MASTER_KEY_ALIAS = "aether_glass_master_key"
    private const val ENCRYPTED_PREFS_NAME = "aether_glass_secure_prefs"

    private var _masterKeyAlias: String? = null
    private var _encryptedPrefs: android.content.SharedPreferences? = null

    val masterKeyAlias: String
        get() = _masterKeyAlias ?: MASTER_KEY_ALIAS

    val encryptedPrefs: android.content.SharedPreferences
        get() = _encryptedPrefs
            ?: throw IllegalStateException("SecurityConfig not initialized. Call SecurityConfig.initialize(context) first.")

    fun initialize(context: Context) {
        runCatching {
            val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC.apply {
                keyAlias = MASTER_KEY_ALIAS
            })
            _masterKeyAlias = masterKey

            _encryptedPrefs = EncryptedSharedPreferences.create(
                ENCRYPTED_PREFS_NAME,
                masterKey,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            Timber.tag("SecurityConfig").i("Encrypted preferences initialized")
        }.onFailure { e ->
            Timber.tag("SecurityConfig").e(e, "Failed to initialize security config")
            throw SecurityInitializationException("Failed to initialize security", e)
        }
    }

    fun isInitialized(): Boolean = _encryptedPrefs != null

    fun clearAll() {
        _encryptedPrefs?.edit()?.clear()?.apply()
        _masterKeyAlias = null
        _encryptedPrefs = null
    }
}

class SecurityInitializationException(message: String, cause: Throwable) : Exception(message, cause)

/**
 * Keystore manager for storing sensitive credentials.
 * Uses Android Keystore for hardware-backed key storage where available.
 */
class KeystoreManager(private val context: Context) {

    private val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    companion object {
        private const val KEY_ALIAS_PREFIX = "aether_glass_"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

    /**
     * Generate or retrieve a secret key for encryption.
     */
    fun getOrCreateKey(alias: String): javax.crypto.SecretKey {
        val fullAlias = KEY_ALIAS_PREFIX + alias
        return try {
            (keyStore.getEntry(fullAlias, null) as java.security.KeyStore.SecretKeyEntry).secretKey
        } catch (_: Exception) {
            generateKey(fullAlias)
            (keyStore.getEntry(fullAlias, null) as java.security.KeyStore.SecretKeyEntry).secretKey
        }
    }

    private fun generateKey(alias: String) {
        val keyGenerator = javax.crypto.KeyGenerator.getInstance(
            javax.crypto.KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val keyGenParameterSpec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            androidx.security.crypto.MasterKeys.AES256_GCM_SPEC.apply {
                keyAlias = alias
                setUserAuthenticationRequired(true)
                setInvalidatedByBiometricEnrollment(true)
            }
        } else {
            // Fallback for older API levels
            android.security.keystore.KeyGenParameterSpec.Builder(alias,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            ).apply {
                setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                setKeySize(256)
                setUserAuthenticationRequired(true)
                setInvalidatedByBiometricEnrollment(true)
            }.build()
        }

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    /**
     * Encrypt data using a keystore-backed key.
     */
    fun encrypt(alias: String, data: ByteArray): EncryptedData {
        val key = getOrCreateKey(alias)
        val cipher = javax.crypto.Cipher.getInstance(TRANSFORMATION)
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(data)
        val iv = cipher.iv
        return EncryptedData(encrypted, iv)
    }

    /**
     * Decrypt data using a keystore-backed key.
     */
    fun decrypt(alias: String, encryptedData: EncryptedData): ByteArray {
        val key = getOrCreateKey(alias)
        val cipher = javax.crypto.Cipher.getInstance(TRANSFORMATION)
        val spec = javax.crypto.spec.GCMParameterSpec(128, encryptedData.iv)
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(encryptedData.ciphertext)
    }

    /**
     * Delete a key from keystore.
     */
    fun deleteKey(alias: String) {
        val fullAlias = KEY_ALIAS_PREFIX + alias
        try {
            keyStore.deleteEntry(fullAlias)
        } catch (e: Exception) {
            Timber.tag("KeystoreManager").w(e, "Failed to delete key: $fullAlias")
        }
    }

    /**
     * Check if a key exists.
     */
    fun hasKey(alias: String): Boolean {
        val fullAlias = KEY_ALIAS_PREFIX + alias
        return keyStore.containsAlias(fullAlias)
    }
}

data class EncryptedData(
    val ciphertext: ByteArray,
    val iv: ByteArray
)

/**
 * Biometric authentication helper.
 * Provides a consistent API for biometric/PIN authentication.
 */
class BiometricAuth(
    private val context: Context,
    private val executor: java.util.concurrent.Executor = java.util.concurrent.Executors.newSingleThreadExecutor()
) {

    private val biometricPrompt by lazy {
        androidx.biometric.BiometricPrompt(
            context as androidx.fragment.app.FragmentActivity,
            executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // Handled by callback
                }
                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    // Handled by callback
                }
                override fun onAuthenticationFailed() {
                    // Handled by callback
                }
            }
        )
    }

    /**
     * Authenticate with biometric or device credential.
     * Returns true on success, false on failure/cancel.
     */
    suspend fun authenticate(
        title: String = "Aether Glass",
        subtitle: String = "Authenticate to continue",
        description: String = "Use your biometric or device PIN to confirm",
        negativeButtonText: String = "Cancel"
    ): Boolean = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        val callback = object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                continuation.resume(true)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode == androidx.biometric.BiometricPrompt.ERROR_CANCELED ||
                    errorCode == androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    continuation.resume(false)
                } else {
                    continuation.resumeWithException(BiometricAuthException(errorCode, errString.toString()))
                }
            }

            override fun onAuthenticationFailed() {
                // User attempted but failed (e.g., wrong fingerprint)
                // Don't resume - let them try again
            }
        }

        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        continuation.invokeOnCancellation {
            biometricPrompt.cancelAuthentication()
        }

        biometricPrompt.authenticate(promptInfo, callback)
    }

    /**
     * Check if biometric authentication is available.
     */
    fun canAuthenticate(): BiometricAvailability {
        val biometricManager = androidx.biometric.BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(
            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
            androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NoHardware
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HardwareUnavailable
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NotEnrolled
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SecurityUpdateRequired
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailability.Unsupported
            else -> BiometricAvailability.Unknown
        }
    }
}

enum class BiometricAvailability {
    Available, NoHardware, HardwareUnavailable, NotEnrolled, SecurityUpdateRequired, Unsupported, Unknown
}

class BiometricAuthException(val errorCode: Int, message: String) : Exception(message)

/**
 * Secure storage wrapper using encrypted preferences.
 */
class SecureStorage(private val prefs: android.content.SharedPreferences) {

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, default: String? = null): String? {
        return prefs.getString(key, default)
    }

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return prefs.getBoolean(key, default)
    }

    fun putLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    fun getLong(key: String, default: Long = 0L): Long {
        return prefs.getLong(key, default)
    }

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun contains(key: String): Boolean {
        return prefs.contains(key)
    }
}