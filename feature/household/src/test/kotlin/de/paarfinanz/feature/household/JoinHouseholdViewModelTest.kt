package de.paarfinanz.feature.household

import androidx.lifecycle.SavedStateHandle
import de.paarfinanz.feature.household.join.JoinHouseholdViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JoinHouseholdViewModelTest {

    private val repository = FakeHouseholdRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(code: String? = null) =
        JoinHouseholdViewModel(repository, SavedStateHandle(mapOf(JoinHouseholdViewModel.ARG_CODE to code)))

    @Test
    fun zuKurzerCodeZeigtFormatHinweis() {
        val vm = viewModel()
        vm.onCodeChange("AB12")
        vm.checkCode()
        assertEquals(R.string.join_error_format, vm.uiState.value.errorRes)
        assertNull(vm.uiState.value.preview)
    }

    @Test
    fun gueltigerCodeZeigtVorschau() {
        val vm = viewModel()
        vm.onCodeChange("ab12-cd34")
        vm.checkCode()
        assertEquals("Anna", vm.uiState.value.preview?.inviterName)
        assertNull(vm.uiState.value.errorRes)
    }

    @Test
    fun unbekannterCodeZeigtFehler() {
        val vm = viewModel()
        vm.onCodeChange("ZZZZ-9999")
        vm.checkCode()
        assertEquals(R.string.error_invite_invalid, vm.uiState.value.errorRes)
    }

    @Test
    fun einladungslinkWirdVorausgefuelltUndGeprueft() {
        val vm = viewModel(code = "AB12CD34")
        assertEquals("AB12-CD34", vm.uiState.value.codeInput)
        assertEquals("Anna & Ben", vm.uiState.value.preview?.householdName)
    }

    @Test
    fun fremderQrCodeWirdAbgelehnt() {
        val vm = viewModel()
        vm.onScanned("https://example.com")
        assertEquals(R.string.join_error_scan, vm.uiState.value.errorRes)
    }

    @Test
    fun beitretenNutztNormalisiertenCode() {
        val vm = viewModel()
        vm.onCodeChange("ab12 cd34")
        vm.accept()
        assertEquals(listOf("AB12CD34"), repository.acceptedCodes)
    }
}
