package de.paarfinanz.feature.household.income

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.paarfinanz.core.data.household.HouseholdRepository
import de.paarfinanz.core.data.household.HouseholdState
import de.paarfinanz.core.domain.model.FairnessBasis
import de.paarfinanz.core.domain.model.HouseholdMember
import de.paarfinanz.core.domain.model.MemberIncome
import de.paarfinanz.core.domain.model.Money
import de.paarfinanz.core.domain.model.MoneyParser
import de.paarfinanz.core.domain.split.PartnerShares
import de.paarfinanz.feature.household.R
import de.paarfinanz.feature.household.householdMessageRes
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IncomeForm(
    val netInput: String = "",
    val fixedCostsInput: String = "",
    val validFrom: YearMonth = YearMonth.now(),
    val isSaving: Boolean = false,
    @StringRes val errorRes: Int? = null,
)

data class IncomeUiState(
    val form: IncomeForm = IncomeForm(),
    val currency: String = "EUR",
    val fairnessBasis: FairnessBasis = FairnessBasis.NET,
    val me: HouseholdMember? = null,
    val partner: HouseholdMember? = null,
    val partnerIncome: MemberIncome? = null,
    /** Vorschau des Verhältnisses mit den eingegebenen Werten; null, solange Angaben fehlen. */
    val previewShares: PartnerShares? = null,
)

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val householdRepository: HouseholdRepository,
) : ViewModel() {

    private val form = MutableStateFlow(IncomeForm())
    private val _saved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val saved: SharedFlow<Unit> = _saved.asSharedFlow()

    val uiState: StateFlow<IncomeUiState> = combine(form, householdRepository.householdState) { form, state ->
        val ready = state as? HouseholdState.Ready ?: return@combine IncomeUiState(form = form)
        val household = ready.household
        val date = form.validFrom.atDay(1)
        val partner = ready.partner
        val myNet = MoneyParser.parse(form.netInput)
        val myFixed = if (form.fixedCostsInput.isBlank()) Money.ZERO else MoneyParser.parse(form.fixedCostsInput)

        val preview = if (myNet != null && myFixed != null && partner != null) {
            val draft = MemberIncome(ready.currentUserId, myNet, myFixed, date)
            household
                .copy(incomes = household.incomes.filterNot { it.userId == ready.currentUserId && it.validFrom == date } + draft)
                .incomeShares(date)
                ?.takeUnless { it.isFallback }
        } else {
            null
        }

        IncomeUiState(
            form = form,
            currency = household.currency,
            fairnessBasis = household.fairnessBasis,
            me = ready.me,
            partner = partner,
            partnerIncome = partner?.let { household.currentIncome(it.userId, date) },
            previewShares = preview,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IncomeUiState())

    init {
        // Vorhandenes Einkommen zum Bearbeiten vorausfüllen.
        viewModelScope.launch {
            val ready = householdRepository.householdState.first { it !is HouseholdState.Loading } as? HouseholdState.Ready
                ?: return@launch
            val current = ready.household.currentIncome(ready.currentUserId, LocalDate.now()) ?: return@launch
            form.update {
                it.copy(
                    netInput = MoneyParser.formatForInput(current.netIncome),
                    fixedCostsInput = if (current.privateFixedCosts.isZero) "" else MoneyParser.formatForInput(current.privateFixedCosts),
                )
            }
        }
    }

    fun onNetChange(value: String) = form.update { it.copy(netInput = value.take(MAX_INPUT), errorRes = null) }

    fun onFixedCostsChange(value: String) = form.update { it.copy(fixedCostsInput = value.take(MAX_INPUT), errorRes = null) }

    fun previousMonth() = form.update { it.copy(validFrom = it.validFrom.minusMonths(1)) }

    fun nextMonth() = form.update { it.copy(validFrom = it.validFrom.plusMonths(1)) }

    fun save() {
        val current = form.value
        if (current.isSaving) return
        val net = MoneyParser.parse(current.netInput)
        val fixed = if (current.fixedCostsInput.isBlank()) Money.ZERO else MoneyParser.parse(current.fixedCostsInput)
        if (net == null || fixed == null) {
            form.update { it.copy(errorRes = R.string.income_error_amount) }
            return
        }
        form.update { it.copy(isSaving = true, errorRes = null) }
        viewModelScope.launch {
            householdRepository.saveIncome(net, fixed, current.validFrom.atDay(1))
                .onSuccess {
                    form.update { it.copy(isSaving = false) }
                    _saved.emit(Unit)
                }
                .onFailure { error -> form.update { it.copy(isSaving = false, errorRes = error.householdMessageRes()) } }
        }
    }

    private companion object {
        const val MAX_INPUT = 16
    }
}
