package de.paarfinanz.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.ui.graphics.vector.ImageVector
import de.paarfinanz.app.R

/** Die vier Haupt-Tabs – bewusst nicht mehr, damit die App übersichtlich bleibt. */
enum class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val showsAddButton: Boolean,
) {
    OVERVIEW("overview", R.string.tab_overview, Icons.Filled.Home, Icons.Outlined.Home, showsAddButton = true),
    EXPENSES("expenses", R.string.tab_expenses, Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong, showsAddButton = true),
    BUDGETS("budgets", R.string.tab_budgets, Icons.Filled.PieChart, Icons.Outlined.PieChart, showsAddButton = false),
    GOALS("goals", R.string.tab_goals, Icons.Filled.Flag, Icons.Outlined.Flag, showsAddButton = false),
}

const val SETTINGS_ROUTE = "settings"
const val INVITE_ROUTE = "invite"
const val INCOME_ROUTE = "income"

/** Womit die Hauptansicht startet – nach dem Onboarding direkt mit dem nächsten Schritt. */
enum class MainStart { OVERVIEW, INVITE, INCOME }
