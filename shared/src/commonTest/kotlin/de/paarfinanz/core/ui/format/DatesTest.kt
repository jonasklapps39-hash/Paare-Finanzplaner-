package de.paarfinanz.core.ui.format

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DatesTest {

    @Test
    fun monatUndJahrAufDeutsch() {
        assertEquals("März 2026", Dates.monthYear(LocalDate(2026, 3, 15)))
    }

    @Test
    fun monateVorUndZurueck() {
        val jan = LocalDate(2026, 1, 1)
        assertEquals(LocalDate(2025, 12, 1), Dates.plusMonths(jan, -1))
        assertEquals(LocalDate(2026, 2, 1), Dates.firstOfMonth(Dates.plusMonths(LocalDate(2026, 1, 31), 1)))
    }
}
