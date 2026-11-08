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
    private val repository: AdminRepository
) : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkCurrentAuth()
    }

    fun checkCurrentAuth() {
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = AuthUiState.Unauthenticated()
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            verifyAndLoadAdmin(user.uid, user.email ?: "", user.displayName ?: "")
        }
    }

    private suspend fun verifyAndLoadAdmin(uid: String, email: String, displayName: String) {
        val initialOwnerEmail = "yahy38989@gmail.com"
        var admin = repository.getAdministrator(uid)

        // If this is the designated initial owner account, auto-bootstrap if not yet created
        if (admin == null && email.equals(initialOwnerEmail, ignoreCase = true)) {
            try {
                repository.bootstrapInitialOwner(uid, email, displayName)
                admin = repository.getAdministrator(uid)
            } catch (e: Exception) {
                Log.e("Auth", "Error bootstrapping initial owner", e)
            }
        }

        if (admin != null && admin.isActive) {
            _uiState.value = AuthUiState.Authenticated(admin)
        } else {
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
                _uiState.value = AuthUiState.Unauthenticated()
            } catch (e: Exception) {
                Log.e("Auth", "Google Sign-In failed", e)
                _uiState.value = AuthUiState.Unauthenticated(e.localizedMessage ?: "Sign in failed")
            }
        }
    }

    fun signOut(context: Context) {
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
