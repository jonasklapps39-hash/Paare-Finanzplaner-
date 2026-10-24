package de.paarfinanz.core.domain.model

/**
 * Liest Beträge in deutscher Schreibweise ein: "2.450,50", "2450,5", "12 €".
 * Ein Punkt gilt nur als Dezimaltrenner, wenn kein Komma vorkommt und danach
 * höchstens zwei Ziffern folgen ("12.5" = 12,50 €, "2.450" = 2.450 €).
 */
object MoneyParser {

    private val allowed = Regex("^[0-9.,]+$")

    fun parse(input: String): Money? {
        val cleaned = input.replace("€", "").replace(" ", "").replace(" ", "").trim()
        if (cleaned.isEmpty() || !allowed.matches(cleaned)) return null

        val (euroPart, centPart) = when {
            ',' in cleaned -> {
                if (cleaned.count { it == ',' } > 1) return null
                cleaned.substringBefore(',').replace(".", "") to cleaned.substringAfter(',')
            }
            Regex("\\.[0-9]{1,2}$").containsMatchIn(cleaned) && cleaned.count { it == '.' } == 1 ->
                cleaned.substringBefore('.') to cleaned.substringAfter('.')
            else -> cleaned.replace(".", "") to ""
        }
        if (centPart.length > 2 || '.' in centPart) return null
        if (euroPart.isEmpty() && centPart.isEmpty()) return null
        if (euroPart.length > 13) return null

        val euros = euroPart.ifEmpty { "0" }.toLong()
        val cents = centPart.padEnd(2, '0').ifEmpty { "00" }.toInt()
        return Money.ofEuros(euros, cents)
    }

    /** Für Eingabefelder: "2450,50" bzw. "2450" ohne Tausenderpunkte. */
    fun formatForInput(money: Money): String {
        val euros = money.cents / 100
        val cents = (money.cents % 100).let { if (it < 0) -it else it }
        return if (cents == 0L) "$euros" else "$euros,${cents.toString().padStart(2, '0')}"
    }
}
