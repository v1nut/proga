package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object EncryptedPrefs {
    private const val PREFS_NAME = "encrypted_kbzhu_prefs"
    private const val KEY_GEMINI_API_KEY = "custom_gemini_api_key"

    private fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        return try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

            EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKeyAlias,
                context.applicationContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.values().first(),
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun getGeminiApiKey(context: Context): String {
        return try {
            getEncryptedSharedPreferences(context).getString(KEY_GEMINI_API_KEY, "")?.trim() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun saveGeminiApiKey(context: Context, apiKey: String) {
        try {
            getEncryptedSharedPreferences(context).edit().putString(KEY_GEMINI_API_KEY, apiKey.trim()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
