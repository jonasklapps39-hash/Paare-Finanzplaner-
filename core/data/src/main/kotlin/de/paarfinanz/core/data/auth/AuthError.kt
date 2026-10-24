package de.paarfinanz.core.data.auth

import io.github.jan.supabase.exceptions.HttpRequestException

/** Fachliche Fehlerarten, die die Oberfläche verständlich anzeigen kann. */
enum class AuthError {
    INVALID_CREDENTIALS,
    EMAIL_NOT_CONFIRMED,
    USER_ALREADY_EXISTS,
    WEAK_PASSWORD,
    RATE_LIMITED,
    NETWORK,
    NOT_CONFIGURED,
    UNKNOWN;

    companion object {
        fun from(error: Throwable): AuthError {
            if (error is HttpRequestException) return NETWORK
            val message = error.message.orEmpty().lowercase()
            return when {
                "invalid login credentials" in message || "invalid_credentials" in message -> INVALID_CREDENTIALS
                "email not confirmed" in message || "email_not_confirmed" in message -> EMAIL_NOT_CONFIRMED
                "already registered" in message || "user_already_exists" in message -> USER_ALREADY_EXISTS
                "password" in message && ("weak" in message || "at least" in message) -> WEAK_PASSWORD
                "rate limit" in message || "over_email_send_rate_limit" in message -> RATE_LIMITED
                else -> UNKNOWN
            }
        }
    }
}

class AuthException(val error: AuthError, cause: Throwable? = null) : Exception(error.name, cause)
