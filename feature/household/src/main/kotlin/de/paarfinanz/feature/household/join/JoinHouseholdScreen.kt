package de.paarfinanz.feature.household.join

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.paarfinanz.feature.household.R
import de.paarfinanz.feature.household.components.OnboardingScaffold

@Composable
fun JoinHouseholdRoute(onBack: () -> Unit, viewModel: JoinHouseholdViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    OnboardingScaffold(title = stringResource(R.string.join_title), onBack = onBack) {
        Text(
            text = stringResource(R.string.join_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedButton(
            onClick = { startQrScan(context, onResult = viewModel::onScanned, onFailure = {}) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Icon(Icons.Outlined.QrCodeScanner, contentDescription = null)
            Text(stringResource(R.string.join_scan), modifier = Modifier.padding(start = 8.dp))
        }

        OutlinedTextField(
            value = state.codeInput,
            onValueChange = viewModel::onCodeChange,
            label = { Text(stringResource(R.string.join_code_label)) },
            placeholder = { Text("AB12-CD34") },
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(onGo = { viewModel.checkCode() }),
            modifier = Modifier.fillMaxWidth(),
        )

        state.errorRes?.let {
            Text(stringResource(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        val preview = state.preview
        if (preview == null) {
            Button(
                onClick = viewModel::checkCode,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                ButtonContent(isLoading = state.isLoading, text = stringResource(R.string.join_check))
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = if (preview.inviterName.isNotBlank()) {
                            stringResource(R.string.join_preview, preview.inviterName, preview.householdName)
                        } else {
                            stringResource(R.string.join_preview_anonymous, preview.householdName)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Button(
                        onClick = viewModel::accept,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        ButtonContent(isLoading = state.isLoading, text = stringResource(R.string.join_accept))
                    }
                }
            }
        }
    }
}

@Composable
private fun ButtonContent(isLoading: Boolean, text: String) {
    if (isLoading) {
        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
    } else {
        Text(text)
    }
}
