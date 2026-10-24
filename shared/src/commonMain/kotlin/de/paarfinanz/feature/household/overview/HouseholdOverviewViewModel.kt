package de.paarfinanz.feature.household.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.paarfinanz.core.data.household.HouseholdRepository
import de.paarfinanz.core.data.household.HouseholdState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HouseholdOverviewViewModel(
    householdRepository: HouseholdRepository,
) : ViewModel() {

    val householdState: StateFlow<HouseholdState> = householdRepository.householdState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), householdRepository.householdState.value)
}
