package de.paarfinanz.core.domain.model

import kotlinx.datetime.LocalDate

/**
 * Einkommen einer Person ab einem Stichtag. Einkommen werden historisiert,
 * damit vergangene Monate mit dem damaligen Verhältnis berechnet bleiben.
 */
data class MemberIncome(
    val userId: UserId,
    val netIncome: Money,
    val privateFixedCosts: Money = Money.ZERO,
    val validFrom: LocalDate,
) {
    init {
        require(!netIncome.isNegative) { "Nettoeinkommen darf nicht negativ sein" }
        require(!privateFixedCosts.isNegative) { "Private Fixkosten dürfen nicht negativ sein" }
    }
}
