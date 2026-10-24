package de.paarfinanz.core.ui.format

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

object Dates {

    private val monthNames = listOf(
        "Januar", "Februar", "März", "April", "Mai", "Juni",
        "Juli", "August", "September", "Oktober", "November", "Dezember",
    )

    fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    fun firstOfMonth(date: LocalDate = today()): LocalDate = LocalDate(date.year, date.monthNumber, 1)

    fun plusMonths(date: LocalDate, months: Int): LocalDate = date.plus(DatePeriod(months = months))

    /** "Oktober 2026" */
    fun monthYear(date: LocalDate): String = "${monthNames[date.monthNumber - 1]} ${date.year}"
}
