package de.paarfinanz.core.domain.split

import de.paarfinanz.core.domain.model.ShareBps
import de.paarfinanz.core.domain.model.UserId

/** Aufteilung zwischen genau zwei Partnern; die Anteile ergeben immer 100 %. */
data class PartnerShares(
    val first: UserId,
    val firstShare: ShareBps,
    val second: UserId,
    /** true, wenn mangels Einkommensdaten auf 50/50 zurückgefallen wurde. */
    val isFallback: Boolean = false,
) {
    init {
        require(first != second) { "Die Partner müssen unterschiedlich sein" }
    }

    val secondShare: ShareBps get() = firstShare.complement

    fun shareOf(userId: UserId): ShareBps = when (userId) {
        first -> firstShare
        second -> secondShare
        else -> throw IllegalArgumentException("${userId.value} gehört nicht zu diesem Haushalt")
    }

    companion object {
        fun equal(first: UserId, second: UserId) = PartnerShares(first, ShareBps.HALF, second)
    }
}
