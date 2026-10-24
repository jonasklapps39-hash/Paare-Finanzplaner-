package de.paarfinanz.core.data.auth

import de.paarfinanz.core.data.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Singleton
class SupabaseAuthRepository @Inject constructor(
    private val client: SupabaseClient,
) : AuthRepository {

    override val authState: Flow<AuthState> = client.auth.sessionStatus
        .map { status ->
            when (status) {
                is SessionStatus.Authenticated -> status.session.user
                    ?.let { AuthState.SignedIn(it.id, it.email) }
                    ?: AuthState.SignedOut
                is SessionStatus.NotAuthenticated -> AuthState.SignedOut
                // Beim Start oder wenn das Token offline nicht erneuert werden konnte:
                // vorhandenen Nutzer behalten, damit die App offline nutzbar bleibt.
                else -> client.auth.currentUserOrNull()
                    ?.let { AuthState.SignedIn(it.id, it.email) }
                    ?: AuthState.Loading
            }
        }
        .distinctUntilChanged()

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatchingAuth {
        client.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }

    override suspend fun signUp(email: String, password: String, displayName: String): Result<SignUpResult> =
        runCatchingAuth {
            client.auth.signUpWith(Email) {
                this.email = email.trim()
                this.password = password
                data = buildJsonObject { put("display_name", displayName.trim()) }
            }
            if (client.auth.currentSessionOrNull() != null) SignUpResult.SignedIn else SignUpResult.ConfirmationRequired
        }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatchingAuth {
        client.auth.resetPasswordForEmail(email.trim())
    }

    override suspend fun signOut(): Result<Unit> = runCatchingAuth {
        client.auth.signOut()
    }

    private inline fun <T> runCatchingAuth(block: () -> T): Result<T> {
        if (!SupabaseConfig.isConfigured) {
            return Result.failure(AuthException(AuthError.NOT_CONFIGURED))
        }
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(AuthException(AuthError.from(e), e))
        }
    }
}
