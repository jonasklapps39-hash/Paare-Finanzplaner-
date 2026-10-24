package de.paarfinanz.feature.household

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.paarfinanz.feature.household.create.CreateHouseholdRoute
import de.paarfinanz.feature.household.join.JoinHouseholdRoute
import de.paarfinanz.feature.household.join.JoinHouseholdViewModel
import de.paarfinanz.feature.household.welcome.WelcomeScreen

private const val WELCOME = "welcome"
private const val CREATE = "create"
private const val JOIN = "join"

/**
 * Onboarding für Angemeldete ohne Haushalt: Willkommen → Erstellen oder Beitreten.
 * Sobald ein Haushalt existiert, schaltet die App selbst zur Hauptansicht um.
 *
 * @param inviteCode Code aus einem Einladungslink; öffnet direkt "Beitreten".
 */
@Composable
fun HouseholdOnboarding(inviteCode: String?) {
    val navController = rememberNavController()
    val joinRoute = "$JOIN?${JoinHouseholdViewModel.ARG_CODE}={${JoinHouseholdViewModel.ARG_CODE}}"

    NavHost(
        navController = navController,
        startDestination = if (inviteCode.isNullOrBlank()) WELCOME else joinRoute,
    ) {
        composable(WELCOME) {
            WelcomeScreen(
                onCreate = { navController.navigate(CREATE) },
                onJoin = { navController.navigate(JOIN) },
            )
        }
        composable(CREATE) {
            CreateHouseholdRoute(onBack = { navController.popBackStack() })
        }
        composable(
            route = joinRoute,
            arguments = listOf(
                navArgument(JoinHouseholdViewModel.ARG_CODE) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = inviteCode
                },
            ),
        ) {
            JoinHouseholdRoute(
                onBack = {
                    if (!navController.popBackStack()) navController.navigate(WELCOME)
                },
            )
        }
    }
}
