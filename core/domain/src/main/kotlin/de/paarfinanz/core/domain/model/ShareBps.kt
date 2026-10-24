package de.paarfinanz.core.domain.model

/**
 * Anteil in Basispunkten: 10.000 = 100 %, 5.000 = 50 %.
 * Ganzzahlig, damit zwei Anteile immer exakt 100 % ergeben.
 */
@JvmInline
value class ShareBps(val value: Int) : Comparable<ShareBps> {

    init {
        require(value in 0..MAX) { "Anteil muss zwischen 0 und $MAX Basispunkten liegen, war $value" }
    }

    /** Der Anteil der anderen Person. */
    val complement: ShareBps get() = ShareBps(MAX - value)

    val percent: Double get() = value / 100.0

    override fun compareTo(other: ShareBps): Int = value.compareTo(other.value)

    companion object {
        const val MAX = 10_000

        val NONE = ShareBps(0)
        val HALF = ShareBps(MAX / 2)
        val FULL = ShareBps(MAX)

        fun ofPercent(percent: Int): ShareBps = ShareBps(percent * 100)
    }
}
