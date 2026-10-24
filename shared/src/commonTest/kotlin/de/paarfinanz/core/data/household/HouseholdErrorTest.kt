package de.paarfinanz.core.data.household

import kotlin.test.Test
import kotlin.test.assertEquals

class HouseholdErrorTest {

    @Test
    fun erkenntHinweiseDerDatenbank() {
        assertEquals(HouseholdError.INVITE_INVALID, HouseholdError.from(Exception("Einladungscode ungültig oder abgelaufen invite_invalid")))
        assertEquals(HouseholdError.ALREADY_MEMBER, HouseholdError.from(Exception("already_member")))
        assertEquals(HouseholdError.HOUSEHOLD_FULL, HouseholdError.from(Exception("household_full")))
        assertEquals(HouseholdError.NOT_MEMBER, HouseholdError.from(Exception("not_member")))
    }

    @Test
    fun eigeneFehlerBleibenErhalten() {
        assertEquals(HouseholdError.NO_HOUSEHOLD, HouseholdError.from(HouseholdException(HouseholdError.NO_HOUSEHOLD)))
    }

    @Test
    fun unbekannteFehler() {
        assertEquals(HouseholdError.UNKNOWN, HouseholdError.from(Exception("Boom")))
    }
}
