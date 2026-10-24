package de.paarfinanz.core.data.household

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Datenbankzeilen, wie Supabase sie liefert. Enums kommen als Strings (z. B. "INCOME").

@Serializable
internal data class MembershipDto(
    @SerialName("household_id") val householdId: String,
)

@Serializable
internal data class HouseholdDto(
    val id: String,
    val name: String,
    val currency: String,
    @SerialName("default_split_mode") val defaultSplitMode: String,
    @SerialName("fairness_basis") val fairnessBasis: String,
    @SerialName("joint_account_enabled") val jointAccountEnabled: Boolean,
)

@Serializable
internal data class ProfileNameDto(
    @SerialName("display_name") val displayName: String = "",
)

@Serializable
internal data class MemberDto(
    @SerialName("user_id") val userId: String,
    val role: String,
    @SerialName("fixed_share_bps") val fixedShareBps: Int,
    @SerialName("joined_at") val joinedAt: String,
    val profile: ProfileNameDto? = null,
)

@Serializable
internal data class IncomeDto(
    @SerialName("user_id") val userId: String,
    @SerialName("net_income_cents") val netIncomeCents: Long,
    @SerialName("private_fixed_costs_cents") val privateFixedCostsCents: Long,
    @SerialName("valid_from") val validFrom: String,
)

@Serializable
internal data class IncomeUpsertDto(
    @SerialName("household_id") val householdId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("net_income_cents") val netIncomeCents: Long,
    @SerialName("private_fixed_costs_cents") val privateFixedCostsCents: Long,
    @SerialName("valid_from") val validFrom: String,
)

@Serializable
internal data class HouseholdSettingsDto(
    val name: String,
    @SerialName("default_split_mode") val defaultSplitMode: String,
    @SerialName("fairness_basis") val fairnessBasis: String,
    @SerialName("joint_account_enabled") val jointAccountEnabled: Boolean,
)

@Serializable
internal data class FixedShareDto(
    @SerialName("fixed_share_bps") val fixedShareBps: Int,
)

@Serializable
internal data class InvitePreviewDto(
    @SerialName("household_name") val householdName: String,
    @SerialName("inviter_name") val inviterName: String,
)
