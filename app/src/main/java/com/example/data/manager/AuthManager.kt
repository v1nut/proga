package com.example.data.manager

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserAuthState(
    val displayName: String = "Пользователь"
)

class AuthManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow(loadAuthState())
    val authState: StateFlow<UserAuthState> = _authState.asStateFlow()

    fun updateProfileName(newName: String) {
        val updated = UserAuthState(displayName = newName.trim().ifBlank { "Пользователь" })
        prefs.edit().putString(KEY_NAME, updated.displayName).apply()
        _authState.value = updated
    }

    private fun loadAuthState(): UserAuthState {
        val name = prefs.getString(KEY_NAME, "Пользователь") ?: "Пользователь"
        return UserAuthState(displayName = name.ifBlank { "Пользователь" })
    }

    companion object {
        private const val PREFS_NAME = "user_auth_prefs"
        private const val KEY_NAME = "auth_name"
    }
}
