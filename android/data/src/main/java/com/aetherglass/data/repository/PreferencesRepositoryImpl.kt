package com.aetherglass.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesKeys
import androidx.datastore.preferences.core.mutations
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.rxPreferencesDataStore
import com.aetherglass.domain.repository.PreferencesRepository
import com.aetherglass.util.Result
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executors

/**
 * DataStore implementation of PreferencesRepository.
 */
class PreferencesRepositoryImpl(
    private val context: Context
) : PreferencesRepository {

    private val dataStore = context.preferencesDataStore("aether_glass_prefs")

    override fun getString(key: String, default: String?): String? {
        return try {
            dataStore.data.first().getString(PreferencesKeys.stringKey(key)) ?: default
        } catch (e: Exception) {
            default
        }
    }

    override fun putString(key: String, value: String): Result<Unit> {
        return com.aetherglass.util.runCatching {
            dataStore.edit { it[PreferencesKeys.stringKey(key)] = value }.await()
        }
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return try {
            dataStore.data.first().getBoolean(PreferencesKeys.booleanKey(key)) ?: default
        } catch (e: Exception) {
            default
        }
    }

    override fun putBoolean(key: String, value: Boolean): Result<Unit> {
        return com.aetherglass.util.runCatching {
            dataStore.edit { it[PreferencesKeys.booleanKey(key)] = value }.await()
        }
    }

    override fun getLong(key: String, default: Long): Long {
        return try {
            dataStore.data.first().getLong(PreferencesKeys.longKey(key)) ?: default
        } catch (e: Exception) {
            default
        }
    }

    override fun putLong(key: String, value: Long): Result<Unit> {
        return com.aetherglass.util.runCatching {
            dataStore.edit { it[PreferencesKeys.longKey(key)] = value }.await()
        }
    }

    override fun remove(key: String): Result<Unit> {
        return com.aetherglass.util.runCatching {
            dataStore.edit { it.remove(PreferencesKeys.stringKey(key)) }.await()
        }
    }

    override fun clear(): Result<Unit> {
        return com.aetherglass.util.runCatching {
            dataStore.edit { it.clear() }.await()
        }
    }
}