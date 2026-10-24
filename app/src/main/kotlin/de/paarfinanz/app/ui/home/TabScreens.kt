package de.paarfinanz.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.paarfinanz.app.R
import de.paarfinanz.core.ui.components.EmptyState
import de.paarfinanz.feature.household.overview.HouseholdOverviewCard

// Ausgaben, Budgets und Ziele folgen in den nächsten Phasen; bis dahin freundliche Leerzustände.

@Composable
fun OverviewScreen(onInvite: () -> Unit, onEditIncome: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HouseholdOverviewCard(onInvite = onInvite, onEditIncome = onEditIncome)
        EmptyState(
            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
            title = stringResource(R.string.expenses_empty_title),
            message = stringResource(R.string.expenses_empty_message),
        )
    }
}

@Composable
fun ExpensesScreen() = TabEmptyState(
    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
    titleRes = R.string.expenses_empty_title,
    messageRes = R.string.expenses_empty_message,
)

@Composable
fun BudgetsScreen() = TabEmptyState(
    icon = Icons.Outlined.PieChart,
    titleRes = R.string.budgets_empty_title,
    messageRes = R.string.budgets_empty_message,
)

@Composable
fun GoalsScreen() = TabEmptyState(
    icon = Icons.Outlined.Flag,
    titleRes = R.string.goals_empty_title,
    messageRes = R.string.goals_empty_message,
)

@Composable
private fun TabEmptyState(
    icon: ImageVector,
    titleRes: Int,
    messageRes: Int,
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        EmptyState(icon = icon, title = stringResource(titleRes), message = stringResource(messageRes))
    }
}
