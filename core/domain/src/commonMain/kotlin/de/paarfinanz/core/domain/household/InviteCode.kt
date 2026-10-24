package de.paarfinanz.core.domain.household

import kotlin.jvm.JvmInline

/**
 * Einladungscode aus 8 Zeichen (A–Z, 0–9). Eingaben sind tolerant:
 * Groß-/Kleinschreibung, Bindestriche, Leerzeichen und der QR-Link werden akzeptiert.
 */
@JvmInline
value class InviteCode private constructor(val value: String) {

    /** Zum Anzeigen und Vorlesen: "AB12-CD34". */
    val formatted: String get() = value.chunked(GROUP_SIZE).joinToString("-")

    /** Inhalt des QR-Codes. */
    val deepLink: String get() = "$DEEPLINK_PREFIX$value"

    companion object {
        const val LENGTH = 8
        const val DEEPLINK_PREFIX = "paarfinanz://invite/"
        private const val GROUP_SIZE = 4

        fun parse(input: String): InviteCode? {
            val trimmed = input.trim()
            val raw = if (trimmed.startsWith(DEEPLINK_PREFIX, ignoreCase = true)) {
                trimmed.substring(DEEPLINK_PREFIX.length)
            } else {
                trimmed
            }
            val normalized = raw.uppercase().filter { it in 'A'..'Z' || it in '0'..'9' }
            return if (normalized.length == LENGTH) InviteCode(normalized) else null
        }
    }
}
