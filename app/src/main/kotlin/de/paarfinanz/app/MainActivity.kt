package de.paarfinanz.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val supabaseClient: SupabaseClient by inject()
    private val deepLinks: DeepLinks by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent { PaarfinanzApp() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    /** Einladungslinks merkt sich [DeepLinks]; alles andere sind Supabase-Bestätigungslinks. */
    private fun handleIntent(intent: Intent) {
        val url = intent.dataString ?: return
        if (!deepLinks.handle(url)) supabaseClient.handleDeeplinks(intent)
    }
}
