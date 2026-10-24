package de.paarfinanz.core.data.household

import de.paarfinanz.core.data.SupabaseConfig
import de.paarfinanz.core.data.auth.AuthRepository
import de.paarfinanz.core.data.auth.AuthState
import de.paarfinanz.core.domain.household.InviteCode
import de.paarfinanz.core.domain.model.Money
import de.paarfinanz.core.domain.model.ShareBps
import de.paarfinanz.core.domain.model.SplitMode
import de.paarfinanz.core.domain.model.UserId
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlin.concurrent.Volatile
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseHouseholdRepository(
    private val client: SupabaseClient,
    authRepository: AuthRepository,
) : HouseholdRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val refreshMutex = Mutex()
    private val _householdState = MutableStateFlow<HouseholdState>(HouseholdState.Loading)
    override val householdState: StateFlow<HouseholdState> = _householdState.asStateFlow()

    @Volatile
    private var currentUser: UserId? = null

    init {
        scope.launch {
            authRepository.authState.collectLatest { auth ->
                when (auth) {
                    is AuthState.SignedIn -> {
                        currentUser = UserId(auth.userId)
                        _householdState.value = HouseholdState.Loading
                        refresh()
                        observeChanges(auth.userId)
                    }
                    AuthState.SignedOut -> {
                        currentUser = null
                        _householdState.value = HouseholdState.None
                    }
                    AuthState.Loading -> _householdState.value = HouseholdState.Loading
                }
            }
        }
    }

    override suspend fun refresh() = withContext(Dispatchers.Default) {
        refreshMutex.withLock {
            val user = currentUser ?: return@withLock
            if (!SupabaseConfig.isConfigured) {
                _householdState.value = HouseholdState.Failed(HouseholdError.NOT_CONFIGURED)
                return@withLock
            }
            try {
                _householdState.value = loadHousehold(user)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Vorhandene Daten bleiben sichtbar, wenn eine Aktualisierung scheitert (z. B. offline).
                if (_householdState.value !is HouseholdState.Ready) {
                    _householdState.value = HouseholdState.Failed(HouseholdError.from(e))
                }
            }
        }
    }

    private suspend fun loadHousehold(user: UserId): HouseholdState {
        val membership = client.from(MEMBERS)
            .select(Columns.list("household_id")) { filter { eq("user_id", user.value) } }
            .decodeSingleOrNull<MembershipDto>()
            ?: return HouseholdState.None

        val householdId = membership.householdId
        return coroutineScope {
            val household = async {
                client.from(HOUSEHOLDS)
                    .select { filter { eq("id", householdId) } }
                    .decodeSingle<HouseholdDto>()
            }
            val members = async {
                client.from(MEMBERS)
                    .select(Columns.raw("user_id, role, fixed_share_bps, joined_at, profile:profiles(display_name)")) {
                        filter { eq("household_id", householdId) }
                    }
                    .decodeList<MemberDto>()
            }
            val incomes = async {
                client.from(INCOMES)
                    .select { filter { eq("household_id", householdId) } }
                    .decodeList<IncomeDto>()
            }
            HouseholdState.Ready(
                household = HouseholdMapper.toDomain(household.await(), members.await(), incomes.await()),
                currentUserId = user,
            )
        }
    }

    /** Lädt neu, sobald sich am Haushalt etwas ändert – z. B. wenn der Partner beitritt. */
    @OptIn(FlowPreview::class)
    private suspend fun observeChanges(userId: String) {
        if (!SupabaseConfig.isConfigured) return
        val channel = client.channel("household-$userId")
        try {
            val changes = listOf(HOUSEHOLDS, MEMBERS, INCOMES).map { table ->
                channel.postgresChangeFlow<PostgresAction>(schema = "public") { this.table = table }
            }
            channel.subscribe()
            merge(*changes.toTypedArray())
                .debounce(REFRESH_DEBOUNCE_MS)
                .collect { refresh() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Ohne Echtzeit funktioniert die App weiter; Daten werden dann bei Aktionen neu geladen.
        } finally {
            withContext(NonCancellable) {
                runCatching { client.realtime.removeChannel(channel) }
            }
        }
    }

    override suspend fun createHousehold(name: String, splitMode: SplitMode): Result<Unit> = call {
        client.postgrest.rpc(
            "create_household",
            buildJsonObject {
                put("p_name", name.trim())
                put("p_split_mode", splitMode.name)
            },
        )
        refresh()
    }

    override suspend fun createInvite(): Result<InviteCode> = call {
        val householdId = requireHousehold().household.id
        val raw = client.postgrest
            .rpc("create_household_invite", buildJsonObject { put("p_household_id", householdId) })
            .decodeAs<String>()
        InviteCode.parse(raw) ?: throw HouseholdException(HouseholdError.UNKNOWN)
    }

    override suspend fun previewInvite(code: InviteCode): Result<InvitePreview> = call {
        val preview = client.postgrest
            .rpc("preview_household_invite", buildJsonObject { put("p_code", code.value) })
            .decodeList<InvitePreviewDto>()
            .firstOrNull()
            ?: throw HouseholdException(HouseholdError.INVITE_INVALID)
        InvitePreview(householdName = preview.householdName, inviterName = preview.inviterName)
    }

    override suspend fun acceptInvite(code: InviteCode): Result<Unit> = call {
        client.postgrest.rpc("accept_household_invite", buildJsonObject { put("p_code", code.value) })
        refresh()
    }

    override suspend fun updateSettings(settings: HouseholdSettings): Result<Unit> = call {
        val householdId = requireHousehold().household.id
        client.from(HOUSEHOLDS).update(
            HouseholdSettingsDto(
                name = settings.name.trim(),
                defaultSplitMode = settings.defaultSplitMode.name,
                fairnessBasis = settings.fairnessBasis.name,
                jointAccountEnabled = settings.jointAccountEnabled,
            ),
        ) { filter { eq("id", householdId) } }
        refresh()
    }

    override suspend fun setMyFixedShare(share: ShareBps): Result<Unit> = call {
        val ready = requireHousehold()
        val partner = ready.partner ?: throw HouseholdException(HouseholdError.NOT_MEMBER)
        val householdId = ready.household.id
        updateFixedShare(householdId, ready.currentUserId, share)
        updateFixedShare(householdId, partner.userId, share.complement)
        refresh()
    }

    private suspend fun updateFixedShare(householdId: String, userId: UserId, share: ShareBps) {
        client.from(MEMBERS).update(FixedShareDto(share.value)) {
            filter {
                eq("household_id", householdId)
                eq("user_id", userId.value)
            }
        }
    }

    override suspend fun saveIncome(netIncome: Money, privateFixedCosts: Money, validFrom: LocalDate): Result<Unit> = call {
        val ready = requireHousehold()
        client.from(INCOMES).upsert(
            IncomeUpsertDto(
                householdId = ready.household.id,
                userId = ready.currentUserId.value,
                netIncomeCents = netIncome.cents,
                privateFixedCostsCents = privateFixedCosts.cents,
                validFrom = validFrom.toString(),
            ),
        ) { onConflict = "household_id,user_id,valid_from" }
        refresh()
    }

    private fun requireHousehold(): HouseholdState.Ready =
        _householdState.value as? HouseholdState.Ready ?: throw HouseholdException(HouseholdError.NO_HOUSEHOLD)

    private suspend fun <T> call(block: suspend () -> T): Result<T> {
        if (!SupabaseConfig.isConfigured) return Result.failure(HouseholdException(HouseholdError.NOT_CONFIGURED))
        return try {
            Result.success(withContext(Dispatchers.Default) { block() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(HouseholdException(HouseholdError.from(e), e))
        }
    }

    private companion object {
        const val HOUSEHOLDS = "households"
        const val MEMBERS = "household_members"
        const val INCOMES = "member_incomes"
        const val REFRESH_DEBOUNCE_MS = 300L
    }
}
