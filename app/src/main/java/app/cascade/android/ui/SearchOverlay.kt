package app.cascade.android.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.cascade.android.data.Node
import app.cascade.android.data.OutlineStore
import app.cascade.android.data.SearchHit
import app.cascade.android.data.dayId
import app.cascade.android.ui.outline.nodeLabel
import app.cascade.design.PaletteField
import app.cascade.design.PaletteGroup
import app.cascade.design.PaletteItem
import app.cascade.design.R
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.FontSize
import app.cascade.design.theme.Space
import java.time.LocalDate

private const val NODE_LIMIT = 8

@DrawableRes
private fun nodeIcon(node: Node): Int = when (node.task?.done) {
    null -> R.drawable.ic_dot
    true -> R.drawable.ic_check_circle_fill
    false -> R.drawable.ic_circle
}

@Composable
fun SearchOverlay(
    store: OutlineStore,
    zoomedId: String?,
    onDismiss: () -> Unit,
    onZoomTo: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Cascade.colors
    var query by remember { mutableStateOf("") }
    val trimmed = query.trim()
    val children = remember(query, zoomedId, store.version) {
        zoomedId?.let { store.search(query, limit = NODE_LIMIT, within = it) }
    }
    val others = remember(query, zoomedId, store.version) {
        store.search(query, limit = NODE_LIMIT, excluding = zoomedId)
    }
    val total = (children?.total ?: 0) + others.total

    val go: (String?) -> Unit = { id ->
        onDismiss()
        onZoomTo(id)
    }

    val create: () -> Unit = {
        if (trimmed.isNotEmpty()) {
            store.create(zoomedId, text = trimmed)
            onDismiss()
        }
    }

    @Composable
    fun Hit(hit: SearchHit, pathFrom: String?) {
        val ancestors = store.ancestorsOf(hit.node.id)
        PaletteItem(
            label = hit.node.text.replace('\n', ' '),
            icon = nodeIcon(hit.node),
            onClick = { go(hit.node.id) },
            modifier = Modifier.testTag("search-hit"),
            ranges = hit.ranges,
            detail = ancestors.drop(ancestors.indexOfFirst { it.id == pathFrom } + 1).joinToString(" / ") { nodeLabel(it) },
        )
    }

    Column(
        modifier
            .fillMaxSize()
            .background(colors.canvas)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("search"),
    ) {
        PaletteField(
            query = query,
            onQueryChange = { query = it },
            onClose = onDismiss,
            placeholder = "Find or create a node…",
            onSubmit = { (children?.hits?.firstOrNull() ?: others.hits.firstOrNull())?.let { go(it.node.id) } },
        )
        HorizontalDivider(color = colors.border)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(Space.x2)) {
            if (trimmed.isNotEmpty()) {
                item(key = "status") {
                    Text(
                        "$total result${if (total == 1) "" else "s"}",
                        Modifier.padding(horizontal = Space.x3, vertical = Space.x1_5),
                        color = colors.muted,
                        fontSize = FontSize.s200,
                    )
                }
            }
            if (children != null && children.hits.isNotEmpty()) {
                item(key = "children") { PaletteGroup("Children") }
                for (hit in children.hits) item(key = "c:${hit.node.id}") { Hit(hit, zoomedId) }
            }
            if (others.hits.isNotEmpty()) {
                item(key = "nodes") { PaletteGroup("Nodes") }
                for (hit in others.hits) item(key = "n:${hit.node.id}") { Hit(hit, null) }
            }
            if (trimmed.isNotEmpty()) {
                item(key = "create") {
                    PaletteGroup("Create")
                    PaletteItem("New node “$trimmed”", R.drawable.ic_plus_bold, create, Modifier.testTag("search-create"))
                }
            }
            item(key = "navigation") {
                PaletteGroup("Navigation")
                PaletteItem("Today's note", R.drawable.ic_calendar_blank, { go(dayId(LocalDate.now())) })
                if (zoomedId != null) PaletteItem("Go back home", R.drawable.ic_house_simple, { go(null) })
            }
        }
    }
}
