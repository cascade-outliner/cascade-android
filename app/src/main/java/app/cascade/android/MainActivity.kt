package app.cascade.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cascade.android.ui.CascadeApp
import app.cascade.design.theme.CascadeTheme

class MainActivity : ComponentActivity() {
    private var deepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) deepLink = nodeIdOf(intent)
        val app = application as CascadeApplication
        setContent {
            CascadeTheme {
                CascadeApp(
                    store = app.store,
                    settings = app.settings,
                    deepLink = deepLink,
                    onDeepLinkHandled = { deepLink = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        nodeIdOf(intent)?.let { deepLink = it }
    }

    private fun nodeIdOf(intent: Intent?): String? {
        val uri = intent?.data ?: return null
        if (uri.scheme != "cascade" || uri.host != "node") return null
        return uri.lastPathSegment?.takeIf { it.isNotBlank() }
    }
}
