package com.example.data.manager

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object AppLanguageManager {

    private const val PREF_KEY_LANG = "app_language"

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("kbzhu_notification_prefs", Context.MODE_PRIVATE)
        return prefs.getString(PREF_KEY_LANG, "ru") ?: "ru"
    }

    fun setLanguage(context: Context, langCode: String) {
        val prefs = context.getSharedPreferences("kbzhu_notification_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_KEY_LANG, langCode).apply()

        try {
            val localeList = LocaleListCompat.forLanguageTags(langCode)
            AppCompatDelegate.setApplicationLocales(localeList)
        } catch (e: Exception) {
            // Ignore if framework locale change fails
        }
    }
}
