package com.finorix.signals.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onSuccess: () -> Unit,
    onLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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

    val passwordStrength = remember(password) {
        when {
            password.isEmpty() -> 0f
            password.length < 6 -> 0.3f
            password.any { it.isDigit() } && password.any { it.isUpperCase() } -> 1f
            else -> 0.6f
        }
    }
    
    val strengthColor = when (passwordStrength) {
        0.3f -> ErrorRed
        0.6f -> Color.Yellow
        1f -> NeonGreen
        else -> Color.Gray
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { data ->
            Snackbar(containerColor = Color.DarkGray, contentColor = ErrorRed, snackbarData = data)
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
            Text("Create Account", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
            Text("Join the Finorix community", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonGreen) },
                colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = NeonGreen, unfocusedBorderColor = Color.DarkGray)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NeonGreen) },
                colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = NeonGreen, unfocusedBorderColor = Color.DarkGray)
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
                colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = NeonGreen, unfocusedBorderColor = Color.DarkGray)
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = passwordStrength,
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = strengthColor,
                trackColor = Color.DarkGray
            )
            Text(
                text = if (passwordStrength < 0.6f) "Weak" else if (passwordStrength < 1f) "Medium" else "Strong",
                color = strengthColor,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonGreen) },
                colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = NeonGreen, unfocusedBorderColor = Color.DarkGray)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    colors = CheckboxDefaults.colors(checkedColor = NeonGreen)
                )
                Text("I agree to the ", color = Color.Gray, fontSize = 12.sp)
                Text("Terms & Conditions", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        if (password != confirmPassword) {
                            snackbarHostState.showSnackbar("Passwords do not match")
                        } else if (!termsAccepted) {
                            snackbarHostState.showSnackbar("Please accept the terms")
                        } else {
                            viewModel.signUp(email, password, name)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(28.dp),
                enabled = authState !is Result.Loading
            ) {
                if (authState is Result.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PureBlack)
                } else {
                    Text("Create Account", color = PureBlack, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?", color = Color.Gray)
                TextButton(onClick = onLoginClick) {
                    Text("Sign In", color = NeonGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
