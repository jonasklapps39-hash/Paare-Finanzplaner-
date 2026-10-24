package de.paarfinanz.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.paarfinanz.core.data.household.HouseholdRepository
import de.paarfinanz.core.data.household.SupabaseHouseholdRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class HouseholdModule {

    @Binds
    abstract fun bindHouseholdRepository(impl: SupabaseHouseholdRepository): HouseholdRepository
}
