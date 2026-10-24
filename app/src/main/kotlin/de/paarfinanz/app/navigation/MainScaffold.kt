package de.paarfinanz.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.paarfinanz.app.R
import de.paarfinanz.app.ui.home.BudgetsScreen
import de.paarfinanz.app.ui.home.ExpensesScreen
import de.paarfinanz.app.ui.home.GoalsScreen
import de.paarfinanz.app.ui.home.OverviewScreen
import de.paarfinanz.app.ui.settings.SettingsRoute
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentTab = TopLevelDestination.entries.firstOrNull { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }
    val isSettings = currentDestination?.route == SETTINGS_ROUTE

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val comingSoon = stringResource(R.string.add_expense_coming_soon)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            isSettings -> stringResource(R.string.settings_title)
                            currentTab != null -> stringResource(currentTab.labelRes)
                            else -> ""
                        },
                    )
                },
                navigationIcon = {
                    if (isSettings) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                        }
                    }
                },
                actions = {
                    if (!isSettings) {
                        IconButton(onClick = { navController.navigate(SETTINGS_ROUTE) { launchSingleTop = true } }) {
                            Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.action_settings))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            if (!isSettings) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { tab ->
                        val selected = tab == currentTab
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(tab.labelRes)) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentTab?.showsAddButton == true) {
                ExtendedFloatingActionButton(
                    onClick = { scope.launch { snackbarHostState.showSnackbar(comingSoon) } },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.action_add_expense)) },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.OVERVIEW.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(TopLevelDestination.OVERVIEW.route) { OverviewScreen() }
            composable(TopLevelDestination.EXPENSES.route) { ExpensesScreen() }
            composable(TopLevelDestination.BUDGETS.route) { BudgetsScreen() }
            composable(TopLevelDestination.GOALS.route) { GoalsScreen() }
            composable(SETTINGS_ROUTE) { SettingsRoute() }
        }
    }
}
