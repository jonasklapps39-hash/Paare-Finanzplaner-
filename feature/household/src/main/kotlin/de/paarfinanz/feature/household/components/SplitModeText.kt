package de.paarfinanz.feature.household.components

import de.paarfinanz.core.domain.model.SplitMode
import de.paarfinanz.feature.household.R

/** Die drei wählbaren Standard-Aufteilungen (PERSONAL gilt nur für einzelne Ausgaben). */
internal val selectableSplitModes = listOf(SplitMode.INCOME, SplitMode.EQUAL, SplitMode.FIXED)

internal val SplitMode.titleRes: Int
    get() = when (this) {
        SplitMode.INCOME -> R.string.split_income_title
        SplitMode.EQUAL -> R.string.split_equal_title
        SplitMode.FIXED -> R.string.split_fixed_title
        SplitMode.PERSONAL -> R.string.split_equal_title
    }

internal val SplitMode.descriptionRes: Int
    get() = when (this) {
        SplitMode.INCOME -> R.string.split_income_description
        SplitMode.EQUAL -> R.string.split_equal_description
        SplitMode.FIXED -> R.string.split_fixed_description
        SplitMode.PERSONAL -> R.string.split_equal_description
    }
