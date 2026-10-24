package de.paarfinanz.core.domain.model

/** Ganzzahl-Arithmetik, die bei Überlauf einen Fehler wirft statt still falsch zu rechnen. */
internal object ExactMath {

    fun add(a: Long, b: Long): Long {
        val result = a + b
        if ((a xor result) and (b xor result) < 0) throw ArithmeticException("long overflow")
        return result
    }

    fun subtract(a: Long, b: Long): Long {
        val result = a - b
        if ((a xor b) and (a xor result) < 0) throw ArithmeticException("long overflow")
        return result
    }

    fun negate(a: Long): Long {
        if (a == Long.MIN_VALUE) throw ArithmeticException("long overflow")
        return -a
    }

    fun multiply(a: Long, b: Long): Long {
        if (a == 0L || b == 0L) return 0L
        val result = a * b
        if (result / b != a || (a == -1L && b == Long.MIN_VALUE) || (b == -1L && a == Long.MIN_VALUE)) {
            throw ArithmeticException("long overflow")
        }
        return result
    }
}
