package de.paarfinanz.feature.auth

import org.jetbrains.compose.resources.StringResource

enum class AuthMode { SIGN_IN, SIGN_UP }

data class AuthUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorRes: StringResource? = null,
    val infoRes: StringResource? = null,
)
