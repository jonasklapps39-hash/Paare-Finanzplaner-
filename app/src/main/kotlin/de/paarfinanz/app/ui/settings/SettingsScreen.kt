package de.paarfinanz.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.paarfinanz.app.BuildConfig
import de.paarfinanz.app.R
import de.paarfinanz.core.ui.components.PartnerAvatar
import de.paarfinanz.core.ui.theme.PaarfinanzTheme

@Composable
fun SettingsRoute(viewModel: SettingsViewModel = hiltViewModel()) {
    val email by viewModel.email.collectAsStateWithLifecycle()
    SettingsScreen(email = email, onSignOut = viewModel::signOut)
}

@Composable
fun SettingsScreen(email: String?, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_account),
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
            Text(text = stringResource(R.string.settings_sign_out), modifier = Modifier.padding(start = 8.dp))
        }
        Text(
            text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
