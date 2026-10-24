package de.paarfinanz.app

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.paarfinanz.app.navigation.MainScaffold
import de.paarfinanz.core.data.auth.AuthState
import de.paarfinanz.feature.auth.AuthRoute

/** Entscheidet anhand des Anmeldestatus, ob Login oder App angezeigt wird. */
@Composable
fun PaarfinanzRoot(viewModel: RootViewModel = hiltViewModel()) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    Crossfade(targetState = authState::class, label = "auth") { stateClass ->
        when (stateClass) {
            AuthState.SignedIn::class -> MainScaffold()
            AuthState.SignedOut::class -> AuthRoute()
            else -> LoadingScreen()
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
