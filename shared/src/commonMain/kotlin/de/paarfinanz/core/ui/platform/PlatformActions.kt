package de.paarfinanz.core.ui.platform

import androidx.compose.runtime.Composable

/** Öffnet das Teilen-Menü des Systems mit einem Text. */
@Composable
expect fun rememberShareText(): (text: String) -> Unit

/**
 * Startet einen QR-Scanner und liefert den gelesenen Text.
 * null, wenn die Plattform keinen eingebauten Scanner bietet (iPhone: dort scannt die Kamera-App).
 */
@Composable
expect fun rememberQrScanner(onResult: (String) -> Unit): (() -> Unit)?

/**
 * Startet die native Google-Anmeldung. null, wenn sie auf dieser Plattform nicht angeboten wird.
 * [onError] erhält true bei Netzwerkfehlern.
 */
@Composable
expect fun rememberGoogleSignIn(onError: (isNetworkError: Boolean) -> Unit): (() -> Unit)?
