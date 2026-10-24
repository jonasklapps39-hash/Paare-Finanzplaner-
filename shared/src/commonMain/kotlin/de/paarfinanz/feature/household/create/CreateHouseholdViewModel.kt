package de.paarfinanz.feature.household.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.paarfinanz.core.data.household.HouseholdRepository
import de.paarfinanz.core.domain.model.SplitMode
import de.paarfinanz.feature.household.householdMessageRes
import de.paarfinanz.shared.resources.Res
import de.paarfinanz.shared.resources.create_error_name
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

data class CreateHouseholdUiState(
    val name: String = "",
    val splitMode: SplitMode = SplitMode.INCOME,
    val isLoading: Boolean = false,
    val errorRes: StringResource? = null,
)

class CreateHouseholdViewModel(
    private val householdRepository: HouseholdRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateHouseholdUiState())
    val uiState: StateFlow<CreateHouseholdUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value.take(60), errorRes = null) }

    fun onSplitModeChange(mode: SplitMode) = _uiState.update { it.copy(splitMode = mode) }

    /** Nach Erfolg wechselt die App automatisch weiter, weil sich der Haushaltsstatus ändert. */
    fun create() {
        val state = _uiState.value
        if (state.isLoading) return
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorRes = Res.string.create_error_name) }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorRes = null) }
        viewModelScope.launch {
            householdRepository.createHousehold(state.name, state.splitMode)
                .onSuccess { _uiState.update { it.copy(isLoading = false) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorRes = error.householdMessageRes()) } }
        }
    }
}
