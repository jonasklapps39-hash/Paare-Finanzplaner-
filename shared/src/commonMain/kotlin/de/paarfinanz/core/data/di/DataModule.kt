package de.paarfinanz.core.data.di

import de.paarfinanz.core.data.auth.AuthRepository
import de.paarfinanz.core.data.auth.SupabaseAuthRepository
import de.paarfinanz.core.data.household.HouseholdRepository
import de.paarfinanz.core.data.household.SupabaseHouseholdRepository
import org.koin.dsl.module

val dataModule = module {
    single { createPaarfinanzSupabaseClient() }
    single<AuthRepository> { SupabaseAuthRepository(get()) }
    single<HouseholdRepository> { SupabaseHouseholdRepository(get(), get()) }
}
