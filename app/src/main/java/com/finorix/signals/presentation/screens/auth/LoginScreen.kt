package com.finorix.signals.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finorix.signals.domain.model.Result
import com.finorix.signals.presentation.theme.NeonGreen
import com.finorix.signals.presentation.theme.PureBlack
import com.finorix.signals.presentation.theme.ErrorRed
import com.finorix.signals.util.animatedGradientBorder
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onSuccess: () -> Unit,
    onSignupClick: () -> Unit,
    onForgotClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(authState) {
        if (authState is Result.Success) {
            onSuccess()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorEvents.collect { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    fun handleGoogleSignIn() {
        scope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("REPLACE_WITH_YOUR_WEB_CLIENT_ID") // Placeholder
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val googleIdToken = result.credential.data.getString("androidx.credentials.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL") ?: ""
                
                // For demonstration, we'd use the actual library constant for the key
                // but this represents the flow.
                viewModel.signInWithGoogle(googleIdToken)
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Google Sign-In failed: ${e.message}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { data ->
            Snackbar(
                containerColor = Color.DarkGray,
                contentColor = ErrorRed,
                snackbarData = data
            )
        }},
        containerColor = PureBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text("Welcome Back", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
            Text("Sign in to continue", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = Color.DarkGray,
                    containerColor = PureBlack
                ),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NeonGreen) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, tint = NeonGreen)
                    }
                },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonGreen) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = Color.DarkGray,
                    containerColor = PureBlack
                )
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = onForgotClick) {
                    Text("Forgot Password?", color = NeonGreen)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.signIn(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .animatedGradientBorder(shape = RoundedCornerShape(28.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(28.dp),
                enabled = authState !is Result.Loading
            ) {
                if (authState is Result.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PureBlack)
                } else {
                    Text("Sign In", color = PureBlack, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("OR", color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { handleGoogleSignIn() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
            ) {
                Text("Sign in with Google", color = Color.White)
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account?", color = Color.Gray)
                TextButton(onClick = onSignupClick) {
                    Text("Sign Up", color = NeonGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
