package de.paarfinanz.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import de.paarfinanz.core.domain.household.InviteCode
import de.paarfinanz.core.ui.theme.PaarfinanzTheme
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var supabaseClient: SupabaseClient

    /** Code aus einem Einladungslink, bis die Person einem Haushalt beitritt oder ihn verwirft. */
    private var pendingInviteCode by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            PaarfinanzTheme {
                PaarfinanzRoot(
                    pendingInviteCode = pendingInviteCode,
                    onInviteCodeConsumed = { pendingInviteCode = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val data = intent.data
        if (data?.scheme == "paarfinanz" && data.host == "invite") {
            pendingInviteCode = InviteCode.parse(data.toString())?.value
        } else {
            supabaseClient.handleDeeplinks(intent)
        }
    }
}
