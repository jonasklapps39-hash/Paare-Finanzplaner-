package de.paarfinanz.core.domain.split

import de.paarfinanz.core.domain.model.FairnessBasis
import de.paarfinanz.core.domain.model.MemberIncome
import de.paarfinanz.core.domain.model.Money
import de.paarfinanz.core.domain.model.ShareBps
import de.paarfinanz.core.domain.model.UserId
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FairnessCalculatorTest {

    private val anna = UserId("anna")
    private val ben = UserId("ben")
    private val jan1 = LocalDate(2026, 1, 1)

    @Test
    fun nettogehalt3000Zu2000Ergibt60Zu40() {
        val incomes = listOf(
            MemberIncome(anna, Money.ofEuros(3_000), validFrom = jan1),
            MemberIncome(ben, Money.ofEuros(2_000), validFrom = jan1),
        )
        val shares = FairnessCalculator.incomeShares(anna, ben, incomes, FairnessBasis.NET, jan1)
        assertEquals(ShareBps(6_000), shares.firstShare)
        assertEquals(ShareBps(4_000), shares.secondShare)
        assertFalse(shares.isFallback)
    }

    @Test
    fun freiVerfuegbaresEinkommenBeruecksichtigtPrivateFixkosten() {
        val incomes = listOf(
            MemberIncome(anna, Money.ofEuros(3_000), Money.ofEuros(1_000), jan1),
            MemberIncome(ben, Money.ofEuros(2_000), Money.ZERO, jan1),
        )
        val shares = FairnessCalculator.incomeShares(anna, ben, incomes, FairnessBasis.DISPOSABLE, jan1)
        assertEquals(ShareBps.HALF, shares.firstShare)
    }

    @Test
    fun fixkostenHoeherAlsNettoZaehlenAlsNullEinkommen() {
        val incomes = listOf(
            MemberIncome(anna, Money.ofEuros(1_000), Money.ofEuros(1_500), jan1),
            MemberIncome(ben, Money.ofEuros(2_000), Money.ZERO, jan1),
        )
        val shares = FairnessCalculator.incomeShares(anna, ben, incomes, FairnessBasis.DISPOSABLE, jan1)
        assertEquals(ShareBps.NONE, shares.firstShare)
        assertEquals(ShareBps.FULL, shares.secondShare)
    }

    @Test
    fun gehaltsaenderungGiltErstAbIhremStichtag() {
        val incomes = listOf(
            MemberIncome(anna, Money.ofEuros(2_000), validFrom = jan1),
            MemberIncome(anna, Money.ofEuros(3_000), validFrom = LocalDate(2026, 6, 1)),
            MemberIncome(ben, Money.ofEuros(2_000), validFrom = jan1),
        )
        val may = FairnessCalculator.incomeShares(anna, ben, incomes, FairnessBasis.NET, LocalDate(2026, 5, 31))
        val june = FairnessCalculator.incomeShares(anna, ben, incomes, FairnessBasis.NET, LocalDate(2026, 6, 1))
        assertEquals(ShareBps.HALF, may.firstShare)
        assertEquals(ShareBps(6_000), june.firstShare)
    }

    @Test
    fun krummeVerhaeltnisseSummierenSichTrotzdemAuf100Prozent() {
        val incomes = listOf(
            MemberIncome(anna, Money(100), validFrom = jan1),
            MemberIncome(ben, Money(200), validFrom = jan1),
        )
        val shares = FairnessCalculator.incomeShares(anna, ben, incomes, FairnessBasis.NET, jan1)
        assertEquals(ShareBps(3_333), shares.firstShare)
        assertEquals(ShareBps(6_667), shares.secondShare)
    }

    @Test
    fun fehlendesEinkommenFaelltAuf50Zu50Zurueck() {
        val incomes = listOf(MemberIncome(anna, Money.ofEuros(3_000), validFrom = jan1))
        val shares = FairnessCalculator.incomeShares(anna, ben, incomes, FairnessBasis.NET, jan1)
        assertEquals(ShareBps.HALF, shares.firstShare)
        assertTrue(shares.isFallback)
    }

    @Test
    fun einkommenVorDemStichtagWirdIgnoriertWennEsNochNichtGilt() {
        val incomes = listOf(
            MemberIncome(anna, Money.ofEuros(3_000), validFrom = LocalDate(2026, 2, 1)),
            MemberIncome(ben, Money.ofEuros(2_000), validFrom = jan1),
        )
        val shares = FairnessCalculator.incomeShares(anna, ben, incomes, FairnessBasis.NET, jan1)
        assertTrue(shares.isFallback)
    }
}
