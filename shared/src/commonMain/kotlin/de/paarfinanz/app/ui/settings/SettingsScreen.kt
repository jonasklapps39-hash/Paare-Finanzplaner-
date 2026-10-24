package de.paarfinanz.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.paarfinanz.core.data.SupabaseConfig
import de.paarfinanz.core.ui.components.PartnerAvatar
import de.paarfinanz.core.ui.theme.PaarfinanzTheme
import de.paarfinanz.feature.household.settings.HouseholdSettingsSection
import de.paarfinanz.shared.resources.Res
import de.paarfinanz.shared.resources.settings_account
import de.paarfinanz.shared.resources.settings_sign_out
import de.paarfinanz.shared.resources.settings_version
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoute(
    onInvite: () -> Unit,
    onEditIncome: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val email by viewModel.email.collectAsStateWithLifecycle()
    SettingsScreen(
        email = email,
        onSignOut = viewModel::signOut,
        householdSection = { HouseholdSettingsSection(onEditIncome = onEditIncome, onInvite = onInvite) },
    )
}

@Composable
fun SettingsScreen(email: String?, onSignOut: () -> Unit, householdSection: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        householdSection()

        Text(
            text = stringResource(Res.string.settings_account),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            ListItem(
                leadingContent = {
                    PartnerAvatar(name = email.orEmpty(), color = PaarfinanzTheme.extendedColors.partnerA)
                },
                headlineContent = { Text(email.orEmpty()) },
            )
        }
        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
            Text(text = stringResource(Res.string.settings_sign_out), modifier = Modifier.padding(start = 8.dp))
        }
        Text(
            text = stringResource(Res.string.settings_version, SupabaseConfig.versionName),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
