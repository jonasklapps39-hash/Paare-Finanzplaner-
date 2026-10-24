package de.paarfinanz.core.ui.format

import de.paarfinanz.core.domain.model.Money
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Formatiert Beträge für die Anzeige, z. B. "1.234,56 €". */
object MoneyFormatter {

    fun format(money: Money, currencyCode: String = "EUR", locale: Locale = Locale.GERMANY): String {
        val format = NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(currencyCode)
        }
        return format.format(BigDecimal.valueOf(money.cents, 2))
    }
}
