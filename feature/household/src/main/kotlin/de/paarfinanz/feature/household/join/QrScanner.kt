package de.paarfinanz.feature.household.join

import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/**
 * Startet den QR-Scanner der Google Play-Dienste. Er läuft in einem eigenen Fenster,
 * die App braucht dafür keine Kamera-Berechtigung.
 */
internal fun startQrScan(context: Context, onResult: (String) -> Unit, onFailure: () -> Unit) {
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    GmsBarcodeScanning.getClient(context, options)
        .startScan()
        .addOnSuccessListener { barcode -> barcode.rawValue?.let(onResult) ?: onFailure() }
        .addOnFailureListener { onFailure() }
}
