package com.jayanth.jmoney.wear.presentation.auth

import android.app.Activity
import android.app.RemoteInput
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.input.RemoteInputIntentHelper

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.authState.collectAsState()
    val listState = rememberTransformingLazyColumnState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val emailLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val results = RemoteInput.getResultsFromIntent(result.data)
            val text = results?.getCharSequence("email_input")?.toString() ?: ""
            email = text
        }
    }

    val passwordLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val results = RemoteInput.getResultsFromIntent(result.data)
            val text = results?.getCharSequence("password_input")?.toString() ?: ""
            password = text
        }
    }

    val launchEmailInput = {
        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        val remoteInput = RemoteInput.Builder("email_input")
            .setLabel("Enter Email")
            .build()
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
        emailLauncher.launch(intent)
    }

    val launchPasswordInput = {
        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        val remoteInput = RemoteInput.Builder("password_input")
            .setLabel("Enter Password")
            .build()
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
        passwordLauncher.launch(intent)
    }

    when (state) {
        is AuthState.Authenticated -> {
            onLoginSuccess()
        }
        is AuthState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        else -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                TransformingLazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = "Login",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp, top = 20.dp)
                        )
                    }

                    if (state is AuthState.Error) {
                        item {
                            Text(
                                text = (state as AuthState.Error).message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = { launchEmailInput() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = if (email.isEmpty()) "Tap to enter Email" else email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (email.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = { launchPasswordInput() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = if (password.isEmpty()) "Tap to enter Password" else "••••••••",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (password.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }

                    item {
                        val isSignInEnabled = email.isNotBlank() && password.isNotBlank()
                        Button(
                            onClick = {
                                if (isSignInEnabled) {
                                    viewModel.signIn(email, password)
                                }
                            },
                            enabled = isSignInEnabled,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Text("Sign In")
                        }
                    }
                }
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun LoginScreenPreview() {
    LoginScreen(onLoginSuccess = {})
}
