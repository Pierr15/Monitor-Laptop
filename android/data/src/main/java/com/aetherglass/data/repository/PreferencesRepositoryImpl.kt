package com.aetherglass.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.emptyPreferences
import com.aetherglass.domain.repository.PreferencesRepository
import com.aetherglass.util.Result
import com.aetherglass.util.runCatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.IOException

private val Context.aetherGlassPreferences: DataStore<Preferences> by preferencesDataStore(
    name = "aether_glass_prefs"
)

/**
 * Synchronous adapter for the legacy PreferencesRepository contract.
 *
 * DataStore itself is asynchronous; calls are moved to IO and isolated from
 * the caller thread. A future API revision should make this repository suspend.
 */
class PreferencesRepositoryImpl(
    context: Context
) : PreferencesRepository {

    private val dataStore = context.applicationContext.aetherGlassPreferences

    override fun getString(key: String, default: String?): String? = try {
        runBlocking(Dispatchers.IO) {
            dataStore.data.catch { cause ->
                if (cause is IOException) emit(emptyPreferences()) else throw cause
            }.first()[stringPreferencesKey(key)] ?: default
        }
    } catch (_: Exception) {
        default
    }

    override fun putString(key: String, value: String): Result<Unit> = runCatching {
        runBlocking(Dispatchers.IO) {
            dataStore.edit { preferences -> preferences[stringPreferencesKey(key)] = value }
        }
    }

    override fun getBoolean(key: String, default: Boolean): Boolean = try {
        runBlocking(Dispatchers.IO) {
            dataStore.data.catch { cause ->
                if (cause is IOException) emit(emptyPreferences()) else throw cause
            }.first()[booleanPreferencesKey(key)] ?: default
        }
    } catch (_: Exception) {
        default
    }

    override fun putBoolean(key: String, value: Boolean): Result<Unit> = runCatching {
        runBlocking(Dispatchers.IO) {
            dataStore.edit { preferences -> preferences[booleanPreferencesKey(key)] = value }
        }
    }

    override fun getLong(key: String, default: Long): Long = try {
        runBlocking(Dispatchers.IO) {
            dataStore.data.catch { cause ->
                if (cause is IOException) emit(emptyPreferences()) else throw cause
            }.first()[longPreferencesKey(key)] ?: default
        }
    } catch (_: Exception) {
        default
    }

    override fun putLong(key: String, value: Long): Result<Unit> = runCatching {
        runBlocking(Dispatchers.IO) {
            dataStore.edit { preferences -> preferences[longPreferencesKey(key)] = value }
        }
    }

    override fun remove(key: String): Result<Unit> = runCatching {
        runBlocking(Dispatchers.IO) {
            val stringKey = stringPreferencesKey(key)
            val booleanKey = booleanPreferencesKey(key)
            val longKey = longPreferencesKey(key)
            dataStore.edit { preferences ->
                preferences.remove(stringKey)
                preferences.remove(booleanKey)
                preferences.remove(longKey)
            }
        }
    }

    override fun clear(): Result<Unit> = runCatching {
        runBlocking(Dispatchers.IO) {
            dataStore.edit { preferences -> preferences.clear() }
        }
    }
}
