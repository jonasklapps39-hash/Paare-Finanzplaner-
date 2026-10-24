package de.paarfinanz.feature.auth

import de.paarfinanz.shared.resources.Res
import de.paarfinanz.shared.resources.auth_error_invalid_email
import de.paarfinanz.shared.resources.auth_error_missing_name
import de.paarfinanz.shared.resources.auth_error_short_password
import org.jetbrains.compose.resources.StringResource
internal object AuthValidation {
    const val MIN_PASSWORD_LENGTH = 8

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun isValidEmail(email: String): Boolean = emailRegex.matches(email.trim())

    fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

    /** Liefert die erste passende Fehlermeldung oder null, wenn alles passt. */
    fun validate(state: AuthUiState): StringResource? = when {
        state.mode == AuthMode.SIGN_UP && state.displayName.isBlank() -> Res.string.auth_error_missing_name
        !isValidEmail(state.email) -> Res.string.auth_error_invalid_email
        state.mode == AuthMode.SIGN_UP && !isValidPassword(state.password) -> Res.string.auth_error_short_password
        state.password.isEmpty() -> Res.string.auth_error_short_password
        else -> null
    }
}
