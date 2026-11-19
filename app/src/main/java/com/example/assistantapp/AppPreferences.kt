package com.example.assistantapp

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages user-configurable app settings such as runtime Gemini API key,
 * navigation intervals, and token-saving optimizations.
 */
object AppPreferences {
    private const val PREFS_NAME = "drishti_settings"
    private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"
    private const val KEY_NAV_INTERVAL_MS = "nav_interval_ms"
    private const val KEY_MOTION_FILTER = "motion_filter_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getCustomApiKey(context: Context): String {
        return getPrefs(context).getString(KEY_CUSTOM_API_KEY, "")?.trim().orEmpty()
    }

    fun setCustomApiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun clearCustomApiKey(context: Context) {
        getPrefs(context).edit().remove(KEY_CUSTOM_API_KEY).apply()
    }

    /**
     * Resolves the active key: prefers runtime user-entered key,
     * falls back to BuildConfig.GEMINI_API_KEY if not configured.
     */
    fun getEffectiveApiKey(context: Context): String {
        val custom = getCustomApiKey(context)
        if (custom.isNotBlank()) return custom
        return BuildConfig.GEMINI_API_KEY.trim()
    }

    /** Default navigation cadence is 3000ms (3 seconds) for responsive obstacle alerts */
    fun getNavIntervalMs(context: Context): Long {
        return getPrefs(context).getLong(KEY_NAV_INTERVAL_MS, 3000L)
    }

    fun setNavIntervalMs(context: Context, intervalMs: Long) {
        getPrefs(context).edit().putLong(KEY_NAV_INTERVAL_MS, intervalMs).apply()
    }

    fun isMotionFilterEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_MOTION_FILTER, true)
    }

    fun setMotionFilterEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_MOTION_FILTER, enabled).apply()
    }
}

