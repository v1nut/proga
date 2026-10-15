package com.example.data.manager

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserAuthState(
    val isLoggedIn: Boolean = false,
    val authType: AuthType = AuthType.GUEST,
    val email: String = "",
    val displayName: String = "Гость",
    val photoUrl: String? = null,
    val syncEnabled: Boolean = false,
    val lastLoginTime: String = "Не вошел"
)

enum class AuthType {
    GOOGLE, EMAIL, GUEST
}

class AuthManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow(loadAuthState())
    val authState: StateFlow<UserAuthState> = _authState.asStateFlow()

    fun signInWithGoogleCredential(idToken: String, email: String, name: String): UserAuthState {
        try {
            val auth = FirebaseAuth.getInstance()
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return loginWithGoogle(email, name)
    }

    fun loginWithGoogle(accountEmail: String = "chistochilling@gmail.com", name: String = "Пользователь Google"): UserAuthState {
        val emailToUse = if (accountEmail.isNotBlank()) accountEmail else "chistochilling@gmail.com"
        val nameToUse = if (name.isNotBlank()) name else emailToUse.substringBefore("@").replaceFirstChar { it.uppercase() }
        
        val updated = UserAuthState(
            isLoggedIn = true,
            authType = AuthType.GOOGLE,
            email = emailToUse,
            displayName = nameToUse,
            syncEnabled = true,
            lastLoginTime = "Сегодня, ${java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}"
        )
        saveAuthState(updated)
        _authState.value = updated
        return updated
    }

    fun loginWithEmail(email: String): UserAuthState {
        val nameFromEmail = email.substringBefore("@").replaceFirstChar { it.uppercase() }
        val updated = UserAuthState(
            isLoggedIn = true,
            authType = AuthType.EMAIL,
            email = email,
            displayName = nameFromEmail,
            syncEnabled = true,
            lastLoginTime = "Сегодня, ${java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}"
        )
        saveAuthState(updated)
        _authState.value = updated
        return updated
    }

    fun updateProfileName(newName: String) {
        val current = _authState.value
        val updated = current.copy(displayName = newName)
        saveAuthState(updated)
        _authState.value = updated
    }

    fun logout() {
        val guest = UserAuthState(
            isLoggedIn = false,
            authType = AuthType.GUEST,
            email = "",
            displayName = "Гость",
            syncEnabled = false
        )
        saveAuthState(guest)
        _authState.value = guest
    }

    private fun saveAuthState(state: UserAuthState) {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, state.isLoggedIn)
            .putString(KEY_AUTH_TYPE, state.authType.name)
            .putString(KEY_EMAIL, state.email)
            .putString(KEY_NAME, state.displayName)
            .putBoolean(KEY_SYNC_ENABLED, state.syncEnabled)
            .apply()
    }

    private fun loadAuthState(): UserAuthState {
        val isLoggedIn = prefs.getBoolean(KEY_LOGGED_IN, false)
        val typeName = prefs.getString(KEY_AUTH_TYPE, AuthType.GUEST.name) ?: AuthType.GUEST.name
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val name = prefs.getString(KEY_NAME, "Гость") ?: "Гость"
        val sync = prefs.getBoolean(KEY_SYNC_ENABLED, false)

        val authType = try { AuthType.valueOf(typeName) } catch (e: Exception) { AuthType.GUEST }

        return UserAuthState(
            isLoggedIn = isLoggedIn,
            authType = authType,
            email = email,
            displayName = if (isLoggedIn && name.isBlank()) "Пользователь Google" else if (!isLoggedIn) "Гость" else name,
            syncEnabled = sync,
            lastLoginTime = if (isLoggedIn) "Активен" else "Не вошел"
        )
    }

    companion object {
        private const val PREFS_NAME = "user_auth_prefs"
        private const val KEY_LOGGED_IN = "auth_logged_in"
        private const val KEY_AUTH_TYPE = "auth_type"
        private const val KEY_EMAIL = "auth_email"
        private const val KEY_NAME = "auth_name"
        private const val KEY_SYNC_ENABLED = "auth_sync_enabled"
    }
}
