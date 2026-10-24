package de.paarfinanz.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MoneyTest {

    @Test
    fun ofeurosRechnetInCentUm() {
        assertEquals(Money(1_234), Money.ofEuros(12, 34))
        assertEquals(Money(-1_234), Money.ofEuros(-12, 34))
    }

    @Test
    fun shareRundetKaufmaennisch() {
        // 0,01 € * 50 % = 0,5 Cent -> 1 Cent
        assertEquals(Money(1), Money(1).share(ShareBps.HALF))
        // 10,01 € * 50 % = 500,5 Cent -> 501 Cent
        assertEquals(Money(501), Money(1_001).share(ShareBps.HALF))
        // 100 € * 33,33 % = 3333 Cent
        assertEquals(Money(3_333), Money(10_000).share(ShareBps(3_333)))
    }

    @Test
    fun shareIstBeiNegativenBetraegenSymmetrisch() {
        assertEquals(Money(-501), Money(-1_001).share(ShareBps.HALF))
    }

    @Test
    fun sumAddiertAlleBetraege() {
        assertEquals(Money(600), listOf(Money(100), Money(200), Money(300)).sum())
        assertEquals(Money.ZERO, emptyList<Money>().sum())
    }

    @Test
    fun ueberlaufWirdErkanntStattStillFalschZuRechnen() {
        assertFailsWith<ArithmeticException> { Money(Long.MAX_VALUE) + Money(1) }
    }

    @Test
    fun ueberlaufBeiMultiplikationUndNegationWirdErkannt() {
        assertFailsWith<ArithmeticException> { Money(Long.MAX_VALUE).share(ShareBps.FULL) }
        assertFailsWith<ArithmeticException> { -Money(Long.MIN_VALUE) }
        assertFailsWith<ArithmeticException> { Money(Long.MIN_VALUE) - Money(1) }
    }
}
