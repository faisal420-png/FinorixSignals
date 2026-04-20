package com.finorix.signals.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finorix.signals.domain.model.Result
import com.finorix.signals.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(repository.getCurrentUser())
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<Result<FirebaseUser>?>(null)
    val authState: StateFlow<Result<FirebaseUser>?> = _authState.asStateFlow()

    private val _uploadState = MutableStateFlow<Result<String>?>(null)
    val uploadState: StateFlow<Result<String>?> = _uploadState.asStateFlow()

    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents: SharedFlow<String> = _errorEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.authStateFlow().collect {
                _currentUser.value = it
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            repository.signUp(email, password, displayName).collect { result ->
                _authState.value = result
                if (result is Result.Error) _errorEvents.emit(result.message)
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            repository.signIn(email, password).collect { result ->
                _authState.value = result
                if (result is Result.Error) _errorEvents.emit(result.message)
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            repository.signInWithGoogle(idToken).collect { result ->
                _authState.value = result
                if (result is Result.Error) _errorEvents.emit(result.message)
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            repository.sendPasswordReset(email).collect { result ->
                if (result is Result.Error) _errorEvents.emit(result.message)
            }
        }
    }

    fun uploadProfilePicture(uri: android.net.Uri) {
        viewModelScope.launch {
            repository.uploadProfilePicture(uri).collect { result ->
                _uploadState.value = result
                if (result is Result.Error) _errorEvents.emit(result.message)
            }
        }
    }

    fun signOut() {
        repository.signOut()
    }
    
    fun clearAuthState() {
        _authState.value = null
    }
}
