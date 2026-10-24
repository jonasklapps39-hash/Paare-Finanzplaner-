package de.paarfinanz.core.ui.platform

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import de.paarfinanz.core.data.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import org.koin.compose.koinInject

@Composable
actual fun rememberShareText(): (text: String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { text ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, null))
        }
    }
}

/** QR-Scanner der Google Play-Dienste: eigenes Fenster, keine Kamera-Berechtigung nötig. */
@Composable
actual fun rememberQrScanner(onResult: (String) -> Unit): (() -> Unit)? {
    val context = LocalContext.current
    return remember(context, onResult) {
        {
            val options = GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
            GmsBarcodeScanning.getClient(context, options)
                .startScan()
                .addOnSuccessListener { barcode -> barcode.rawValue?.let(onResult) }
        }
    }
}

@Composable
actual fun rememberGoogleSignIn(onError: (isNetworkError: Boolean) -> Unit): (() -> Unit)? {
    if (!SupabaseConfig.isGoogleSignInAvailable) return null
    val client = koinInject<SupabaseClient>()
    val state = client.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Error -> onError(false)
                is NativeSignInResult.NetworkError -> onError(true)
                // Erfolg wird über den Sitzungsstatus erkannt, Abbruch braucht keine Meldung.
                else -> Unit
            }
        },
    )
    return { state.startFlow() }
}
