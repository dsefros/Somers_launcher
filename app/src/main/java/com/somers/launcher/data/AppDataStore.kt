package com.somers.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.somers.launcher.domain.ActivationStateStore
import com.somers.launcher.domain.AppLanguage
import com.somers.launcher.domain.NetworkMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class AppDataStore(context: Context) : ActivationStateStore {
    private val ds = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("launcher_state.preferences_pb") }
    )

    override val activatedFlow: Flow<Boolean> = ds.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.ACTIVATED] ?: false }

    override val languageFlow: Flow<AppLanguage> = ds.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { AppLanguage.fromCode(it[Keys.LANGUAGE]) }

    override val networkModeFlow: Flow<NetworkMode?> = ds.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { value -> value[Keys.NETWORK_MODE]?.let { runCatching { NetworkMode.valueOf(it) }.getOrNull() } }

    override suspend fun setActivated(value: Boolean) {
        ds.edit { it[Keys.ACTIVATED] = value }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        ds.edit { it[Keys.LANGUAGE] = language.code }
    }

    override suspend fun setNetworkMode(mode: NetworkMode?) {
        ds.edit { prefs ->
            if (mode == null) prefs.remove(Keys.NETWORK_MODE) else prefs[Keys.NETWORK_MODE] = mode.name
        }
    }

    private object Keys {
        val ACTIVATED = booleanPreferencesKey("activated")
        val LANGUAGE = stringPreferencesKey("language")
        val NETWORK_MODE = stringPreferencesKey("network_mode")
    }
}
