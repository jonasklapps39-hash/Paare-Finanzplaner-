package de.paarfinanz.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.paarfinanz.core.data.auth.AuthRepository
import de.paarfinanz.core.data.auth.AuthState
import de.paarfinanz.core.data.household.HouseholdRepository
import de.paarfinanz.core.data.household.HouseholdState
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RootViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val householdRepository: HouseholdRepository,
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthState.Loading)

    val householdState: StateFlow<HouseholdState> = householdRepository.householdState

    fun retry() {
        viewModelScope.launch { householdRepository.refresh() }
    }
}
