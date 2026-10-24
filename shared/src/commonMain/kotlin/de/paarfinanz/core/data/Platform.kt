package de.paarfinanz.core.data

/** Kleine Plattform-Infos für Stellen, an denen sich Android und iPhone unterscheiden. */
expect object Platform {
    val isAndroid: Boolean
}
