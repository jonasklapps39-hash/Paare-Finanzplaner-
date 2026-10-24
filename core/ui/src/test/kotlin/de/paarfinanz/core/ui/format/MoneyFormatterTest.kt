package de.paarfinanz.core.ui.format

import de.paarfinanz.core.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {

    // NumberFormat nutzt je nach JDK ein geschütztes Leerzeichen vor dem €-Zeichen.
    private fun normalize(text: String) = text.replace(' ', ' ').replace(' ', ' ')

    @Test
    fun formatiertEuroDeutsch() {
        assertEquals("1.234,56 €", normalize(MoneyFormatter.format(Money(123_456))))
    }

    @Test
    fun formatiertNegativeBetraege() {
        assertEquals("-12,30 €", normalize(MoneyFormatter.format(Money(-1_230))))
    }
}
