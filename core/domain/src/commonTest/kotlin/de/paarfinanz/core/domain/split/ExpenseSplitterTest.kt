package de.paarfinanz.core.domain.split

import de.paarfinanz.core.domain.model.Money
import de.paarfinanz.core.domain.model.ShareBps
import de.paarfinanz.core.domain.model.SplitMode
import de.paarfinanz.core.domain.model.UserId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExpenseSplitterTest {

    private val anna = UserId("anna")
    private val ben = UserId("ben")
    private val income = PartnerShares(anna, ShareBps(6_000), ben)
    private val fixed = PartnerShares(anna, ShareBps(7_000), ben)

    @Test
    fun payershareJeNachAufteilungsart() {
        assertEquals(ShareBps.HALF, ExpenseSplitter.payerShare(SplitMode.EQUAL, ben, income, fixed))
        assertEquals(ShareBps.FULL, ExpenseSplitter.payerShare(SplitMode.PERSONAL, ben, income, fixed))
        assertEquals(ShareBps(4_000), ExpenseSplitter.payerShare(SplitMode.INCOME, ben, income, fixed))
        assertEquals(ShareBps(3_000), ExpenseSplitter.payerShare(SplitMode.FIXED, ben, income, fixed))
    }

    @Test
    fun teileErgebenImmerExaktDenGesamtbetrag() {
        val amounts = listOf(1L, 3L, 99L, 1_001L, 12_345L, 99_999L)
        val shares = listOf(0, 1, 3_333, 5_000, 6_667, 9_999, 10_000)
        for (cents in amounts) for (bps in shares) {
            val result = ExpenseSplitter.split(Money(cents), ShareBps(bps))
            assertEquals(Money(cents), result.payerPart + result.partnerPart, "cents=$cents bps=$bps")
        }
    }

    @Test
    fun einkaufVon85EuroBei60Zu40() {
        val result = ExpenseSplitter.split(Money.ofEuros(85), ShareBps(6_000))
        assertEquals(Money.ofEuros(51), result.payerPart)
        assertEquals(Money.ofEuros(34), result.partnerPart)
    }

    @Test
    fun fremdePersonIstKeinTeilDerAufteilung() {
        assertFailsWith<IllegalArgumentException> {
            ExpenseSplitter.payerShare(SplitMode.INCOME, UserId("eve"), income, fixed)
        }
    }
}
