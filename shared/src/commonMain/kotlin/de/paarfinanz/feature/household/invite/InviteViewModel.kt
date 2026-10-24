package de.paarfinanz.feature.household.invite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.paarfinanz.core.data.household.HouseholdRepository
import de.paarfinanz.core.data.household.HouseholdState
import de.paarfinanz.core.domain.household.InviteCode
import de.paarfinanz.feature.household.householdMessageRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

data class InviteUiState(
    val code: InviteCode? = null,
    val isLoading: Boolean = true,
    /** Name des Partners, sobald er beigetreten ist ("" wenn ohne Namen). */
    val joinedPartnerName: String? = null,
    val errorRes: StringResource? = null,
)

class InviteViewModel(
    private val householdRepository: HouseholdRepository,
) : ViewModel() {

    private val inviteState = MutableStateFlow(InviteUiState())

    val uiState: StateFlow<InviteUiState> = combine(inviteState, householdRepository.householdState) { invite, household ->
        val partner = (household as? HouseholdState.Ready)?.partner
        invite.copy(joinedPartnerName = partner?.displayName)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InviteUiState())

    init {
        viewModelScope.launch {
            // Nur einladen, solange der Haushalt noch nicht vollständig ist.
            val household = householdRepository.householdState.first { it !is HouseholdState.Loading }
            if ((household as? HouseholdState.Ready)?.household?.isComplete == true) {
                inviteState.update { it.copy(isLoading = false) }
            } else {
                createCode()
            }
        }
    }

    fun createCode() {
        inviteState.update { it.copy(isLoading = true, errorRes = null) }
        viewModelScope.launch {
            householdRepository.createInvite()
                .onSuccess { code -> inviteState.update { it.copy(code = code, isLoading = false) } }
                .onFailure { error -> inviteState.update { it.copy(isLoading = false, errorRes = error.householdMessageRes()) } }
        }
    }
}
