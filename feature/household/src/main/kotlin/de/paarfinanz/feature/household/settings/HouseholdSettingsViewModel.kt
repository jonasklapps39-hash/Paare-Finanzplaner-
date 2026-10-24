package de.paarfinanz.feature.household.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.paarfinanz.core.data.household.HouseholdRepository
import de.paarfinanz.core.data.household.HouseholdSettings
import de.paarfinanz.core.data.household.HouseholdState
import de.paarfinanz.core.domain.model.FairnessBasis
import de.paarfinanz.core.domain.model.ShareBps
import de.paarfinanz.core.domain.model.SplitMode
import de.paarfinanz.feature.household.householdMessageRes
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HouseholdSettingsViewModel @Inject constructor(
    private val householdRepository: HouseholdRepository,
) : ViewModel() {

    val householdState: StateFlow<HouseholdState> = householdRepository.householdState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), householdRepository.householdState.value)

    private val _errorRes = MutableStateFlow<Int?>(null)
    /** Fehlermeldung als String-Ressource. */
    val errorRes: StateFlow<Int?> = _errorRes.asStateFlow()

    fun rename(name: String) {
        if (name.isBlank()) return
        updateSettings { it.copy(name = name.trim().take(60)) }
    }

    fun setDefaultSplitMode(mode: SplitMode) = updateSettings { it.copy(defaultSplitMode = mode) }

    fun setFairnessBasis(basis: FairnessBasis) = updateSettings { it.copy(fairnessBasis = basis) }

    fun setJointAccountEnabled(enabled: Boolean) = updateSettings { it.copy(jointAccountEnabled = enabled) }

    fun setMyFixedShare(percent: Int) {
        _errorRes.value = null
        viewModelScope.launch {
            householdRepository.setMyFixedShare(ShareBps.ofPercent(percent.coerceIn(0, 100)))
                .onFailure { _errorRes.value = it.householdMessageRes() }
        }
    }

    private fun updateSettings(change: (HouseholdSettings) -> HouseholdSettings) {
        val household = (householdState.value as? HouseholdState.Ready)?.household ?: return
        val current = HouseholdSettings(
            name = household.name,
            defaultSplitMode = household.defaultSplitMode,
            fairnessBasis = household.fairnessBasis,
            jointAccountEnabled = household.jointAccountEnabled,
        )
        val updated = change(current)
        if (updated == current) return
        _errorRes.value = null
        viewModelScope.launch {
            householdRepository.updateSettings(updated)
                .onFailure { _errorRes.value = it.householdMessageRes() }
        }
    }
}
