package de.paarfinanz.feature.household.invite

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.paarfinanz.core.domain.household.InviteCode
import de.paarfinanz.core.ui.platform.rememberShareText
import de.paarfinanz.core.ui.theme.PaarfinanzTheme
import de.paarfinanz.feature.household.components.OnboardingScaffold
import de.paarfinanz.shared.resources.Res
import de.paarfinanz.shared.resources.invite_continue
import de.paarfinanz.shared.resources.invite_joined
import de.paarfinanz.shared.resources.invite_joined_fallback
import de.paarfinanz.shared.resources.invite_later
import de.paarfinanz.shared.resources.invite_message
import de.paarfinanz.shared.resources.invite_new_code
import de.paarfinanz.shared.resources.invite_qr_description
import de.paarfinanz.shared.resources.invite_share
import de.paarfinanz.shared.resources.invite_share_text
import de.paarfinanz.shared.resources.invite_title
import de.paarfinanz.shared.resources.invite_waiting
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InviteRoute(onBack: (() -> Unit)?, onDone: () -> Unit, viewModel: InviteViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val share = rememberShareText()

    OnboardingScaffold(title = stringResource(Res.string.invite_title), onBack = onBack) {
        val partnerName = state.joinedPartnerName
        AnimatedContent(targetState = partnerName != null, label = "invite") { joined ->
            if (joined) {
                PartnerJoined(name = partnerName.orEmpty(), onContinue = onDone)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(Res.string.invite_message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    InviteCodeCard(code = state.code, isLoading = state.isLoading)
                    state.errorRes?.let {
                        Text(stringResource(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                    val code = state.code
                    val shareMessage = code?.let { stringResource(Res.string.invite_share_text, it.formatted) }
                    Button(
                        onClick = { shareMessage?.let(share) },
                        enabled = code != null,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null)
                        Text(stringResource(Res.string.invite_share), modifier = Modifier.padding(start = 8.dp))
                    }
                    OutlinedButton(
                        onClick = viewModel::createCode,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null)
                        Text(stringResource(Res.string.invite_new_code), modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        Text(
                            text = stringResource(Res.string.invite_waiting),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = onDone, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text(stringResource(Res.string.invite_later))
                    }
                }
            }
        }
    }
}

@Composable
private fun InviteCodeCard(code: InviteCode?, isLoading: Boolean) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Immer weißer Hintergrund – auch im Dark Mode –, damit Scanner den Code sicher erkennen.
            Surface(shape = MaterialTheme.shapes.medium, color = Color.White, modifier = Modifier.size(220.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    if (code == null || isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Image(
                            painter = rememberQrCodePainter(code.deepLink),
                            contentDescription = stringResource(Res.string.invite_qr_description),
                            modifier = Modifier.padding(12.dp).size(196.dp),
                        )
                    }
                }
            }
            Text(
                text = code?.formatted ?: "····-····",
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PartnerJoined(name: String, onContinue: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = PaarfinanzTheme.extendedColors.positive,
            modifier = Modifier.size(72.dp),
        )
        Text(
            text = if (name.isNotBlank()) stringResource(Res.string.invite_joined, name) else stringResource(Res.string.invite_joined_fallback),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text(stringResource(Res.string.invite_continue))
        }
    }
}
