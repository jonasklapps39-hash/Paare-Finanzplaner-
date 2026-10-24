package de.paarfinanz.feature.household.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.paarfinanz.feature.household.R
import de.paarfinanz.feature.household.components.OnboardingScaffold
import de.paarfinanz.feature.household.components.SplitModeOption
import de.paarfinanz.feature.household.components.selectableSplitModes

@Composable
fun CreateHouseholdRoute(onBack: () -> Unit, viewModel: CreateHouseholdViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScaffold(title = stringResource(R.string.create_title), onBack = onBack) {
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = { Text(stringResource(R.string.create_name_label)) },
            placeholder = { Text(stringResource(R.string.create_name_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.create_split_title), style = MaterialTheme.typography.titleMedium)
            selectableSplitModes.forEach { mode ->
                SplitModeOption(
                    mode = mode,
                    selected = state.splitMode == mode,
                    onSelect = { viewModel.onSplitModeChange(mode) },
                )
            }
            Text(
                text = stringResource(R.string.create_split_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        state.errorRes?.let {
            Text(stringResource(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = viewModel::create,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(stringResource(R.string.create_action))
            }
        }
    }
}
