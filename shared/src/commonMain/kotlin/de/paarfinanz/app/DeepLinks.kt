package de.paarfinanz.app

import de.paarfinanz.core.domain.household.InviteCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Merkt sich Einladungen aus Links (paarfinanz://invite/CODE), bis die Person
 * einem Haushalt beigetreten ist. Android und iPhone liefern Links hier ab.
 */
class DeepLinks {

    private val _pendingInviteCode = MutableStateFlow<String?>(null)
    val pendingInviteCode: StateFlow<String?> = _pendingInviteCode.asStateFlow()

    /** @return true, wenn der Link eine Einladung war. */
    fun handle(url: String): Boolean {
        if (!url.startsWith(InviteCode.DEEPLINK_PREFIX, ignoreCase = true)) return false
        _pendingInviteCode.value = InviteCode.parse(url)?.value
        return true
    }

    fun consumeInviteCode() {
        _pendingInviteCode.value = null
    }
}
