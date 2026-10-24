package de.paarfinanz.app.di

import de.paarfinanz.app.DeepLinks
import de.paarfinanz.app.RootViewModel
import de.paarfinanz.app.ui.settings.SettingsViewModel
import de.paarfinanz.core.data.di.dataModule
import de.paarfinanz.feature.auth.AuthViewModel
import de.paarfinanz.feature.household.create.CreateHouseholdViewModel
import de.paarfinanz.feature.household.income.IncomeViewModel
import de.paarfinanz.feature.household.invite.InviteViewModel
import de.paarfinanz.feature.household.join.JoinHouseholdViewModel
import de.paarfinanz.feature.household.overview.HouseholdOverviewViewModel
import de.paarfinanz.feature.household.settings.HouseholdSettingsViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    single { DeepLinks() }
    viewModelOf(::RootViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::CreateHouseholdViewModel)
    viewModel { params -> JoinHouseholdViewModel(get(), params.getOrNull<String>()) }
    viewModelOf(::InviteViewModel)
    viewModelOf(::IncomeViewModel)
    viewModelOf(::HouseholdOverviewViewModel)
    viewModelOf(::HouseholdSettingsViewModel)
}

val appModules = listOf(dataModule, viewModelModule)

/** Startet Koin; auf Android zusätzlich mit dem Application-Context (siehe [platformSetup]). */
fun initKoin(platformSetup: KoinApplication.() -> Unit = {}) {
    startKoin {
        platformSetup()
        modules(appModules)
    }
}
