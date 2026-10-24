package de.paarfinanz.core.data

/** Zugangsdaten zum Supabase-Projekt, gesetzt über local.properties. */
object SupabaseConfig {
    val url: String = BuildConfig.SUPABASE_URL
    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY
    val googleWebClientId: String = BuildConfig.GOOGLE_WEB_CLIENT_ID

    /** false, solange keine Supabase-Zugangsdaten hinterlegt sind (z. B. im CI-Build). */
    val isConfigured: Boolean get() = url.isNotBlank() && anonKey.isNotBlank()

    val isGoogleSignInAvailable: Boolean get() = isConfigured && googleWebClientId.isNotBlank()

    /** Deeplink, über den Bestätigungs-Mails zurück in die App führen. */
    const val DEEPLINK_SCHEME = "paarfinanz"
    const val DEEPLINK_HOST = "login"
}
