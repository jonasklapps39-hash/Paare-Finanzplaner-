package de.paarfinanz.feature.auth

internal object AuthValidation {
    const val MIN_PASSWORD_LENGTH = 8

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun isValidEmail(email: String): Boolean = emailRegex.matches(email.trim())

    fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

    /** Liefert die erste passende Fehlermeldung oder null, wenn alles passt. */
    fun validate(state: AuthUiState): Int? = when {
        state.mode == AuthMode.SIGN_UP && state.displayName.isBlank() -> R.string.auth_error_missing_name
        !isValidEmail(state.email) -> R.string.auth_error_invalid_email
        state.mode == AuthMode.SIGN_UP && !isValidPassword(state.password) -> R.string.auth_error_short_password
        state.password.isEmpty() -> R.string.auth_error_short_password
        else -> null
    }
}
