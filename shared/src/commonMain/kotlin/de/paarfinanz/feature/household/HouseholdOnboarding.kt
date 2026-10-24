package de.paarfinanz.feature.household

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.paarfinanz.feature.household.create.CreateHouseholdRoute
import de.paarfinanz.feature.household.join.JoinHouseholdRoute
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

    NavHost(
        navController = navController,
        startDestination = if (inviteCode.isNullOrBlank()) WELCOME else JOIN,
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
        composable(JOIN) {
            JoinHouseholdRoute(
                initialCode = inviteCode,
                onBack = {
                    if (!navController.popBackStack()) navController.navigate(WELCOME)
                },
            )
        }
    }
}
