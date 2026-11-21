package com.porua.app.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "porua_auth_prefs")

data class AuthUser(
    val name: String,
    val emailOrPhone: String,
    val group: String,     // Science, Humanities, Business Studies
    val hscClass: String   // Class 11, Class 12
)

class AuthDataStore(private val context: Context) {

    companion object {
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_EMAIL_OR_PHONE = stringPreferencesKey("email_or_phone")
        val KEY_PASSWORD_HASH = stringPreferencesKey("password_hash")
        val KEY_GROUP = stringPreferencesKey("user_group")
        val KEY_HSC_CLASS = stringPreferencesKey("hsc_class")
        val KEY_EXAM_YEAR = stringPreferencesKey("exam_year")
        val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val KEY_GROK_API_KEY = stringPreferencesKey("grok_api_key")
    }

    val geminiApiKey: Flow<String?> = context.authDataStore.data.map { prefs ->
        prefs[KEY_GEMINI_API_KEY]
    }

    val grokApiKey: Flow<String?> = context.authDataStore.data.map { prefs ->
        prefs[KEY_GROK_API_KEY]
    }

    suspend fun saveAiKeys(geminiKey: String, grokKey: String) {
        context.authDataStore.edit { prefs ->
            prefs[KEY_GEMINI_API_KEY] = geminiKey.trim()
            prefs[KEY_GROK_API_KEY] = grokKey.trim()
        }
    }

    val isLoggedIn: Flow<Boolean> = context.authDataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    val examYear: Flow<String?> = context.authDataStore.data.map { prefs ->
        prefs[KEY_EXAM_YEAR]
    }

    val currentUser: Flow<AuthUser?> = context.authDataStore.data.map { prefs ->
        val loggedIn = prefs[KEY_IS_LOGGED_IN] ?: false
        if (loggedIn) {
            AuthUser(
                name = prefs[KEY_USER_NAME] ?: "",
                emailOrPhone = prefs[KEY_EMAIL_OR_PHONE] ?: "",
                group = prefs[KEY_GROUP] ?: "Science",
                hscClass = prefs[KEY_HSC_CLASS] ?: "Class 11"
            )
        } else {
            null
        }
    }

    val storedPasswordHash: Flow<String?> = context.authDataStore.data.map { prefs ->
        prefs[KEY_PASSWORD_HASH]
    }

    val storedEmailOrPhone: Flow<String?> = context.authDataStore.data.map { prefs ->
        prefs[KEY_EMAIL_OR_PHONE]
    }

    suspend fun saveAccount(
        name: String,
        emailOrPhone: String,
        passwordHash: String,
        group: String,
        hscClass: String
    ) {
        context.authDataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_USER_NAME] = name
            prefs[KEY_EMAIL_OR_PHONE] = emailOrPhone
            prefs[KEY_PASSWORD_HASH] = passwordHash
            prefs[KEY_GROUP] = group
            prefs[KEY_HSC_CLASS] = hscClass
        }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.authDataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = loggedIn
        }
    }

    suspend fun saveExamYear(year: String) {
        context.authDataStore.edit { prefs ->
            prefs[KEY_EXAM_YEAR] = year
        }
    }

    suspend fun logout() {
        context.authDataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = false
        }
    }
}
