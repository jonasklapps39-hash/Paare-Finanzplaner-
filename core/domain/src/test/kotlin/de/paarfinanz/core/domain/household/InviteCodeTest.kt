package de.paarfinanz.core.domain.household

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InviteCodeTest {

    @Test
    fun `eingaben werden tolerant gelesen`() {
        val expected = InviteCode.parse("AB12CD34")
        assertEquals("AB12CD34", expected?.value)
        assertEquals(expected, InviteCode.parse(" ab12-cd34 "))
        assertEquals(expected, InviteCode.parse("AB12 CD34"))
        assertEquals(expected, InviteCode.parse("paarfinanz://invite/AB12CD34"))
        assertEquals(expected, InviteCode.parse("PAARFINANZ://INVITE/ab12cd34"))
    }

    @Test
    fun `falsche laengen sind ungueltig`() {
        assertNull(InviteCode.parse("AB12CD3"))
        assertNull(InviteCode.parse("AB12CD345"))
        assertNull(InviteCode.parse(""))
        assertNull(InviteCode.parse("https://example.com/AB12CD34"))
    }

    @Test
    fun `anzeige und qr inhalt`() {
        val code = InviteCode.parse("ab12cd34")!!
        assertEquals("AB12-CD34", code.formatted)
        assertEquals("paarfinanz://invite/AB12CD34", code.deepLink)
    }
}
