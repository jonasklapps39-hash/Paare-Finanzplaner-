package de.paarfinanz.feature.household

import de.paarfinanz.core.data.household.HouseholdError
import de.paarfinanz.shared.resources.Res
import de.paarfinanz.shared.resources.error_already_member
import de.paarfinanz.shared.resources.error_household_full
import de.paarfinanz.shared.resources.error_invite_invalid
import de.paarfinanz.shared.resources.error_network
import de.paarfinanz.shared.resources.error_no_household
import de.paarfinanz.shared.resources.error_not_configured
import de.paarfinanz.shared.resources.error_not_member
import de.paarfinanz.shared.resources.error_unknown
import org.jetbrains.compose.resources.StringResource

internal fun HouseholdError.messageRes(): StringResource = when (this) {
    HouseholdError.INVITE_INVALID -> Res.string.error_invite_invalid
    HouseholdError.ALREADY_MEMBER -> Res.string.error_already_member
    HouseholdError.HOUSEHOLD_FULL -> Res.string.error_household_full
    HouseholdError.NOT_MEMBER -> Res.string.error_not_member
    HouseholdError.NO_HOUSEHOLD -> Res.string.error_no_household
    HouseholdError.NETWORK -> Res.string.error_network
    HouseholdError.NOT_CONFIGURED -> Res.string.error_not_configured
    HouseholdError.UNKNOWN -> Res.string.error_unknown
}

internal fun Throwable.householdMessageRes(): StringResource = HouseholdError.from(this).messageRes()
