package de.paarfinanz.feature.household.income

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.paarfinanz.core.ui.format.MoneyFormatter
import de.paarfinanz.feature.household.R
import de.paarfinanz.feature.household.components.OnboardingScaffold
import de.paarfinanz.feature.household.components.ShareBar
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun IncomeRoute(onBack: (() -> Unit)?, onDone: () -> Unit, viewModel: IncomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnDone by rememberUpdatedState(onDone)
    LaunchedEffect(viewModel) { viewModel.saved.collect { currentOnDone() } }

    val form = state.form
    val partnerName = state.partner?.displayName?.ifBlank { null } ?: stringResource(R.string.partner_fallback)

    OnboardingScaffold(title = stringResource(R.string.income_title), onBack = onBack) {
        Text(
            text = stringResource(R.string.income_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        AmountField(
            value = form.netInput,
            onValueChange = viewModel::onNetChange,
            label = stringResource(R.string.income_net_label),
            imeAction = ImeAction.Next,
        )
        AmountField(
            value = form.fixedCostsInput,
            onValueChange = viewModel::onFixedCostsChange,
            label = stringResource(R.string.income_fixed_label),
            supportingText = stringResource(R.string.income_fixed_hint),
            imeAction = ImeAction.Done,
        )

        MonthSelector(month = form.validFrom, onPrevious = viewModel::previousMonth, onNext = viewModel::nextMonth)

        if (state.partner != null) {
            val partnerIncome = state.partnerIncome
            Text(
                text = if (partnerIncome != null) {
                    stringResource(R.string.income_partner, partnerName, MoneyFormatter.format(partnerIncome.netIncome, state.currency))
                } else {
                    stringResource(R.string.income_partner_missing, partnerName)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.income_preview_title), style = MaterialTheme.typography.titleMedium)
                val shares = state.previewShares
                val me = state.me
                if (shares != null && me != null && state.partner != null) {
                    // Reihenfolge wie im Haushalt (Besitzer zuerst), damit die Farben überall gleich sind.
                    val meFirst = shares.first == me.userId
                    val meLabel = stringResource(R.string.me)
                    ShareBar(
                        firstName = if (meFirst) meLabel else partnerName,
                        firstShare = shares.firstShare,
                        secondName = if (meFirst) partnerName else meLabel,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.income_preview_waiting),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        form.errorRes?.let {
            Text(stringResource(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = viewModel::save,
            enabled = !form.isSaving,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (form.isSaving) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(stringResource(R.string.income_save))
            }
        }
        TextButton(onClick = onDone, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(stringResource(R.string.income_skip))
        }
    }
}

@Composable
private fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    imeAction: ImeAction,
    supportingText: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        suffix = { Text("€") },
        supportingText = supportingText?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = imeAction),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun MonthSelector(month: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.GERMANY)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.income_valid_from),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.income_previous_month))
        }
        Text(text = month.format(formatter), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.income_next_month))
        }
    }
}
