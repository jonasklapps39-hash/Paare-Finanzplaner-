package de.paarfinanz.feature.household.join

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.paarfinanz.core.data.household.HouseholdRepository
import de.paarfinanz.core.data.household.InvitePreview
import de.paarfinanz.core.domain.household.InviteCode
import de.paarfinanz.feature.household.R
import de.paarfinanz.feature.household.householdMessageRes
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JoinHouseholdUiState(
    val codeInput: String = "",
    val preview: InvitePreview? = null,
    val isLoading: Boolean = false,
    @StringRes val errorRes: Int? = null,
)

@HiltViewModel
class JoinHouseholdViewModel @Inject constructor(
    private val householdRepository: HouseholdRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinHouseholdUiState())
    val uiState: StateFlow<JoinHouseholdUiState> = _uiState.asStateFlow()

    init {
        // Aus einem Einladungslink (paarfinanz://invite/CODE) direkt vorausfüllen und prüfen.
        savedStateHandle.get<String>(ARG_CODE)?.takeIf { it.isNotBlank() }?.let(::onScanned)
    }

    fun onCodeChange(value: String) = _uiState.update {
        it.copy(codeInput = value.take(MAX_INPUT_LENGTH), preview = null, errorRes = null)
    }

    fun onScanned(rawValue: String) {
        val code = InviteCode.parse(rawValue)
        if (code == null) {
            _uiState.update { it.copy(errorRes = R.string.join_error_scan, preview = null) }
            return
        }
        _uiState.update { it.copy(codeInput = code.formatted, preview = null, errorRes = null) }
        checkCode()
    }

    fun checkCode() {
        val code = parsedCodeOrShowError() ?: return
        _uiState.update { it.copy(isLoading = true, errorRes = null) }
        viewModelScope.launch {
            householdRepository.previewInvite(code)
                .onSuccess { preview -> _uiState.update { it.copy(isLoading = false, preview = preview) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorRes = error.householdMessageRes()) } }
        }
    }

    /** Nach Erfolg wechselt die App automatisch weiter, weil sich der Haushaltsstatus ändert. */
    fun accept() {
        val code = parsedCodeOrShowError() ?: return
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorRes = null) }
        viewModelScope.launch {
            householdRepository.acceptInvite(code)
                .onSuccess { _uiState.update { it.copy(isLoading = false) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorRes = error.householdMessageRes()) } }
        }
    }

    private fun parsedCodeOrShowError(): InviteCode? {
        val code = InviteCode.parse(_uiState.value.codeInput)
        if (code == null) _uiState.update { it.copy(errorRes = R.string.join_error_format) }
        return code
    }

    companion object {
        const val ARG_CODE = "code"
        private const val MAX_INPUT_LENGTH = 12
    }
}
