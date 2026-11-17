package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.CurrencyCode
import com.example.data.local.SeedDataProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "bazaarhub_prefs")

enum class AppThemeMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System");

    companion object {
        fun fromString(value: String): AppThemeMode =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: LIGHT
    }
}

data class UserPrefs(
    val activeUserId: String = SeedDataProvider.DEFAULT_BUYER_ID,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val currency: CurrencyCode = CurrencyCode.INR,
    val notificationsEnabled: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val ACTIVE_USER_ID = stringPreferencesKey("active_user_id")
        val LANGUAGE = stringPreferencesKey("language_code")
        val CURRENCY = stringPreferencesKey("currency_code")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val prefsFlow: Flow<UserPrefs> = context.dataStore.data.map { prefs ->
        UserPrefs(
            activeUserId = prefs[Keys.ACTIVE_USER_ID] ?: SeedDataProvider.DEFAULT_BUYER_ID,
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE] ?: "en"),
            currency = CurrencyCode.fromCode(prefs[Keys.CURRENCY] ?: "INR"),
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            themeMode = AppThemeMode.fromString(prefs[Keys.THEME_MODE] ?: "LIGHT")
        )
    }

    suspend fun setActiveUserId(userId: String) {
        context.dataStore.edit { it[Keys.ACTIVE_USER_ID] = userId }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setCurrency(currency: CurrencyCode) {
        context.dataStore.edit { it[Keys.CURRENCY] = currency.code }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }
}
