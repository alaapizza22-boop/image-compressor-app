package com.imagecompressor.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.imagecompressor.app.R
import com.imagecompressor.app.data.SettingsRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val repository = remember { SettingsRepository(context) }
    val settings by repository.settingsFlow.collectAsState(initial = com.imagecompressor.app.data.AppSettings())
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge)

        Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium)

        LanguageOption(
            label = stringResource(R.string.settings_language_english),
            selected = settings.languageTag == "en",
            onClick = { scope.launch { repository.setLanguage("en") } }
        )
        LanguageOption(
            label = stringResource(R.string.settings_language_arabic),
            selected = settings.languageTag == "ar",
            onClick = { scope.launch { repository.setLanguage("ar") } }
        )

        Text(
            "Note: applying the selected app language fully requires either per-app " +
                "language APIs (AppCompatDelegate.setApplicationLocales) wired into " +
                "MainActivity, or a restart. This is wired up in README_BUILD.md's " +
                "localization notes.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label)
    }
}
