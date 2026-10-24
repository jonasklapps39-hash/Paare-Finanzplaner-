package de.paarfinanz.core.data.auth

import kotlinx.coroutines.flow.Flow

sealed interface AuthState {
    /** Gespeicherte Sitzung wird noch geladen. */
    data object Loading : AuthState

    data object SignedOut : AuthState

    data class SignedIn(val userId: String, val email: String?) : AuthState
}

sealed interface SignUpResult {
    /** Konto angelegt und direkt angemeldet. */
    data object SignedIn : SignUpResult

    /** Konto angelegt, die E-Mail-Adresse muss noch bestätigt werden. */
    data object ConfirmationRequired : SignUpResult
}

interface AuthRepository {
    val authState: Flow<AuthState>

    suspend fun signIn(email: String, password: String): Result<Unit>

    suspend fun signUp(email: String, password: String, displayName: String): Result<SignUpResult>

    suspend fun sendPasswordReset(email: String): Result<Unit>

    suspend fun signOut(): Result<Unit>
}
