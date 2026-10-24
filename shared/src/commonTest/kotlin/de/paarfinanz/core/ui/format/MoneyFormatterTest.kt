package de.paarfinanz.core.ui.format

import de.paarfinanz.core.domain.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatterTest {

    // Zwischen Betrag und Währung steht ein geschütztes Leerzeichen, damit "€" nie allein umbricht.
    private val nbsp = ' '

    @Test
    fun formatiertEuroDeutsch() {
        assertEquals("1.234,56${nbsp}€", MoneyFormatter.format(Money(123_456)))
        assertEquals("0,05${nbsp}€", MoneyFormatter.format(Money(5)))
        assertEquals("1.000.000,00${nbsp}€", MoneyFormatter.format(Money(100_000_000)))
    }

    @Test
    fun formatiertNegativeBetraege() {
        assertEquals("-12,30${nbsp}€", MoneyFormatter.format(Money(-1_230)))
    }

    @Test
    fun extremwerteBrechenNicht() {
        assertEquals("-92.233.720.368.547.758,08${nbsp}€", MoneyFormatter.format(Money(Long.MIN_VALUE)))
    }

    @Test
    fun andereWaehrungen() {
        assertEquals("10,00${nbsp}CHF", MoneyFormatter.format(Money(1_000), "chf"))
    }
}
