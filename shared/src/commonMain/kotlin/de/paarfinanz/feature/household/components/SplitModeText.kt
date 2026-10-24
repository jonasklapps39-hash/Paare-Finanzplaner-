package de.paarfinanz.feature.household.components

import de.paarfinanz.core.domain.model.SplitMode
import de.paarfinanz.shared.resources.Res
import de.paarfinanz.shared.resources.split_equal_description
import de.paarfinanz.shared.resources.split_equal_title
import de.paarfinanz.shared.resources.split_fixed_description
import de.paarfinanz.shared.resources.split_fixed_title
import de.paarfinanz.shared.resources.split_income_description
import de.paarfinanz.shared.resources.split_income_title
import org.jetbrains.compose.resources.StringResource

/** Die drei wählbaren Standard-Aufteilungen (PERSONAL gilt nur für einzelne Ausgaben). */
internal val selectableSplitModes = listOf(SplitMode.INCOME, SplitMode.EQUAL, SplitMode.FIXED)

internal val SplitMode.titleRes: StringResource
    get() = when (this) {
        SplitMode.INCOME -> Res.string.split_income_title
        SplitMode.EQUAL -> Res.string.split_equal_title
        SplitMode.FIXED -> Res.string.split_fixed_title
        SplitMode.PERSONAL -> Res.string.split_equal_title
    }

internal val SplitMode.descriptionRes: StringResource
    get() = when (this) {
        SplitMode.INCOME -> Res.string.split_income_description
        SplitMode.EQUAL -> Res.string.split_equal_description
        SplitMode.FIXED -> Res.string.split_fixed_description
        SplitMode.PERSONAL -> Res.string.split_equal_description
    }
