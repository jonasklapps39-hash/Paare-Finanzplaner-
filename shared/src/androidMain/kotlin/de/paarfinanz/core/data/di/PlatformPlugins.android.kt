package de.paarfinanz.core.data.di

import de.paarfinanz.core.data.SupabaseConfig
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin

internal actual fun SupabaseClientBuilder.installPlatformPlugins() {
    install(ComposeAuth) {
        googleNativeLogin(serverClientId = SupabaseConfig.googleWebClientId)
    }
}
