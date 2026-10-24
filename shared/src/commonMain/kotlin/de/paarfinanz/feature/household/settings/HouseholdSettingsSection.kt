package de.paarfinanz.feature.household.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.paarfinanz.core.data.household.HouseholdState
import de.paarfinanz.core.domain.model.FairnessBasis
import de.paarfinanz.core.domain.model.SplitMode
import de.paarfinanz.feature.household.components.selectableSplitModes
import de.paarfinanz.feature.household.components.titleRes
import de.paarfinanz.shared.resources.Res
import de.paarfinanz.shared.resources.partner_fallback
import de.paarfinanz.shared.resources.settings_cancel
import de.paarfinanz.shared.resources.settings_default_split
import de.paarfinanz.shared.resources.settings_edit_income
import de.paarfinanz.shared.resources.settings_fairness
import de.paarfinanz.shared.resources.settings_fairness_disposable
import de.paarfinanz.shared.resources.settings_fairness_hint
import de.paarfinanz.shared.resources.settings_fairness_net
import de.paarfinanz.shared.resources.settings_fixed_share
import de.paarfinanz.shared.resources.settings_household
import de.paarfinanz.shared.resources.settings_household_name
import de.paarfinanz.shared.resources.settings_invite
import de.paarfinanz.shared.resources.settings_joint_account
import de.paarfinanz.shared.resources.settings_joint_account_hint
import de.paarfinanz.shared.resources.settings_rename
import de.paarfinanz.shared.resources.settings_save
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Haushalts-Einstellungen als Abschnitt für den Einstellungs-Screen der App. */
@Composable
fun HouseholdSettingsSection(
    onEditIncome: () -> Unit,
    onInvite: () -> Unit,
    viewModel: HouseholdSettingsViewModel = koinViewModel(),
) {
    val state by viewModel.householdState.collectAsStateWithLifecycle()
    val errorRes by viewModel.errorRes.collectAsStateWithLifecycle()
    val ready = state as? HouseholdState.Ready ?: return
    val household = ready.household
    var showRename by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.settings_household),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            SettingsRow(
                title = stringResource(Res.string.settings_household_name),
                value = household.name,
                icon = { Icon(Icons.Outlined.Edit, contentDescription = stringResource(Res.string.settings_rename)) },
                onClick = { showRename = true },
            )
            HorizontalDivider()

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.settings_default_split), style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    selectableSplitModes.forEach { mode ->
                        FilterChip(
                            selected = household.defaultSplitMode == mode,
                            onClick = { viewModel.setDefaultSplitMode(mode) },
                            label = { Text(stringResource(mode.titleRes)) },
                        )
                    }
                }
                val me = ready.me
                val partner = ready.partner
                if (household.defaultSplitMode == SplitMode.FIXED && me != null && partner != null) {
                    FixedShareSlider(
                        myPercent = me.fixedShare.percent.roundToInt(),
                        partnerName = partner.displayName.ifBlank { stringResource(Res.string.partner_fallback) },
                        onChangeFinished = viewModel::setMyFixedShare,
                    )
                }
            }
            HorizontalDivider()

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.settings_fairness), style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = household.fairnessBasis == FairnessBasis.NET,
                        onClick = { viewModel.setFairnessBasis(FairnessBasis.NET) },
                        label = { Text(stringResource(Res.string.settings_fairness_net)) },
                    )
                    FilterChip(
                        selected = household.fairnessBasis == FairnessBasis.DISPOSABLE,
                        onClick = { viewModel.setFairnessBasis(FairnessBasis.DISPOSABLE) },
                        label = { Text(stringResource(Res.string.settings_fairness_disposable)) },
                    )
                }
                Text(
                    text = stringResource(Res.string.settings_fairness_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider()

            ListItem(
                headlineContent = { Text(stringResource(Res.string.settings_joint_account)) },
                supportingContent = { Text(stringResource(Res.string.settings_joint_account_hint)) },
                trailingContent = {
                    Switch(checked = household.jointAccountEnabled, onCheckedChange = viewModel::setJointAccountEnabled)
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
            HorizontalDivider()

            SettingsRow(
                title = stringResource(Res.string.settings_edit_income),
                leading = { Icon(Icons.Outlined.Payments, contentDescription = null) },
                onClick = onEditIncome,
            )
            if (!household.isComplete) {
                HorizontalDivider()
                SettingsRow(
                    title = stringResource(Res.string.settings_invite),
                    leading = { Icon(Icons.Outlined.PersonAdd, contentDescription = null) },
                    onClick = onInvite,
                )
            }
        }
        errorRes?.let {
            Text(stringResource(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
    }

    if (showRename) {
        RenameDialog(
            initialName = household.name,
            onConfirm = { name ->
                viewModel.rename(name)
                showRename = false
            },
            onDismiss = { showRename = false },
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    value: String? = null,
    leading: (@Composable () -> Unit)? = null,
    icon: @Composable () -> Unit = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = value?.let { { Text(it) } },
        leadingContent = leading,
        trailingContent = icon,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun FixedShareSlider(myPercent: Int, partnerName: String, onChangeFinished: (Int) -> Unit) {
    // Lokaler Wert, damit der Regler flüssig reagiert; gespeichert wird beim Loslassen.
    var value by remember(myPercent) { mutableFloatStateOf(myPercent.toFloat()) }
    val current = value.roundToInt()
    Text(
        text = stringResource(Res.string.settings_fixed_share, current, partnerName, 100 - current),
        style = MaterialTheme.typography.bodyMedium,
    )
    Slider(
        value = value,
        onValueChange = { value = it },
        onValueChangeFinished = { onChangeFinished(value.roundToInt()) },
        valueRange = 0f..100f,
        steps = 19,
    )
}

@Composable
private fun RenameDialog(initialName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_rename)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(60) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text(stringResource(Res.string.settings_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.settings_cancel)) }
        },
    )
}
