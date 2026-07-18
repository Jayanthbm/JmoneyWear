package com.jayanth.jmoney.wear.presentation.auth

import android.app.Activity
import android.app.RemoteInput
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.Text
import com.jayanth.jmoney.wear.presentation.common.buttons.ExpressiveButton
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
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                ScreenScaffold(
                    scrollState = listState,
                    scrollIndicator = {
                        ScrollIndicator(state = listState)
                    }
                ) { contentPadding ->
                    TransformingLazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = contentPadding,
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
                            shape = ButtonDefaults.shape,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (email.isEmpty()) "Enter Email" else email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (email.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { launchPasswordInput() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = ButtonDefaults.shape,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (password.isEmpty()) "Enter Password" else "••••••••",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (password.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        val isSignInEnabled = email.isNotBlank() && password.isNotBlank()
                        ExpressiveButton(
                            onClick = {
                                if (isSignInEnabled) {
                                    viewModel.signIn(email, password)
                                }
                            },
                            text = "Sign In",
                            isPrimary = true,
                            enabled = isSignInEnabled,
                            width = 120.dp,
                            height = 40.dp,
                            shape = ButtonDefaults.shape
                        )
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
