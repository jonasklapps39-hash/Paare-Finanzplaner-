package de.paarfinanz.core.data

/** Zugangsdaten zum Supabase-Projekt (siehe supabase.properties bzw. local.properties). */
object SupabaseConfig {
    val url: String = AppConfig.SUPABASE_URL
    val anonKey: String = AppConfig.SUPABASE_ANON_KEY
    val googleWebClientId: String = AppConfig.GOOGLE_WEB_CLIENT_ID

    /** false, solange keine Supabase-Zugangsdaten hinterlegt sind (z. B. im CI-Build). */
    val isConfigured: Boolean get() = url.isNotBlank() && anonKey.isNotBlank()

    val versionName: String = AppConfig.VERSION_NAME

    /** Google-Anmeldung gibt es nur auf Android (auf dem iPhone ginge sie nur mit „Mit Apple anmelden“). */
    val isGoogleSignInAvailable: Boolean get() = isConfigured && googleWebClientId.isNotBlank() && Platform.isAndroid

    /** Deeplink, über den Bestätigungs-Mails zurück in die App führen. */
    const val DEEPLINK_SCHEME = "paarfinanz"
    const val DEEPLINK_HOST = "login"
}
