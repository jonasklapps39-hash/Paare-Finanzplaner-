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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.paarfinanz.app.ui.home.BudgetsScreen
import de.paarfinanz.app.ui.home.ExpensesScreen
import de.paarfinanz.app.ui.home.GoalsScreen
import de.paarfinanz.app.ui.home.OverviewScreen
import de.paarfinanz.app.ui.settings.SettingsRoute
import de.paarfinanz.feature.household.income.IncomeRoute
import de.paarfinanz.feature.household.invite.InviteRoute
import de.paarfinanz.shared.resources.Res
import de.paarfinanz.shared.resources.action_add_expense
import de.paarfinanz.shared.resources.action_back
import de.paarfinanz.shared.resources.action_settings
import de.paarfinanz.shared.resources.add_expense_coming_soon
import de.paarfinanz.shared.resources.settings_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(start: MainStart = MainStart.OVERVIEW, onStartHandled: () -> Unit = {}) {
    val navController = rememberNavController()
    // Über rememberUpdatedState, damit der NavHost-Graph nicht bei jeder Änderung neu entsteht.
    val currentStart by rememberUpdatedState(start)
    val currentOnStartHandled by rememberUpdatedState(onStartHandled)
    var startHandled by rememberSaveable { mutableStateOf(false) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentTab = TopLevelDestination.entries.firstOrNull { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }
    val isSettings = currentDestination?.route == SETTINGS_ROUTE
    // Einladen und Einkommen bringen eine eigene Kopfzeile mit.
    val isFullScreen = currentDestination?.route in setOf(INVITE_ROUTE, INCOME_ROUTE)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val comingSoon = stringResource(Res.string.add_expense_coming_soon)

    Scaffold(
        topBar = {
            if (!isFullScreen) TopAppBar(
                title = {
                    Text(
                        text = when {
                            isSettings -> stringResource(Res.string.settings_title)
                            currentTab != null -> stringResource(currentTab.labelRes)
                            else -> ""
                        },
                    )
                },
                navigationIcon = {
                    if (isSettings) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.action_back))
                        }
                    }
                },
                actions = {
                    if (!isSettings) {
                        IconButton(onClick = { navController.navigate(SETTINGS_ROUTE) { launchSingleTop = true } }) {
                            Icon(Icons.Outlined.Settings, contentDescription = stringResource(Res.string.action_settings))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            if (!isSettings && !isFullScreen) {
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
                    text = { Text(stringResource(Res.string.action_add_expense)) },
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
            composable(TopLevelDestination.OVERVIEW.route) {
                // Nach dem Onboarding: über der Übersicht direkt den nächsten Schritt öffnen.
                LaunchedEffect(Unit) {
                    if (startHandled) return@LaunchedEffect
                    startHandled = true
                    when (currentStart) {
                        MainStart.OVERVIEW -> Unit
                        MainStart.INVITE -> navController.navigate(INVITE_ROUTE)
                        MainStart.INCOME -> navController.navigate(INCOME_ROUTE)
                    }
                    currentOnStartHandled()
                }
                OverviewScreen(
                    onInvite = { navController.navigate(INVITE_ROUTE) },
                    onEditIncome = { navController.navigate(INCOME_ROUTE) },
                )
            }
            composable(TopLevelDestination.EXPENSES.route) { ExpensesScreen() }
            composable(TopLevelDestination.BUDGETS.route) { BudgetsScreen() }
            composable(TopLevelDestination.GOALS.route) { GoalsScreen() }
            composable(SETTINGS_ROUTE) {
                SettingsRoute(
                    onInvite = { navController.navigate(INVITE_ROUTE) },
                    onEditIncome = { navController.navigate(INCOME_ROUTE) },
                )
            }
            composable(INVITE_ROUTE) {
                InviteRoute(
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() },
                )
            }
            composable(INCOME_ROUTE) {
                IncomeRoute(
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}
