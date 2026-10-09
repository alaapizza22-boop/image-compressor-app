package com.imagecompressor.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imagecompressor.app.R
import com.imagecompressor.app.viewmodel.AuthUiState
import com.imagecompressor.app.viewmodel.AuthViewModel

@Composable
fun AccountScreen(
    paddingValues: PaddingValues,
    onNavigateLogin: () -> Unit,
    onNavigateRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.account_title), style = MaterialTheme.typography.titleLarge)

        when (val s = state) {
            is AuthUiState.SignedIn -> {
                Text(stringResource(R.string.signed_in_as, s.user.email ?: ""))
                Button(onClick = { viewModel.logout() }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.logout_button))
                }
            }
            else -> {
                Text(stringResource(R.string.guest_mode))
                Button(onClick = onNavigateLogin, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.login_title))
                }
                OutlinedButton(onClick = onNavigateRegister, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.register_title))
                }
            }
        }
    }
}
