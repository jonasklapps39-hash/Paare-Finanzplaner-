package de.paarfinanz.core.data.di

import io.github.jan.supabase.SupabaseClientBuilder

// Auf dem iPhone gibt es vorerst nur die E-Mail-Anmeldung; keine zusätzlichen Plugins nötig.
internal actual fun SupabaseClientBuilder.installPlatformPlugins() = Unit
