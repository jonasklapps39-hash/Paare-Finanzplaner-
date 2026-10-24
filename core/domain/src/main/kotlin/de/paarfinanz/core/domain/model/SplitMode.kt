package de.paarfinanz.core.domain.model

/** Wie eine gemeinsame Ausgabe zwischen den beiden Partnern aufgeteilt wird. */
enum class SplitMode {
    /** Jeder trägt die Hälfte. */
    EQUAL,

    /** Anteilig nach Einkommen (siehe [FairnessBasis]). */
    INCOME,

    /** Fester, im Haushalt hinterlegter Prozentsatz, z. B. 60/40. */
    FIXED,

    /** Gehört nur der zahlenden Person. */
    PERSONAL,
}

/** Grundlage für die einkommensabhängige Aufteilung. */
enum class FairnessBasis {
    /** Nettogehalt. */
    NET,

    /** Nettogehalt abzüglich privater Fixkosten (Kredit, Unterhalt, ...). */
    DISPOSABLE,
}
