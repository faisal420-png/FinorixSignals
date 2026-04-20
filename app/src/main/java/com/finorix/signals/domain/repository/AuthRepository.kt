package com.finorix.signals.domain.repository

import com.finorix.signals.domain.model.Result
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun signUp(email: String, password: String, displayName: String): Flow<Result<FirebaseUser>>
    fun signIn(email: String, password: String): Flow<Result<FirebaseUser>>
    fun signInWithGoogle(idToken: String): Flow<Result<FirebaseUser>>
    fun sendPasswordReset(email: String): Flow<Result<Unit>>
    fun signOut()
    fun getCurrentUser(): FirebaseUser?
    fun authStateFlow(): Flow<FirebaseUser?>
    fun uploadProfilePicture(uri: android.net.Uri): Flow<com.finorix.signals.domain.model.Result<String>>
    suspend fun updateFcmToken(token: String)
}
