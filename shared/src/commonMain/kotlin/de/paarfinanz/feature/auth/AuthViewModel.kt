package de.paarfinanz.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.paarfinanz.core.data.SupabaseConfig
import de.paarfinanz.core.data.auth.AuthError
import de.paarfinanz.core.data.auth.AuthException
import de.paarfinanz.core.data.auth.AuthRepository
import de.paarfinanz.core.data.auth.SignUpResult
import de.paarfinanz.shared.resources.Res
import de.paarfinanz.shared.resources.auth_confirm_email
import de.paarfinanz.shared.resources.auth_error_email_not_confirmed
import de.paarfinanz.shared.resources.auth_error_google
import de.paarfinanz.shared.resources.auth_error_invalid_credentials
import de.paarfinanz.shared.resources.auth_error_invalid_email
import de.paarfinanz.shared.resources.auth_error_network
import de.paarfinanz.shared.resources.auth_error_rate_limited
import de.paarfinanz.shared.resources.auth_error_unknown
import de.paarfinanz.shared.resources.auth_error_user_exists
import de.paarfinanz.shared.resources.auth_error_weak_password
import de.paarfinanz.shared.resources.auth_not_configured
import de.paarfinanz.shared.resources.auth_reset_sent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(infoRes = if (SupabaseConfig.isConfigured) null else Res.string.auth_not_configured),
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onDisplayNameChange(value: String) = _uiState.update { it.copy(displayName = value.take(60), errorRes = null) }

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, errorRes = null) }

    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorRes = null) }

    fun toggleMode() = _uiState.update {
        it.copy(
            mode = if (it.mode == AuthMode.SIGN_IN) AuthMode.SIGN_UP else AuthMode.SIGN_IN,
            errorRes = null,
            infoRes = null,
        )
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return
        AuthValidation.validate(state)?.let { error ->
            _uiState.update { it.copy(errorRes = error) }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorRes = null, infoRes = null) }
        viewModelScope.launch {
            when (state.mode) {
                AuthMode.SIGN_IN -> authRepository.signIn(state.email, state.password)
                    .onSuccess { _uiState.update { it.copy(isLoading = false) } }
                    .onFailure(::showError)
                AuthMode.SIGN_UP -> authRepository.signUp(state.email, state.password, state.displayName)
                    .onSuccess { result ->
                        _uiState.update {
                            if (result == SignUpResult.ConfirmationRequired) {
                                it.copy(isLoading = false, mode = AuthMode.SIGN_IN, password = "", infoRes = Res.string.auth_confirm_email)
                            } else {
                                it.copy(isLoading = false)
                            }
                        }
                    }
                    .onFailure(::showError)
            }
        }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.email
        if (!AuthValidation.isValidEmail(email)) {
            _uiState.update { it.copy(errorRes = Res.string.auth_error_invalid_email) }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorRes = null) }
        viewModelScope.launch {
            authRepository.sendPasswordReset(email)
                .onSuccess { _uiState.update { it.copy(isLoading = false, infoRes = Res.string.auth_reset_sent) } }
                .onFailure(::showError)
        }
    }

    /** Fehler aus der nativen Google-Anmeldung (Erfolg kommt über den Sitzungsstatus). */
    fun onGoogleError(isNetworkError: Boolean) = _uiState.update {
        it.copy(errorRes = if (isNetworkError) Res.string.auth_error_network else Res.string.auth_error_google)
    }

    private fun showError(error: Throwable) {
        val authError = (error as? AuthException)?.error ?: AuthError.from(error)
        _uiState.update { it.copy(isLoading = false, errorRes = authError.toMessage()) }
    }

    private fun AuthError.toMessage(): StringResource = when (this) {
        AuthError.INVALID_CREDENTIALS -> Res.string.auth_error_invalid_credentials
        AuthError.EMAIL_NOT_CONFIRMED -> Res.string.auth_error_email_not_confirmed
        AuthError.USER_ALREADY_EXISTS -> Res.string.auth_error_user_exists
        AuthError.WEAK_PASSWORD -> Res.string.auth_error_weak_password
        AuthError.RATE_LIMITED -> Res.string.auth_error_rate_limited
        AuthError.NETWORK -> Res.string.auth_error_network
        AuthError.NOT_CONFIGURED -> Res.string.auth_not_configured
        AuthError.UNKNOWN -> Res.string.auth_error_unknown
    }
}
