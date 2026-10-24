package de.paarfinanz.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.paarfinanz.core.data.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
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
        install(ComposeAuth) {
            googleNativeLogin(serverClientId = SupabaseConfig.googleWebClientId)
        }
    }
}
