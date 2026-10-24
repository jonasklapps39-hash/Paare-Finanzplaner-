package de.paarfinanz.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MoneyParserTest {

    @Test
    fun deutscheSchreibweise() {
        assertEquals(Money(245_050), MoneyParser.parse("2.450,50"))
        assertEquals(Money(245_050), MoneyParser.parse("2450,50"))
        assertEquals(Money(245_050), MoneyParser.parse("2450,5"))
        assertEquals(Money(245_000), MoneyParser.parse("2450"))
        assertEquals(Money(245_000), MoneyParser.parse("2.450"))
        assertEquals(Money(1_234_567_800), MoneyParser.parse("12.345.678"))
        assertEquals(Money(50), MoneyParser.parse(",5"))
    }

    @Test
    fun punktAlsDezimaltrennerWennEindeutig() {
        assertEquals(Money(1_250), MoneyParser.parse("12.5"))
        assertEquals(Money(1_250), MoneyParser.parse("12.50"))
    }

    @Test
    fun waehrungszeichenUndLeerzeichenWerdenIgnoriert() {
        assertEquals(Money(1_200), MoneyParser.parse(" 12 € "))
        assertEquals(Money(245_050), MoneyParser.parse("2.450,50 €"))
    }

    @Test
    fun ungueltigeEingaben() {
        assertNull(MoneyParser.parse(""))
        assertNull(MoneyParser.parse("abc"))
        assertNull(MoneyParser.parse("1,234,5"))
        assertNull(MoneyParser.parse("12,345"))
        assertNull(MoneyParser.parse("-5"))
        assertNull(MoneyParser.parse(","))
    }

    @Test
    fun formatFuerEingabefelder() {
        assertEquals("2450,50", MoneyParser.formatForInput(Money(245_050)))
        assertEquals("2450", MoneyParser.formatForInput(Money(245_000)))
        assertEquals("0,05", MoneyParser.formatForInput(Money(5)))
    }
}
