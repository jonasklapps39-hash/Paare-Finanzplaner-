package de.paarfinanz.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ShareBpsTest {

    @Test
    fun complementErgibtZusammen100Prozent() {
        val share = ShareBps(6_234)
        assertEquals(ShareBps.MAX, share.value + share.complement.value)
    }

    @Test
    fun ofpercentRechnetInBasispunkteUm() {
        assertEquals(ShareBps(6_000), ShareBps.ofPercent(60))
    }

    @Test
    fun werteAusserhalbVon0Bis100ProzentSindUngueltig() {
        assertFailsWith<IllegalArgumentException> { ShareBps(-1) }
        assertFailsWith<IllegalArgumentException> { ShareBps(10_001) }
    }
}
