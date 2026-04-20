package com.finorix.signals.data.repository

import com.finorix.signals.domain.model.Result
import com.finorix.signals.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: com.google.firebase.storage.FirebaseStorage,
    private val analyticsHelper: com.finorix.signals.util.AnalyticsHelper,
    private val crashlytics: com.google.firebase.crashlytics.FirebaseCrashlytics
) : AuthRepository {

    override fun signUp(email: String, password: String, displayName: String): Flow<Result<FirebaseUser>> = flow {
        emit(Result.Loading)
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                // Update profile display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                user.updateProfile(profileUpdates).await()
                
                // Create user document in Firestore
                createUserInFirestore(user)
                
                analyticsHelper.logSignup("email")
                crashlytics.setUserId(user.uid)
                
                emit(Result.Success(user))
            } else {
                emit(Result.Error("Sign up failed: User is null"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "Unknown sign up error"))
        }
    }

    override fun signIn(email: String, password: String): Flow<Result<FirebaseUser>> = flow {
        emit(Result.Loading)
        try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                analyticsHelper.logLogin("email")
                crashlytics.setUserId(user.uid)
                emit(Result.Success(user))
            } else {
                emit(Result.Error("Sign in failed: User is null"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "Unknown sign in error"))
        }
    }

    override fun signInWithGoogle(idToken: String): Flow<Result<FirebaseUser>> = flow {
        emit(Result.Loading)
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                // Update Firestore for Google users too
                createUserInFirestore(user)
                analyticsHelper.logLogin("google")
                crashlytics.setUserId(user.uid)
                emit(Result.Success(user))
            } else {
                emit(Result.Error("Google Sign-In failed: User is null"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "Unknown Google error"))
        }
    }

    override fun sendPasswordReset(email: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            auth.sendPasswordResetEmail(email).await()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "Password reset failed"))
        }
    }

    override fun signOut() = auth.signOut()

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener {
            trySend(it.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun uploadProfilePicture(uri: android.net.Uri): Flow<Result<String>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            trySend(Result.Error("Not logged in"))
            close()
            return@callbackFlow
        }
        
        trySend(Result.Loading)
        
        val storageRef = storage.reference.child("avatars/$uid.jpg")
        val uploadTask = storageRef.putFile(uri)
        
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                db.collection("users").document(uid)
                    .update("photoUrl", downloadUrl.toString())
                    .addOnSuccessListener {
                        trySend(Result.Success(downloadUrl.toString()))
                        close()
                    }
                    .addOnFailureListener { e ->
                        trySend(Result.Error(e.localizedMessage ?: "Failed to update Firestore"))
                        close()
                    }
            }.addOnFailureListener { e ->
                trySend(Result.Error(e.localizedMessage ?: "Failed to get download URL"))
                close()
            }
        }.addOnFailureListener { e ->
            trySend(Result.Error(e.localizedMessage ?: "Upload failed"))
            close()
        }
        
        awaitClose { uploadTask.cancel() }
    }

    override suspend fun updateFcmToken(token: String) {
        val uid = auth.currentUser?.uid ?: return
        try {
            db.collection("users").document(uid).update("fcmToken", token).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun createUserInFirestore(user: FirebaseUser) {
        val userMap = hashMapOf(
            "uid" to user.uid,
            "email" to user.email,
            "displayName" to user.displayName,
            "photoUrl" to user.photoUrl?.toString(),
            "createdAt" to System.currentTimeMillis(),
            "plan" to "free",
            "winRate" to 0,
            "totalSignals" to 0
        )
        // Use SetOptions.merge() to avoid overwriting existing data if they re-login
        db.collection("users").document(user.uid)
            .set(userMap, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }
}
