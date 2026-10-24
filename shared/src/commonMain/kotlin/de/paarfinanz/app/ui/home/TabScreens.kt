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
import androidx.compose.ui.unit.dp
import de.paarfinanz.core.ui.components.EmptyState
import de.paarfinanz.feature.household.overview.HouseholdOverviewCard
import de.paarfinanz.shared.resources.Res
import de.paarfinanz.shared.resources.budgets_empty_message
import de.paarfinanz.shared.resources.budgets_empty_title
import de.paarfinanz.shared.resources.expenses_empty_message
import de.paarfinanz.shared.resources.expenses_empty_title
import de.paarfinanz.shared.resources.goals_empty_message
import de.paarfinanz.shared.resources.goals_empty_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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
            title = stringResource(Res.string.expenses_empty_title),
            message = stringResource(Res.string.expenses_empty_message),
        )
    }
}

@Composable
fun ExpensesScreen() = TabEmptyState(
    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
    titleRes = Res.string.expenses_empty_title,
    messageRes = Res.string.expenses_empty_message,
)

@Composable
fun BudgetsScreen() = TabEmptyState(
    icon = Icons.Outlined.PieChart,
    titleRes = Res.string.budgets_empty_title,
    messageRes = Res.string.budgets_empty_message,
)

@Composable
fun GoalsScreen() = TabEmptyState(
    icon = Icons.Outlined.Flag,
    titleRes = Res.string.goals_empty_title,
    messageRes = Res.string.goals_empty_message,
)

@Composable
private fun TabEmptyState(
    icon: ImageVector,
    titleRes: StringResource,
    messageRes: StringResource,
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        EmptyState(icon = icon, title = stringResource(titleRes), message = stringResource(messageRes))
    }
}
