package de.paarfinanz.core.data.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthErrorTest {

    @Test
    fun erkenntFalscheZugangsdaten() {
        assertEquals(AuthError.INVALID_CREDENTIALS, AuthError.from(Exception("Invalid login credentials")))
    }

    @Test
    fun erkenntUnbestaetigteEmail() {
        assertEquals(AuthError.EMAIL_NOT_CONFIRMED, AuthError.from(Exception("Email not confirmed")))
    }

    @Test
    fun erkenntVorhandenesKonto() {
        assertEquals(AuthError.USER_ALREADY_EXISTS, AuthError.from(Exception("User already registered")))
    }

    @Test
    fun unbekannteFehler() {
        assertEquals(AuthError.UNKNOWN, AuthError.from(Exception("Boom")))
    }
}
