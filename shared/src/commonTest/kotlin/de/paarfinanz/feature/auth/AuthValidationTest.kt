package de.paarfinanz.feature.auth

import de.paarfinanz.shared.resources.Res
import de.paarfinanz.shared.resources.auth_error_missing_name
import de.paarfinanz.shared.resources.auth_error_short_password
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthValidationTest {

    @Test
    fun emailFormat() {
        assertTrue(AuthValidation.isValidEmail("anna@example.com"))
        assertTrue(AuthValidation.isValidEmail("  anna@example.com "))
        assertFalse(AuthValidation.isValidEmail("anna@"))
        assertFalse(AuthValidation.isValidEmail("anna example.com"))
    }

    @Test
    fun registrierungBrauchtNamenUndLangesPasswort() {
        val base = AuthUiState(mode = AuthMode.SIGN_UP, displayName = "Anna", email = "anna@example.com", password = "geheim123")
        assertNull(AuthValidation.validate(base))
        assertEquals(Res.string.auth_error_missing_name, AuthValidation.validate(base.copy(displayName = " ")))
        assertEquals(Res.string.auth_error_short_password, AuthValidation.validate(base.copy(password = "kurz")))
    }

    @Test
    fun anmeldungAkzeptiertVorhandenePasswoerter() {
        val state = AuthUiState(mode = AuthMode.SIGN_IN, email = "anna@example.com", password = "alt")
        assertNull(AuthValidation.validate(state))
    }
}
