package de.paarfinanz.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MoneyTest {

    @Test
    fun `ofEuros rechnet in Cent um`() {
        assertEquals(Money(1_234), Money.ofEuros(12, 34))
        assertEquals(Money(-1_234), Money.ofEuros(-12, 34))
    }

    @Test
    fun `share rundet kaufmaennisch`() {
        // 0,01 € * 50 % = 0,5 Cent -> 1 Cent
        assertEquals(Money(1), Money(1).share(ShareBps.HALF))
        // 10,01 € * 50 % = 500,5 Cent -> 501 Cent
        assertEquals(Money(501), Money(1_001).share(ShareBps.HALF))
        // 100 € * 33,33 % = 3333 Cent
        assertEquals(Money(3_333), Money(10_000).share(ShareBps(3_333)))
    }

    @Test
    fun `share ist bei negativen Betraegen symmetrisch`() {
        assertEquals(Money(-501), Money(-1_001).share(ShareBps.HALF))
    }

    @Test
    fun `sum addiert alle Betraege`() {
        assertEquals(Money(600), listOf(Money(100), Money(200), Money(300)).sum())
        assertEquals(Money.ZERO, emptyList<Money>().sum())
    }

    @Test
    fun `ueberlauf wird erkannt statt still falsch zu rechnen`() {
        assertFailsWith<ArithmeticException> { Money(Long.MAX_VALUE) + Money(1) }
    }
}
