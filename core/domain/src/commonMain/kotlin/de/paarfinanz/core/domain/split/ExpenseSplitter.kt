package de.paarfinanz.core.domain.split

import de.paarfinanz.core.domain.model.Money
import de.paarfinanz.core.domain.model.ShareBps
import de.paarfinanz.core.domain.model.SplitMode
import de.paarfinanz.core.domain.model.UserId

/** Ergebnis einer Aufteilung: was trägt die zahlende Person, was der Partner. */
data class SplitResult(val payerPart: Money, val partnerPart: Money)

object ExpenseSplitter {

    /** Anteil, den die zahlende Person bei einer Ausgabe selbst trägt. */
    fun payerShare(
        mode: SplitMode,
        payer: UserId,
        incomeShares: PartnerShares,
        fixedShares: PartnerShares,
    ): ShareBps = when (mode) {
        SplitMode.EQUAL -> ShareBps.HALF
        SplitMode.PERSONAL -> ShareBps.FULL
        SplitMode.INCOME -> incomeShares.shareOf(payer)
        SplitMode.FIXED -> fixedShares.shareOf(payer)
    }

    /**
     * Teilt einen Betrag auf. Der Rundungsrest landet beim Partner, sodass
     * beide Teile in Summe immer exakt den Ausgangsbetrag ergeben.
     */
    fun split(amount: Money, payerShare: ShareBps): SplitResult {
        val payerPart = amount.share(payerShare)
        return SplitResult(payerPart = payerPart, partnerPart = amount - payerPart)
    }
}
