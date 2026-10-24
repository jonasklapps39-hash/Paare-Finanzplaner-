package de.paarfinanz.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ShareBpsTest {

    @Test
    fun `complement ergibt zusammen 100 Prozent`() {
        val share = ShareBps(6_234)
        assertEquals(ShareBps.MAX, share.value + share.complement.value)
    }

    @Test
    fun `ofPercent rechnet in Basispunkte um`() {
        assertEquals(ShareBps(6_000), ShareBps.ofPercent(60))
    }

    @Test
    fun `werte ausserhalb von 0 bis 100 Prozent sind ungueltig`() {
        assertFailsWith<IllegalArgumentException> { ShareBps(-1) }
        assertFailsWith<IllegalArgumentException> { ShareBps(10_001) }
    }
}
