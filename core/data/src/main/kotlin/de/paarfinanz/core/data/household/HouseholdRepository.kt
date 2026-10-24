package de.paarfinanz.core.data.household

import de.paarfinanz.core.domain.household.InviteCode
import de.paarfinanz.core.domain.model.FairnessBasis
import de.paarfinanz.core.domain.model.Household
import de.paarfinanz.core.domain.model.Money
import de.paarfinanz.core.domain.model.ShareBps
import de.paarfinanz.core.domain.model.SplitMode
import de.paarfinanz.core.domain.model.UserId
import java.time.LocalDate
import kotlinx.coroutines.flow.StateFlow

sealed interface HouseholdState {
    data object Loading : HouseholdState

    /** Angemeldet, aber noch in keinem Haushalt. */
    data object None : HouseholdState

    data class Ready(val household: Household, val currentUserId: UserId) : HouseholdState {
        val me get() = household.member(currentUserId)
        val partner get() = household.partnerOf(currentUserId)
    }

    /** Laden fehlgeschlagen, bevor je ein Haushalt angezeigt wurde. */
    data class Failed(val error: HouseholdError) : HouseholdState
}

data class InvitePreview(val householdName: String, val inviterName: String)

data class HouseholdSettings(
    val name: String,
    val defaultSplitMode: SplitMode,
    val fairnessBasis: FairnessBasis,
    val jointAccountEnabled: Boolean,
)

interface HouseholdRepository {
    /** Haushalt der angemeldeten Person, live aktualisiert, wenn der Partner etwas ändert. */
    val householdState: StateFlow<HouseholdState>

    suspend fun refresh()

    suspend fun createHousehold(name: String, splitMode: SplitMode): Result<Unit>

    suspend fun createInvite(): Result<InviteCode>

    suspend fun previewInvite(code: InviteCode): Result<InvitePreview>

    suspend fun acceptInvite(code: InviteCode): Result<Unit>

    suspend fun updateSettings(settings: HouseholdSettings): Result<Unit>

    /** Setzt den festen Anteil der angemeldeten Person; der Partner erhält den Rest. */
    suspend fun setMyFixedShare(share: ShareBps): Result<Unit>

    suspend fun saveIncome(netIncome: Money, privateFixedCosts: Money, validFrom: LocalDate): Result<Unit>
}
