package de.paarfinanz.core.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

@Composable
actual fun rememberShareText(): (text: String) -> Unit = remember {
    { text ->
        val controller = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
        topViewController()?.presentViewController(controller, animated = true, completion = null)
    }
}

// Die Kamera-App des iPhones erkennt QR-Codes selbst und öffnet den Einladungslink in Paarfinanz.
@Composable
actual fun rememberQrScanner(onResult: (String) -> Unit): (() -> Unit)? = null

// Google-Anmeldung bietet das iPhone vorerst nicht an (sonst wäre auch „Mit Apple anmelden“ Pflicht).
@Composable
actual fun rememberGoogleSignIn(onError: (isNetworkError: Boolean) -> Unit): (() -> Unit)? = null

private fun topViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}
