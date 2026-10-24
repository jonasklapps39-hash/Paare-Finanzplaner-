package de.paarfinanz.core.data.household

import de.paarfinanz.core.domain.model.FairnessBasis
import de.paarfinanz.core.domain.model.Household
import de.paarfinanz.core.domain.model.HouseholdMember
import de.paarfinanz.core.domain.model.MemberIncome
import de.paarfinanz.core.domain.model.MemberRole
import de.paarfinanz.core.domain.model.Money
import de.paarfinanz.core.domain.model.ShareBps
import de.paarfinanz.core.domain.model.SplitMode
import de.paarfinanz.core.domain.model.UserId
import kotlinx.datetime.LocalDate

internal object HouseholdMapper {

    fun toDomain(household: HouseholdDto, members: List<MemberDto>, incomes: List<IncomeDto>): Household = Household(
        id = household.id,
        name = household.name,
        currency = household.currency.trim(),
        defaultSplitMode = enumOrDefault(household.defaultSplitMode, SplitMode.INCOME),
        fairnessBasis = enumOrDefault(household.fairnessBasis, FairnessBasis.NET),
        jointAccountEnabled = household.jointAccountEnabled,
        members = members
            .sortedWith(compareBy<MemberDto> { it.role != MemberRole.OWNER.name }.thenBy { it.joinedAt })
            .map { member ->
                HouseholdMember(
                    userId = UserId(member.userId),
                    displayName = member.profile?.displayName.orEmpty(),
                    role = enumOrDefault(member.role, MemberRole.MEMBER),
                    fixedShare = ShareBps(member.fixedShareBps.coerceIn(0, ShareBps.MAX)),
                )
            },
        incomes = incomes.map { income ->
            MemberIncome(
                userId = UserId(income.userId),
                netIncome = Money(income.netIncomeCents),
                privateFixedCosts = Money(income.privateFixedCostsCents),
                validFrom = LocalDate.parse(income.validFrom),
            )
        },
    )

    private inline fun <reified T : Enum<T>> enumOrDefault(value: String, default: T): T =
        enumValues<T>().firstOrNull { it.name == value } ?: default
}
