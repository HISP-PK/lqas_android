package org.dhis2.dqapp.data

import android.content.Context
import org.dhis2.dqapp.AuthMode
import org.dhis2.dqapp.PeriodType

private const val PREFS_NAME = "dqapp_prefs"
private const val KEY_BASE_URL = "base_url"
private const val KEY_AUTH_MODE = "auth_mode"
private const val KEY_USERNAME = "username"
private const val KEY_PASSWORD = "password"
private const val KEY_DISTRICT_LEVEL = "district_level"
private const val KEY_HF_LEVEL = "hf_level"
private const val KEY_DATA_ELEMENT_COUNT = "data_element_count"
private const val KEY_PERIOD_TYPE = "period_type"
private const val KEY_CONNECTED = "connected"
private const val KEY_SESSION_COOKIE = "session_cookie"

data class AppPrefs(
    val baseUrl: String = "",
    val authMode: AuthMode = AuthMode.BASIC,
    val username: String = "",
    val password: String = "",
    val districtLevel: Int = 3,
    val hfLevel: Int = 6,
    val dataElementCount: Int = 12,
    val periodType: PeriodType = PeriodType.MONTHLY,
    val connected: Boolean = false,
    val sessionCookie: String = ""
)

suspend fun loadPrefs(context: Context): AppPrefs {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val auth = prefs.getString(KEY_AUTH_MODE, null)?.let { runCatching { AuthMode.valueOf(it) }.getOrNull() }
    val period = prefs.getString(KEY_PERIOD_TYPE, null)?.let { runCatching { PeriodType.valueOf(it) }.getOrNull() }

    return AppPrefs(
        baseUrl = prefs.getString(KEY_BASE_URL, "") ?: "",
        authMode = auth ?: AuthMode.BASIC,
        username = prefs.getString(KEY_USERNAME, "") ?: "",
        password = prefs.getString(KEY_PASSWORD, "") ?: "",
        districtLevel = prefs.getInt(KEY_DISTRICT_LEVEL, 3),
        hfLevel = prefs.getInt(KEY_HF_LEVEL, 6),
        dataElementCount = prefs.getInt(KEY_DATA_ELEMENT_COUNT, 12),
        periodType = period ?: PeriodType.MONTHLY,
        connected = prefs.getBoolean(KEY_CONNECTED, false),
        sessionCookie = prefs.getString(KEY_SESSION_COOKIE, "") ?: ""
    )
}

suspend fun savePrefs(context: Context, prefs: AppPrefs) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_BASE_URL, prefs.baseUrl)
        .putString(KEY_AUTH_MODE, prefs.authMode.name)
        .putString(KEY_USERNAME, prefs.username)
        .putString(KEY_PASSWORD, prefs.password)
        .putInt(KEY_DISTRICT_LEVEL, prefs.districtLevel)
        .putInt(KEY_HF_LEVEL, prefs.hfLevel)
        .putInt(KEY_DATA_ELEMENT_COUNT, prefs.dataElementCount)
        .putString(KEY_PERIOD_TYPE, prefs.periodType.name)
        .putBoolean(KEY_CONNECTED, prefs.connected)
        .putString(KEY_SESSION_COOKIE, prefs.sessionCookie)
        .apply()
}

suspend fun clearPrefs(context: Context) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .clear()
        .apply()
}
