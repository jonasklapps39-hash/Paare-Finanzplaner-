package de.paarfinanz.core.data.di

import de.paarfinanz.core.data.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

internal fun createPaarfinanzSupabaseClient(): SupabaseClient = createSupabaseClient(
    // Platzhalter, damit die App auch ohne Konfiguration startet; Anfragen schlagen dann fehl.
    supabaseUrl = SupabaseConfig.url.ifBlank { "https://nicht-konfiguriert.supabase.co" },
    supabaseKey = SupabaseConfig.anonKey.ifBlank { "nicht-konfiguriert" },
) {
    install(Auth) {
        scheme = SupabaseConfig.DEEPLINK_SCHEME
        host = SupabaseConfig.DEEPLINK_HOST
    }
    install(Postgrest)
    install(Realtime)
    installPlatformPlugins()
}

/** Plattform-spezifische Supabase-Plugins, z. B. der native Google-Login auf Android. */
internal expect fun SupabaseClientBuilder.installPlatformPlugins()
