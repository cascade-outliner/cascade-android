package app.cascade.android.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.cascade.android.commands.nodeLink
import app.cascade.android.data.OutlineStore
import app.cascade.android.data.Settings
import app.cascade.android.data.isDayId
import app.cascade.android.ui.outline.OutlineScreen
import app.cascade.design.theme.Cascade

class Navigator(initial: List<String> = emptyList()) {
    val stack = mutableStateListOf<String>().apply { addAll(initial) }

    val current: String? get() = stack.lastOrNull()

    fun zoomTo(id: String?) {
        val top = current
        when {
            id == null -> stack.clear()
            id == top -> Unit
            top != null && isDayId(top) && isDayId(id) -> stack[stack.lastIndex] = id
            else -> stack.add(id)
        }
    }

    fun back() {
        stack.removeLastOrNull()
    }

    companion object {
        val Saver = listSaver<Navigator, String>(save = { it.stack.toList() }, restore = { Navigator(it) })
    }
}

@Composable
fun CascadeApp(
    store: OutlineStore,
    settings: Settings,
    deepLink: String?,
    onDeepLinkHandled: () -> Unit,
) {
    val context = LocalContext.current
    val navigator = rememberSaveable(saver = Navigator.Saver) { Navigator() }
    var onboarded by remember { mutableStateOf(settings.onboarded) }
    var searching by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(deepLink) {
        if (deepLink != null) {
            navigator.zoomTo(deepLink)
            onDeepLinkHandled()
        }
    }

    val copyLink = remember(context) {
        { id: String ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Cascade link", nodeLink(id)))
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Cascade.colors.canvas)) {
        if (!store.ready) return@Box
        store.version
        if (!onboarded && store.size == 0) {
            Onboarding(
                onDone = {
                    settings.onboarded = true
                    onboarded = true
                },
            )
            return@Box
        }

        BackHandler(enabled = navigator.current != null && !searching) { navigator.back() }
        OutlineScreen(
            store = store,
            zoomedId = navigator.current,
            onZoomTo = navigator::zoomTo,
            onSearch = { searching = true },
            copyLink = copyLink,
        )
        AnimatedVisibility(
            visible = searching,
            enter = fadeIn(tween(150)) + slideInVertically(tween(200)) { it / 30 },
            exit = fadeOut(tween(100)),
        ) {
            BackHandler { searching = false }
            SearchOverlay(
                store = store,
                zoomedId = navigator.current,
                onDismiss = { searching = false },
                onZoomTo = navigator::zoomTo,
            )
        }
    }
}
