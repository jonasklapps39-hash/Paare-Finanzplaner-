package de.paarfinanz.feature.household

import de.paarfinanz.core.data.household.HouseholdError
import de.paarfinanz.core.data.household.HouseholdException
import de.paarfinanz.core.data.household.HouseholdRepository
import de.paarfinanz.core.data.household.HouseholdSettings
import de.paarfinanz.core.data.household.HouseholdState
import de.paarfinanz.core.data.household.InvitePreview
import de.paarfinanz.core.domain.household.InviteCode
import de.paarfinanz.core.domain.model.Money
import de.paarfinanz.core.domain.model.ShareBps
import de.paarfinanz.core.domain.model.SplitMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate

/** Einfache Test-Implementierung: kennt genau einen gültigen Code. */
class FakeHouseholdRepository(
    private val validCode: String = "AB12CD34",
) : HouseholdRepository {

    override val householdState = MutableStateFlow<HouseholdState>(HouseholdState.None)
    val acceptedCodes = mutableListOf<String>()

    override suspend fun refresh() = Unit

    override suspend fun createHousehold(name: String, splitMode: SplitMode) = Result.success(Unit)

    override suspend fun createInvite(): Result<InviteCode> = Result.success(InviteCode.parse(validCode)!!)

    override suspend fun previewInvite(code: InviteCode): Result<InvitePreview> =
        if (code.value == validCode) {
            Result.success(InvitePreview(householdName = "Anna & Ben", inviterName = "Anna"))
        } else {
            Result.failure(HouseholdException(HouseholdError.INVITE_INVALID))
        }

    override suspend fun acceptInvite(code: InviteCode): Result<Unit> {
        acceptedCodes += code.value
        return Result.success(Unit)
    }

    override suspend fun updateSettings(settings: HouseholdSettings) = Result.success(Unit)

    override suspend fun setMyFixedShare(share: ShareBps) = Result.success(Unit)

    override suspend fun saveIncome(netIncome: Money, privateFixedCosts: Money, validFrom: LocalDate) = Result.success(Unit)
}
