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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imagecompressor.app.R
import com.imagecompressor.app.viewmodel.AuthUiState
import com.imagecompressor.app.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateLogin: () -> Unit,
    onRegistered: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthUiState.SignedIn) onRegistered()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.register_title), style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password_label)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(stringResource(R.string.confirm_password_label)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        localError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (state is AuthUiState.Error) {
            Text((state as AuthUiState.Error).message, color = MaterialTheme.colorScheme.error)
        }
        if (state is AuthUiState.Loading) {
            CircularProgressIndicator()
        }

        Button(
            onClick = {
                if (password != confirmPassword) {
                    localError = "Passwords do not match"
                } else {
                    localError = null
                    viewModel.register(email.trim(), password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.register_button))
        }

        TextButton(onClick = onNavigateLogin) {
            Text(stringResource(R.string.have_account_link))
        }
        TextButton(onClick = onNavigateBack) {
            Text(stringResource(R.string.skip_login))
        }
    }
}
