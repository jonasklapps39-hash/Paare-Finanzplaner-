package de.paarfinanz.core.domain.split

import de.paarfinanz.core.domain.model.FairnessBasis
import de.paarfinanz.core.domain.model.MemberIncome
import de.paarfinanz.core.domain.model.Money
import de.paarfinanz.core.domain.model.ShareBps
import de.paarfinanz.core.domain.model.UserId
import java.time.LocalDate

/** Berechnet die einkommensabhängigen Anteile zweier Partner. */
object FairnessCalculator {

    /** Das zum Stichtag gültige Einkommen einer Person, falls vorhanden. */
    fun incomeAt(incomes: List<MemberIncome>, userId: UserId, date: LocalDate): MemberIncome? =
        incomes
            .filter { it.userId == userId && !it.validFrom.isAfter(date) }
            .maxByOrNull { it.validFrom }

    fun effectiveIncome(income: MemberIncome, basis: FairnessBasis): Money = when (basis) {
        FairnessBasis.NET -> income.netIncome
        FairnessBasis.DISPOSABLE -> maxOf(Money.ZERO, income.netIncome - income.privateFixedCosts)
    }

    /**
     * Anteile nach Einkommen zum Stichtag. Fehlt ein Einkommen oder ist die Summe 0,
     * wird auf 50/50 zurückgefallen und [PartnerShares.isFallback] gesetzt.
     */
    fun incomeShares(
        first: UserId,
        second: UserId,
        incomes: List<MemberIncome>,
        basis: FairnessBasis,
        date: LocalDate,
    ): PartnerShares {
        val firstIncome = incomeAt(incomes, first, date)?.let { effectiveIncome(it, basis) }
        val secondIncome = incomeAt(incomes, second, date)?.let { effectiveIncome(it, basis) }
        if (firstIncome == null || secondIncome == null) {
            return PartnerShares.equal(first, second).copy(isFallback = true)
        }
        val total = firstIncome + secondIncome
        if (total.isZero) {
            return PartnerShares.equal(first, second).copy(isFallback = true)
        }
        val scaled = Math.multiplyExact(firstIncome.cents, ShareBps.MAX.toLong())
        val firstBps = ((scaled + total.cents / 2) / total.cents).toInt()
        return PartnerShares(first, ShareBps(firstBps), second)
    }
}
