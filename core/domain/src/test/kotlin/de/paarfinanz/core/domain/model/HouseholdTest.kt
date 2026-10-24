package de.paarfinanz.core.domain.model

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HouseholdTest {

    private val anna = HouseholdMember(UserId("anna"), "Anna", MemberRole.OWNER, ShareBps(6_000))
    private val ben = HouseholdMember(UserId("ben"), "Ben", MemberRole.MEMBER, ShareBps(4_000))
    private val jan1 = LocalDate.of(2026, 1, 1)

    private fun household(members: List<HouseholdMember>, incomes: List<MemberIncome> = emptyList()) = Household(
        id = "h1",
        name = "Anna & Ben",
        currency = "EUR",
        defaultSplitMode = SplitMode.INCOME,
        fairnessBasis = FairnessBasis.NET,
        jointAccountEnabled = false,
        members = members,
        incomes = incomes,
    )

    @Test
    fun `ohne partner gibt es noch keine aufteilung`() {
        val single = household(listOf(anna))
        assertFalse(single.isComplete)
        assertNull(single.incomeShares(jan1))
        assertNull(single.fixedShares())
        assertNull(single.partnerOf(anna.userId))
    }

    @Test
    fun `vollstaendiger haushalt berechnet anteile`() {
        val incomes = listOf(
            MemberIncome(anna.userId, Money.ofEuros(3_000), validFrom = jan1),
            MemberIncome(ben.userId, Money.ofEuros(2_000), validFrom = jan1),
        )
        val complete = household(listOf(anna, ben), incomes)
        assertTrue(complete.isComplete)
        assertEquals(ShareBps(6_000), complete.incomeShares(jan1)?.shareOf(anna.userId))
        assertEquals(ShareBps(4_000), complete.fixedShares()?.shareOf(ben.userId))
        assertEquals(ben, complete.partnerOf(anna.userId))
        assertEquals(1, complete.colorIndexOf(ben.userId))
    }
}
