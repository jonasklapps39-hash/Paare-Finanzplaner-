package de.paarfinanz.feature.household

import androidx.annotation.StringRes
import de.paarfinanz.core.data.household.HouseholdError

@StringRes
internal fun HouseholdError.messageRes(): Int = when (this) {
    HouseholdError.INVITE_INVALID -> R.string.error_invite_invalid
    HouseholdError.ALREADY_MEMBER -> R.string.error_already_member
    HouseholdError.HOUSEHOLD_FULL -> R.string.error_household_full
    HouseholdError.NOT_MEMBER -> R.string.error_not_member
    HouseholdError.NO_HOUSEHOLD -> R.string.error_no_household
    HouseholdError.NETWORK -> R.string.error_network
    HouseholdError.NOT_CONFIGURED -> R.string.error_not_configured
    HouseholdError.UNKNOWN -> R.string.error_unknown
}

@StringRes
internal fun Throwable.householdMessageRes(): Int = HouseholdError.from(this).messageRes()
