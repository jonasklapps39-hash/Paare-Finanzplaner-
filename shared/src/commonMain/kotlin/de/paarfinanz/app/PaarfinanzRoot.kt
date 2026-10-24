package de.paarfinanz.app

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.paarfinanz.app.navigation.MainScaffold
import de.paarfinanz.app.navigation.MainStart
import de.paarfinanz.core.data.auth.AuthState
import de.paarfinanz.core.data.household.HouseholdError
import de.paarfinanz.core.data.household.HouseholdState
import de.paarfinanz.feature.auth.AuthRoute
import de.paarfinanz.feature.household.HouseholdOnboarding
import de.paarfinanz.shared.resources.Res
import de.paarfinanz.shared.resources.action_retry
import de.paarfinanz.shared.resources.household_load_failed
import de.paarfinanz.shared.resources.household_not_configured
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private enum class RootScreen { LOADING, AUTH, ONBOARDING, MAIN, FAILED }

/** Entscheidet anhand von Anmeldung und Haushalt, welcher Bereich angezeigt wird. */
@Composable
fun PaarfinanzRoot(
    viewModel: RootViewModel = koinViewModel(),
    deepLinks: DeepLinks = koinInject(),
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val householdState by viewModel.householdState.collectAsStateWithLifecycle()
    val pendingInviteCode by deepLinks.pendingInviteCode.collectAsStateWithLifecycle()

    // Wer in dieser Sitzung gerade erst einen Haushalt angelegt hat oder beigetreten ist,
    // wird direkt zum nächsten Schritt geführt (Einladen bzw. Einkommen).
    var cameFromOnboarding by rememberSaveable { mutableStateOf(false) }

    val screen = when (authState) {
        AuthState.Loading -> RootScreen.LOADING
        AuthState.SignedOut -> RootScreen.AUTH
        is AuthState.SignedIn -> when (householdState) {
            HouseholdState.Loading -> RootScreen.LOADING
            HouseholdState.None -> RootScreen.ONBOARDING
            is HouseholdState.Ready -> RootScreen.MAIN
            is HouseholdState.Failed -> RootScreen.FAILED
        }
    }

    LaunchedEffect(screen) {
        if (screen == RootScreen.ONBOARDING) cameFromOnboarding = true
        if (screen == RootScreen.MAIN) deepLinks.consumeInviteCode()
    }

    Crossfade(targetState = screen, label = "root") { target ->
        when (target) {
            RootScreen.LOADING -> LoadingScreen()
            RootScreen.AUTH -> AuthRoute()
            RootScreen.ONBOARDING -> HouseholdOnboarding(inviteCode = pendingInviteCode)
            RootScreen.MAIN -> {
                val ready = householdState as? HouseholdState.Ready
                val start = when {
                    !cameFromOnboarding || ready == null -> MainStart.OVERVIEW
                    !ready.household.isComplete -> MainStart.INVITE
                    else -> MainStart.INCOME
                }
                MainScaffold(start = start, onStartHandled = { cameFromOnboarding = false })
            }
            RootScreen.FAILED -> FailedScreen(
                message = stringResource(
                    if ((householdState as? HouseholdState.Failed)?.error == HouseholdError.NOT_CONFIGURED) {
                        Res.string.household_not_configured
                    } else {
                        Res.string.household_load_failed
                    },
                ),
                onRetry = viewModel::retry,
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun FailedScreen(message: String, onRetry: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            Button(onClick = onRetry) { Text(stringResource(Res.string.action_retry)) }
        }
    }
}
