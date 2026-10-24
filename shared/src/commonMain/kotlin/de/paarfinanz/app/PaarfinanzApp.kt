package de.paarfinanz.app

import androidx.compose.runtime.Composable
import de.paarfinanz.core.ui.theme.PaarfinanzTheme

/** Einstieg der Oberfläche – identisch auf Android und iPhone. */
@Composable
fun PaarfinanzApp() {
    PaarfinanzTheme {
        PaarfinanzRoot()
    }
}
