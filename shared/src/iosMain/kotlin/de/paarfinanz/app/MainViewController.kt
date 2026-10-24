package de.paarfinanz.app

import androidx.compose.ui.window.ComposeUIViewController
import de.paarfinanz.app.di.initKoin
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.mp.KoinPlatform
import platform.Foundation.NSURL
import platform.UIKit.UIViewController

private var koinStarted = false

private fun ensureKoin() {
    if (!koinStarted) {
        initKoin()
        koinStarted = true
    }
}

/** Wird von der SwiftUI-Hülle (iosApp/ContentView.swift) aufgerufen. */
@Suppress("FunctionName", "unused")
fun MainViewController(): UIViewController {
    ensureKoin()
    return ComposeUIViewController { PaarfinanzApp() }
}

/**
 * Links, mit denen die App geöffnet wurde (paarfinanz://…): Einladungen aus dem QR-Code
 * und Bestätigungslinks aus Supabase-Mails. Aufruf aus iOSApp.swift via onOpenURL.
 */
@Suppress("unused")
fun handleOpenUrl(url: String) {
    ensureKoin()
    val koin = KoinPlatform.getKoin()
    if (koin.get<DeepLinks>().handle(url)) return
    val nsUrl = NSURL.URLWithString(url) ?: return
    koin.get<SupabaseClient>().handleDeeplinks(nsUrl)
}
