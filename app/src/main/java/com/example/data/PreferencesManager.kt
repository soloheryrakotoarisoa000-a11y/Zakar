package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.localization.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "zakar_settings")

class PreferencesManager(private val context: Context) {

    private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
    private val KEY_PASTORAL_TITLE = stringPreferencesKey("pastoral_title")
    private val KEY_USER_NAME = stringPreferencesKey("user_name")
    private val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    private val KEY_APP_LOCK_PIN = stringPreferencesKey("app_lock_pin")
    private val KEY_VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    private val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch_done")

    val appLanguageFlow: Flow<AppLanguage?> = context.dataStore.data.map { prefs ->
        val code = prefs[KEY_APP_LANGUAGE]
        if (code.isNullOrEmpty()) null else AppLanguage.fromCode(code)
    }

    val isFirstLaunchFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_FIRST_LAUNCH] != true
    }

    val pastoralTitleFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PASTORAL_TITLE] ?: "Pasteur"
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: ""
    }

    val appLockEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_APP_LOCK_ENABLED] ?: false
    }

    val appLockPinFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_APP_LOCK_PIN] ?: ""
    }

    val vibrationEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_VIBRATION_ENABLED] ?: true
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "SYSTEM"
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_LANGUAGE] = language.code
            prefs[KEY_FIRST_LAUNCH] = true
        }
    }

    suspend fun setPastoralTitle(title: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PASTORAL_TITLE] = title
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = name
        }
    }

    suspend fun setAppLock(enabled: Boolean, pin: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_LOCK_ENABLED] = enabled
            prefs[KEY_APP_LOCK_PIN] = pin
        }
    }

    suspend fun setVibration(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode
        }
    }

    suspend fun resetAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
