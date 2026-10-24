package de.paarfinanz.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.paarfinanz.core.data.auth.AuthRepository
import de.paarfinanz.core.data.auth.SupabaseAuthRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    abstract fun bindAuthRepository(impl: SupabaseAuthRepository): AuthRepository
}
