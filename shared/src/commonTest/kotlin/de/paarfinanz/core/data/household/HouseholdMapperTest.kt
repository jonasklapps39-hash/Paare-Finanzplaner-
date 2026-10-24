package de.paarfinanz.core.data.household

import de.paarfinanz.core.domain.model.FairnessBasis
import de.paarfinanz.core.domain.model.MemberRole
import de.paarfinanz.core.domain.model.Money
import de.paarfinanz.core.domain.model.SplitMode
import de.paarfinanz.core.domain.model.UserId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class HouseholdMapperTest {

    private val household = HouseholdDto(
        id = "h1",
        name = "Anna & Ben",
        currency = "EUR",
        defaultSplitMode = "FIXED",
        fairnessBasis = "DISPOSABLE",
        jointAccountEnabled = true,
    )

    @Test
    fun besitzerStehtImmerZuerst() {
        val members = listOf(
            MemberDto("ben", "MEMBER", 4_000, "2026-01-01T10:00:00Z", ProfileNameDto("Ben")),
            MemberDto("anna", "OWNER", 6_000, "2026-01-02T10:00:00Z", ProfileNameDto("Anna")),
        )
        val result = HouseholdMapper.toDomain(household, members, emptyList())
        assertEquals(listOf("anna", "ben"), result.members.map { it.userId.value })
        assertEquals(MemberRole.OWNER, result.members.first().role)
        assertEquals(SplitMode.FIXED, result.defaultSplitMode)
        assertEquals(FairnessBasis.DISPOSABLE, result.fairnessBasis)
    }

    @Test
    fun einkommenWerdenUebernommen() {
        val incomes = listOf(IncomeDto("anna", 300_000, 50_000, "2026-10-01"))
        val result = HouseholdMapper.toDomain(household, emptyList(), incomes)
        val income = result.incomes.single()
        assertEquals(UserId("anna"), income.userId)
        assertEquals(Money(300_000), income.netIncome)
        assertEquals(Money(50_000), income.privateFixedCosts)
        assertEquals(LocalDate(2026, 10, 1), income.validFrom)
    }

    @Test
    fun unbekannteWerteFallenAufStandardZurueck() {
        val result = HouseholdMapper.toDomain(household.copy(defaultSplitMode = "NEU"), emptyList(), emptyList())
        assertEquals(SplitMode.INCOME, result.defaultSplitMode)
    }
}
