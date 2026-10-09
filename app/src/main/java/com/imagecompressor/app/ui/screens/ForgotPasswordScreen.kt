package com.imagecompressor.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imagecompressor.app.R
import com.imagecompressor.app.viewmodel.AuthUiState
import com.imagecompressor.app.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.forgot_password_title), style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        when (state) {
            is AuthUiState.ResetEmailSent -> Text(stringResource(R.string.reset_email_sent))
            is AuthUiState.Error -> Text((state as AuthUiState.Error).message, color = MaterialTheme.colorScheme.error)
            is AuthUiState.Loading -> CircularProgressIndicator()
            else -> {}
        }

        Button(
            onClick = { viewModel.sendPasswordReset(email.trim()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.reset_password_button))
        }

        TextButton(onClick = onNavigateBack) {
            Text(stringResource(R.string.skip_login))
        }
    }
}
