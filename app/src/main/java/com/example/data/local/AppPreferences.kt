package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "jio_panal_prefs")

class AppPreferences(private val context: Context) {

    private object Keys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val LOGGED_IN_EMAIL = stringPreferencesKey("logged_in_email")
        val ADMIN_PASSKEY = stringPreferencesKey("admin_passkey")
        val COMMISSION_RATE = doublePreferencesKey("commission_rate")
        val BIOMETRIC_UNLOCKED_SESSION = booleanPreferencesKey("biometric_unlocked_session")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_DARK_MODE] ?: true
    }

    val languageCode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE_CODE] ?: "en"
    }

    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.BIOMETRIC_ENABLED] ?: true
    }

    val loggedInEmail: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.LOGGED_IN_EMAIL]?.takeIf { it.isNotBlank() }
    }

    val adminPasskey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.ADMIN_PASSKEY] ?: "SAINI@9076"
    }

    val commissionRate: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[Keys.COMMISSION_RATE] ?: 15.0
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_DARK_MODE] = enabled }
    }

    suspend fun setLanguageCode(code: String) {
        context.dataStore.edit { it[Keys.LANGUAGE_CODE] = code }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setLoggedInEmail(email: String?) {
        context.dataStore.edit { prefs ->
            if (email.isNullOrBlank()) {
                prefs.remove(Keys.LOGGED_IN_EMAIL)
            } else {
                prefs[Keys.LOGGED_IN_EMAIL] = email.trim().lowercase()
            }
        }
    }

    suspend fun setAdminPasskey(newPasskey: String) {
        context.dataStore.edit { it[Keys.ADMIN_PASSKEY] = newPasskey }
    }

    suspend fun setCommissionRate(rate: Double) {
        context.dataStore.edit { it[Keys.COMMISSION_RATE] = rate }
    }
}
