package de.paarfinanz.core.domain.model

import kotlin.jvm.JvmInline

/**
 * Geldbetrag in Cent. Beträge werden nie als Double gespeichert,
 * damit beim Aufteilen keine Rundungsfehler entstehen.
 */
@JvmInline
value class Money(val cents: Long) : Comparable<Money> {

    operator fun plus(other: Money): Money = Money(ExactMath.add(cents, other.cents))

    operator fun minus(other: Money): Money = Money(ExactMath.subtract(cents, other.cents))

    operator fun unaryMinus(): Money = Money(ExactMath.negate(cents))

    override fun compareTo(other: Money): Int = cents.compareTo(other.cents)

    val isZero: Boolean get() = cents == 0L

    val isNegative: Boolean get() = cents < 0L

    val absolute: Money get() = if (isNegative) -this else this

    /**
     * Anteil dieses Betrags, kaufmännisch auf ganze Cent gerundet
     * (0,5 Cent wird vom Nullpunkt weg gerundet).
     */
    fun share(share: ShareBps): Money {
        val scaled = ExactMath.multiply(cents, share.value.toLong())
        val half = ShareBps.MAX / 2L
        val rounded = if (scaled >= 0) (scaled + half) / ShareBps.MAX else -((-scaled + half) / ShareBps.MAX)
        return Money(rounded)
    }

    companion object {
        val ZERO = Money(0)

        fun ofEuros(euros: Long, cents: Int = 0): Money {
            require(cents in 0..99) { "Cent-Anteil muss zwischen 0 und 99 liegen" }
            val sign = if (euros < 0) -1 else 1
            return Money(ExactMath.add(ExactMath.multiply(euros, 100L), sign * cents.toLong()))
        }
    }
}

fun Iterable<Money>.sum(): Money = fold(Money.ZERO) { acc, money -> acc + money }
