package de.paarfinanz.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.paarfinanz.core.data.SupabaseConfig
import de.paarfinanz.core.data.auth.AuthError
import de.paarfinanz.core.data.auth.AuthException
import de.paarfinanz.core.data.auth.AuthRepository
import de.paarfinanz.core.data.auth.SignUpResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composeAuth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    supabaseClient: SupabaseClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(infoRes = if (SupabaseConfig.isConfigured) null else R.string.auth_not_configured),
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** Für den nativen Google-Login (Credential Manager). */
    val composeAuth: ComposeAuth = supabaseClient.composeAuth
    val isGoogleSignInAvailable: Boolean = SupabaseConfig.isGoogleSignInAvailable

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
                                it.copy(isLoading = false, mode = AuthMode.SIGN_IN, password = "", infoRes = R.string.auth_confirm_email)
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
            _uiState.update { it.copy(errorRes = R.string.auth_error_invalid_email) }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorRes = null) }
        viewModelScope.launch {
            authRepository.sendPasswordReset(email)
                .onSuccess { _uiState.update { it.copy(isLoading = false, infoRes = R.string.auth_reset_sent) } }
                .onFailure(::showError)
        }
    }

    fun onGoogleResult(result: NativeSignInResult) {
        when (result) {
            is NativeSignInResult.Error -> _uiState.update { it.copy(errorRes = R.string.auth_error_google) }
            is NativeSignInResult.NetworkError -> _uiState.update { it.copy(errorRes = R.string.auth_error_network) }
            // Erfolg wird über den Sitzungsstatus erkannt, Abbruch braucht keine Meldung.
            else -> Unit
        }
    }

    private fun showError(error: Throwable) {
        val authError = (error as? AuthException)?.error ?: AuthError.from(error)
        _uiState.update { it.copy(isLoading = false, errorRes = authError.toMessage()) }
    }

    private fun AuthError.toMessage(): Int = when (this) {
        AuthError.INVALID_CREDENTIALS -> R.string.auth_error_invalid_credentials
        AuthError.EMAIL_NOT_CONFIRMED -> R.string.auth_error_email_not_confirmed
        AuthError.USER_ALREADY_EXISTS -> R.string.auth_error_user_exists
        AuthError.WEAK_PASSWORD -> R.string.auth_error_weak_password
        AuthError.RATE_LIMITED -> R.string.auth_error_rate_limited
        AuthError.NETWORK -> R.string.auth_error_network
        AuthError.NOT_CONFIGURED -> R.string.auth_not_configured
        AuthError.UNKNOWN -> R.string.auth_error_unknown
    }
}
