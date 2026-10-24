package de.paarfinanz.core.ui.format

import de.paarfinanz.core.domain.model.Money

/** Formatiert Beträge in deutscher Schreibweise, z. B. "1.234,56 €" – identisch auf Android und iPhone. */
object MoneyFormatter {

    fun format(money: Money, currencyCode: String = "EUR"): String {
        val negative = money.cents < 0
        // Über ULong rechnen, damit auch Long.MIN_VALUE sauber formatiert wird.
        val absolute = if (negative) (-(money.cents + 1)).toULong() + 1u else money.cents.toULong()
        val euros = (absolute / 100u).toString()
        val cents = (absolute % 100u).toString().padStart(2, '0')
        val groupedEuros = euros.reversed().chunked(3).joinToString(".").reversed()
        val sign = if (negative) "-" else ""
        return "$sign$groupedEuros,$cents ${symbolFor(currencyCode)}"
    }

    private fun symbolFor(currencyCode: String): String = when (currencyCode.uppercase()) {
        "EUR" -> "€"
        "CHF" -> "CHF"
        "USD" -> "$"
        "GBP" -> "£"
        else -> currencyCode.uppercase()
    }
}
