package de.paarfinanz.core.domain.model

import de.paarfinanz.core.domain.split.FairnessCalculator
import de.paarfinanz.core.domain.split.PartnerShares
import java.time.LocalDate

enum class MemberRole { OWNER, MEMBER }

data class HouseholdMember(
    val userId: UserId,
    val displayName: String,
    val role: MemberRole,
    /** Anteil bei der Aufteilung "Fester Anteil". */
    val fixedShare: ShareBps,
)

data class Household(
    val id: String,
    val name: String,
    val currency: String,
    val defaultSplitMode: SplitMode,
    val fairnessBasis: FairnessBasis,
    val jointAccountEnabled: Boolean,
    /** Besitzer zuerst, damit beide Geräte dieselbe Reihenfolge (und Farben) zeigen. */
    val members: List<HouseholdMember>,
    val incomes: List<MemberIncome>,
) {
    val isComplete: Boolean get() = members.size == 2

    fun member(userId: UserId): HouseholdMember? = members.firstOrNull { it.userId == userId }

    fun partnerOf(userId: UserId): HouseholdMember? = members.firstOrNull { it.userId != userId }

    /** Index 0 oder 1 – bestimmt die Partnerfarbe. */
    fun colorIndexOf(userId: UserId): Int = members.indexOfFirst { it.userId == userId }.coerceAtLeast(0)

    fun currentIncome(userId: UserId, date: LocalDate): MemberIncome? =
        FairnessCalculator.incomeAt(incomes, userId, date)

    /** Einkommensabhängige Anteile zum Stichtag; null, solange der Partner fehlt. */
    fun incomeShares(date: LocalDate): PartnerShares? {
        if (!isComplete) return null
        return FairnessCalculator.incomeShares(members[0].userId, members[1].userId, incomes, fairnessBasis, date)
    }

    /** Feste Anteile; null, solange der Partner fehlt. */
    fun fixedShares(): PartnerShares? {
        if (!isComplete) return null
        return PartnerShares(members[0].userId, members[0].fixedShare, members[1].userId)
    }
}
