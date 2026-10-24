package de.paarfinanz.feature.household.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.paarfinanz.core.ui.theme.PaarfinanzTheme
import de.paarfinanz.shared.resources.Res
import de.paarfinanz.shared.resources.welcome_create
import de.paarfinanz.shared.resources.welcome_join
import de.paarfinanz.shared.resources.welcome_message
import de.paarfinanz.shared.resources.welcome_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeScreen(onCreate: () -> Unit, onJoin: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CoupleIllustration()
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.welcome_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.welcome_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onCreate, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text(stringResource(Res.string.welcome_create))
                }
                OutlinedButton(onClick = onJoin, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text(stringResource(Res.string.welcome_join))
                }
            }
        }
    }
}

/** Zwei Kreise in den Partnerfarben mit einem Haus in der Schnittmenge. */
@Composable
private fun CoupleIllustration() {
    val colors = PaarfinanzTheme.extendedColors
    Box(modifier = Modifier.size(width = 136.dp, height = 88.dp)) {
        Surface(shape = CircleShape, color = colors.partnerA.copy(alpha = 0.85f), modifier = Modifier.size(88.dp)) {}
        Surface(
            shape = CircleShape,
            color = colors.partnerB.copy(alpha = 0.85f),
            modifier = Modifier.offset(x = 48.dp).size(88.dp),
        ) {}
        Icon(
            imageVector = Icons.Outlined.Home,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surface,
            modifier = Modifier.align(Alignment.Center).size(32.dp),
        )
    }
}
