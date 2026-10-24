package de.paarfinanz.core.data.household

import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException

/** Fachliche Fehler rund um den Haushalt. Die Datenbank liefert dafür Hinweise (HINT). */
enum class HouseholdError {
    INVITE_INVALID,
    ALREADY_MEMBER,
    HOUSEHOLD_FULL,
    NOT_MEMBER,
    NO_HOUSEHOLD,
    NETWORK,
    NOT_CONFIGURED,
    UNKNOWN;

    companion object {
        fun from(error: Throwable): HouseholdError {
            if (error is HouseholdException) return error.error
            if (error is HttpRequestException) return NETWORK
            val text = buildString {
                append(error.message.orEmpty())
                if (error is RestException) {
                    append(' ').append(error.error)
                    append(' ').append(error.description.orEmpty())
                }
            }.lowercase()
            return when {
                "invite_invalid" in text || "ungültig oder abgelaufen" in text -> INVITE_INVALID
                "already_member" in text || "bereits zu einem haushalt" in text -> ALREADY_MEMBER
                "household_full" in text || "höchstens zwei" in text || "bereits vollständig" in text -> HOUSEHOLD_FULL
                "not_member" in text || "kein mitglied" in text -> NOT_MEMBER
                else -> UNKNOWN
            }
        }
    }
}

class HouseholdException(val error: HouseholdError, cause: Throwable? = null) : Exception(error.name, cause)
