package de.paarfinanz.feature.auth

import androidx.annotation.StringRes

enum class AuthMode { SIGN_IN, SIGN_UP }

data class AuthUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    @StringRes val errorRes: Int? = null,
    @StringRes val infoRes: Int? = null,
)
