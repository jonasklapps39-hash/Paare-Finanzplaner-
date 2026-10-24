package de.paarfinanz.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.paarfinanz.app.R
import de.paarfinanz.core.domain.model.Money
import de.paarfinanz.core.ui.components.EmptyState
import de.paarfinanz.core.ui.format.MoneyFormatter
import de.paarfinanz.core.ui.theme.AmountTextStyle

// Platzhalter-Screens der Phase 1. Inhalte folgen mit Haushalt (Phase 2) und Ausgaben (Phase 3).

@Composable
fun OverviewScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.overview_greeting),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = MoneyFormatter.format(Money.ZERO),
                    style = AmountTextStyle,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        EmptyState(
            icon = Icons.Outlined.Group,
            title = stringResource(R.string.overview_empty_title),
            message = stringResource(R.string.overview_empty_message),
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
