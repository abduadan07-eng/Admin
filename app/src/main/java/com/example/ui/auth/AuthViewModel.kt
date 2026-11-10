package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.AdminRole
import com.example.data.model.Administrator
import com.example.data.repository.AdminRepository
import com.example.data.util.SessionManager
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data class Unauthenticated(val errorMessage: String? = null) : AuthUiState
    data class Unauthorized(val email: String) : AuthUiState
    data class Authenticated(val admin: Administrator) : AuthUiState
}

class AuthViewModel(
    private val repository: AdminRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            viewModelScope.launch {
                verifyAndLoadAdmin(user.uid, user.email ?: "", user.displayName ?: "")
            }
        } else {
            val saved = sessionManager.getSavedAdmin()
            if (saved == null) {
                _uiState.value = AuthUiState.Unauthenticated()
            }
        }
    }

    init {
        // 1. Immediately restore saved session so user does not need to sign in repeatedly
        val savedAdmin = sessionManager.getSavedAdmin()
        if (savedAdmin != null && savedAdmin.isActive) {
            _uiState.value = AuthUiState.Authenticated(savedAdmin)
        }

        // 2. Attach auth listener to keep session in sync with Firebase
        auth.addAuthStateListener(authStateListener)

        // 3. If no local session exists, check current auth
        if (_uiState.value !is AuthUiState.Authenticated) {
            checkCurrentAuth()
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            auth.removeAuthStateListener(authStateListener)
        } catch (ignored: Exception) {
        }
    }

    fun checkCurrentAuth() {
        val user = auth.currentUser
        if (user == null) {
            val savedAdmin = sessionManager.getSavedAdmin()
            if (savedAdmin != null && savedAdmin.isActive) {
                _uiState.value = AuthUiState.Authenticated(savedAdmin)
            } else {
                _uiState.value = AuthUiState.Unauthenticated()
            }
            return
        }

        viewModelScope.launch {
            if (_uiState.value !is AuthUiState.Authenticated) {
                _uiState.value = AuthUiState.Loading
            }
            verifyAndLoadAdmin(user.uid, user.email ?: "", user.displayName ?: "")
        }
    }

    private suspend fun verifyAndLoadAdmin(uid: String, email: String, displayName: String) {
        val cleanEmail = email.trim().lowercase()
        val isInitialOwner = cleanEmail == "yahy38989@gmail.com" || cleanEmail == "aa8402502@gmail.com"

        var admin = repository.getAdministrator(uid)

        if (isInitialOwner) {
            // Guarantee Super Admin role for initial owner
            if (admin == null || !admin.isActive) {
                admin = Administrator(
                    id = uid,
                    email = cleanEmail,
                    displayName = displayName.ifBlank { "Owner / Super Admin" },
                    role = AdminRole.SUPER_ADMIN.roleKey,
                    permissions = listOf("all"),
                    isActive = true,
                    assignedBy = "system_initial"
                )
            }

            // Sync to Firestore in background
            try {
                repository.bootstrapInitialOwner(uid, cleanEmail, displayName)
            } catch (e: Exception) {
                Log.w("Auth", "Initial owner bootstrap sync error: ${e.message}", e)
            }
        }

        if (admin != null && admin.isActive) {
            sessionManager.saveSession(admin)
            _uiState.value = AuthUiState.Authenticated(admin)
        } else {
            val saved = sessionManager.getSavedAdmin()
            if (saved?.id == uid) {
                sessionManager.clearSession()
            }
            _uiState.value = AuthUiState.Unauthorized(email)
        }
    }

    fun signInWithGoogle(context: Context) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Unauthenticated("Google Sign-In configuration missing: default_web_client_id")
            return
        }

        val credentialManager = CredentialManager.create(context)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = credentialManager.getCredential(context as Activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        verifyAndLoadAdmin(user.uid, user.email ?: "", user.displayName ?: "")
                    } else {
                        _uiState.value = AuthUiState.Unauthenticated("Authentication failed: No user returned")
                    }
                } else {
                    _uiState.value = AuthUiState.Unauthenticated("Unexpected credential type received")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In cancelled: ${e.message}", e)
                if (_uiState.value is AuthUiState.Loading) {
                    val saved = sessionManager.getSavedAdmin()
                    if (saved != null && saved.isActive) {
                        _uiState.value = AuthUiState.Authenticated(saved)
                    } else {
                        _uiState.value = AuthUiState.Unauthenticated()
                    }
                }
            } catch (e: Exception) {
                Log.e("Auth", "Google Sign-In failed", e)
                val saved = sessionManager.getSavedAdmin()
                if (saved != null && saved.isActive) {
                    _uiState.value = AuthUiState.Authenticated(saved)
                } else {
                    _uiState.value = AuthUiState.Unauthenticated(e.localizedMessage ?: "Sign in failed")
                }
            }
        }
    }

    fun signOut(context: Context) {
        sessionManager.clearSession()
        auth.signOut()
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("Auth", "Failed to clear credential state", e)
            } finally {
                _uiState.value = AuthUiState.Unauthenticated()
            }
        }
    }
}
