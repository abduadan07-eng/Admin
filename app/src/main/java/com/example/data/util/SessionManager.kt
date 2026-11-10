package com.example.data.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AdminRole
import com.example.data.model.Administrator

/**
 * Persists administrator session locally so that the administrator is not
 * prompted to sign in with Google repeatedly on every application launch.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "cv_admin_session_prefs"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_ADMIN_ID = "key_admin_id"
        private const val KEY_ADMIN_EMAIL = "key_admin_email"
        private const val KEY_ADMIN_NAME = "key_admin_name"
        private const val KEY_ADMIN_ROLE = "key_admin_role"
        private const val KEY_ADMIN_PERMISSIONS = "key_admin_permissions"
    }

    fun saveSession(admin: Administrator) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_ADMIN_ID, admin.id)
            .putString(KEY_ADMIN_EMAIL, admin.email)
            .putString(KEY_ADMIN_NAME, admin.displayName)
            .putString(KEY_ADMIN_ROLE, admin.role)
            .putStringSet(KEY_ADMIN_PERMISSIONS, admin.permissions.toSet())
            .apply()
    }

    fun getSavedAdmin(): Administrator? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val id = prefs.getString(KEY_ADMIN_ID, null) ?: return null
        val email = prefs.getString(KEY_ADMIN_EMAIL, null) ?: return null
        val name = prefs.getString(KEY_ADMIN_NAME, "") ?: ""
        val role = prefs.getString(KEY_ADMIN_ROLE, AdminRole.SUPER_ADMIN.roleKey) ?: AdminRole.SUPER_ADMIN.roleKey
        val permissions = prefs.getStringSet(KEY_ADMIN_PERMISSIONS, emptySet())?.toList() ?: listOf("all")

        return Administrator(
            id = id,
            email = email,
            displayName = name,
            role = role,
            permissions = permissions,
            isActive = true
        )
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
