package app.cascade.android.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cascade.android.R
import app.cascade.android.data.Node
import app.cascade.android.data.OutlineStore
import app.cascade.android.data.SearchHit
import app.cascade.android.data.dayId
import app.cascade.android.ui.components.CascadeIcon
import app.cascade.android.ui.components.SquareIconButton
import app.cascade.android.ui.outline.nodeLabel
import app.cascade.android.ui.theme.Cascade
import app.cascade.android.ui.theme.FontSize
import app.cascade.android.ui.theme.Onest
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
    store.version
    var query by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

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

    Column(
        modifier
            .fillMaxSize()
            .background(colors.canvas)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("search"),
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CascadeIcon(R.drawable.ic_magnifying_glass, size = 18.dp, tint = colors.muted)
            val style = TextStyle(fontFamily = Onest, fontSize = FontSize.s700, color = colors.ink)
            Box(Modifier.weight(1f).padding(vertical = 10.dp)) {
                if (query.isEmpty()) Text("Find or create a node…", style = style, color = colors.placeholder, maxLines = 1)
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().focusRequester(focus).testTag("search-input"),
                    textStyle = style,
                    singleLine = true,
                    cursorBrush = SolidColor(colors.info),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            val first = children?.hits?.firstOrNull() ?: others.hits.firstOrNull()
                            if (first != null) go(first.node.id)
                        },
                    ),
                )
            }
            SquareIconButton(R.drawable.ic_x_bold, "Close search", onDismiss, size = 40.dp, iconSize = 14.dp)
        }
        HorizontalDivider(color = colors.border)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) {
            if (trimmed.isNotEmpty()) {
                item(key = "status") {
                    Text(
                        "$total result${if (total == 1) "" else "s"}",
                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = colors.muted,
                        fontSize = FontSize.s200,
                    )
                }
            }
            if (children != null && children.hits.isNotEmpty()) {
                item(key = "children") { GroupLabel("Children") }
                for (hit in children.hits) {
                    item(key = "c:${hit.node.id}") { HitItem(store, hit, zoomedId) { go(hit.node.id) } }
                }
            }
            if (others.hits.isNotEmpty()) {
                item(key = "nodes") { GroupLabel("Nodes") }
                for (hit in others.hits) {
                    item(key = "n:${hit.node.id}") { HitItem(store, hit, null) { go(hit.node.id) } }
                }
            }
            if (trimmed.isNotEmpty()) {
                item(key = "create") {
                    GroupLabel("Create")
                    ActionItem("New node “$trimmed”", R.drawable.ic_plus_bold, Modifier.testTag("search-create"), create)
                }
            }
            item(key = "navigation") {
                GroupLabel("Navigation")
                ActionItem("Today's note", R.drawable.ic_calendar_blank) { go(dayId(LocalDate.now())) }
                if (zoomedId != null) ActionItem("Go back home", R.drawable.ic_house_simple) { go(null) }
            }
        }
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(
        text.uppercase(),
        Modifier.padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 6.dp),
        color = Cascade.colors.muted,
        fontSize = FontSize.s100,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.8.sp,
    )
}

@Composable
private fun ItemRow(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
            CascadeIcon(icon, size = 16.dp, tint = Cascade.colors.muted)
        }
        Column(Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun ActionItem(label: String, @DrawableRes icon: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ItemRow(icon, modifier, onClick) { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}

@Composable
private fun HitItem(store: OutlineStore, hit: SearchHit, pathFrom: String?, onClick: () -> Unit) {
    val colors = Cascade.colors
    val ancestors = store.ancestorsOf(hit.node.id)
    val path = ancestors
        .drop(ancestors.indexOfFirst { it.id == pathFrom } + 1)
        .joinToString(" / ") { nodeLabel(it) }
    val mark = SpanStyle(fontWeight = FontWeight.SemiBold, background = colors.infoMuted, color = colors.info)
    val label: AnnotatedString = buildAnnotatedString {
        append(hit.node.text.replace('\n', ' '))
        for (range in hit.ranges) addStyle(mark, range.first, range.last + 1)
    }
    ItemRow(
        nodeIcon(hit.node),
        Modifier.testTag("search-hit").semantics { contentDescription = hit.node.text },
        onClick,
    ) {
        Text(label, maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (path.isNotEmpty()) {
            Text(path, color = colors.muted, fontSize = FontSize.s200, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
