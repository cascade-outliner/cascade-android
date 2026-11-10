package app.cascade.android.ui.outline

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cascade.android.commands.SlashCommand
import app.cascade.android.commands.SlashContext
import app.cascade.android.data.Node
import app.cascade.android.data.OutlineStore
import app.cascade.android.data.Row
import app.cascade.android.data.dayDate
import app.cascade.android.data.dayId
import app.cascade.android.data.dayTitle
import app.cascade.android.data.isDayId
import app.cascade.android.data.isoDay
import app.cascade.android.data.relativeDay
import app.cascade.android.ui.theme.Cascade
import app.cascade.android.ui.theme.FontSize
import app.cascade.android.ui.theme.Onest
import java.time.LocalDate
import kotlin.math.abs

private const val HEADER_KEY = "\u0000header"
private const val EMPTY_KEY = "\u0000empty"
private const val DUE_KEY = "\u0000due"

private fun virtualDay(id: String) = Node(id = id, parentId = null, order = "", text = dayTitle(dayDate(id)))

private fun zoomedNode(store: OutlineStore, zoomedId: String?): Node? =
    zoomedId?.let { store.get(it) ?: if (isDayId(it)) virtualDay(it) else null }

@Composable
fun OutlineScreen(
    store: OutlineStore,
    zoomedId: String?,
    onZoomTo: (String?) -> Unit,
    onSearch: () -> Unit,
    copyLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Cascade.colors
    store.version
    val found = zoomedId == null || zoomedNode(store, zoomedId) != null
    val captureFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var menuId by remember { mutableStateOf<String?>(null) }
    var captured by remember { mutableIntStateOf(0) }

    val currentZoomedId by rememberUpdatedState(zoomedId)
    val capture: (String, List<SlashCommand>) -> Unit = { text, picked ->
        val id = store.create(currentZoomedId, text = text)
        for (command in picked) {
            val node = store.get(id) ?: break
            command.run(SlashContext(store, node, 0, onZoomTo, copyLink))
        }
        captured++
    }

    Column(modifier.fillMaxSize().background(colors.canvas).windowInsetsPadding(WindowInsets.safeDrawing)) {
        AppHeader(store, zoomedId, onZoomTo, onSearch)
        Box(Modifier.weight(1f).fillMaxWidth()) {
            AnimatedContent(
                targetState = zoomedId,
                transitionSpec = {
                    (fadeIn(tween(220, delayMillis = 60)) + slideInVertically(tween(220, delayMillis = 60)) { it / 24 })
                        .togetherWith(fadeOut(tween(90)))
                },
                label = "zoom",
            ) { pageId ->
                OutlinePage(
                    store = store,
                    zoomedId = pageId,
                    activeId = menuId,
                    captured = if (pageId == zoomedId) captured else 0,
                    onMenu = {
                        keyboard?.hide()
                        menuId = it
                    },
                    onZoomTo = onZoomTo,
                    copyLink = copyLink,
                    onAddStep = {
                        captureFocus.requestFocus()
                        keyboard?.show()
                    },
                )
            }
            if (found) {
                CaptureBar(
                    onSubmit = capture,
                    focusRequester = captureFocus,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
    }

    menuId?.let { id ->
        NodeMenuSheet(store, id, onDismiss = { menuId = null }, onZoomTo = onZoomTo, copyLink = copyLink)
    }
}

private class DragState {
    var activeId by mutableStateOf<String?>(null)
    var start by mutableStateOf(Offset.Zero)
    var pointer by mutableStateOf(Offset.Zero)
    var projection by mutableStateOf<Projection?>(null)

    fun reset() {
        activeId = null
        projection = null
    }
}

@Composable
private fun OutlinePage(
    store: OutlineStore,
    zoomedId: String?,
    activeId: String?,
    captured: Int,
    onMenu: (String) -> Unit,
    onZoomTo: (String?) -> Unit,
    copyLink: (String) -> Unit,
    onAddStep: () -> Unit,
) {
    val version = store.version
    val today = LocalDate.now()
    val zoomed = zoomedNode(store, zoomedId)
    if (zoomedId != null && zoomed == null) {
        NodeNotFound(onBack = { onZoomTo(null) }, Modifier.padding(top = 32.dp))
        return
    }

    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val drag = remember { DragState() }
    val allRows = remember(version, zoomedId) { store.rows(zoomedId) }
    val rows = remember(allRows, drag.activeId) { withoutDescendants(allRows, drag.activeId) }
    val dropRows = remember(rows, drag.activeId) { rows.filter { it.node.id != drag.activeId } }
    val activeRow = rows.firstOrNull { it.node.id == drag.activeId }
    val currentRows by rememberUpdatedState(rows)
    val currentDropRows by rememberUpdatedState(dropRows)

    LaunchedEffect(captured) {
        if (captured > 0) listState.animateScrollToItem((listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0))
    }

    fun project(position: Offset) {
        val active = currentRows.firstOrNull { it.node.id == drag.activeId }
        if (active == null) {
            drag.projection = null
            return
        }
        val info = listState.layoutInfo
        val y = position.y + info.viewportStartOffset
        val ids = currentDropRows.mapTo(HashSet()) { it.node.id }
        val over = info.visibleItemsInfo
            .filter { it.key in ids }
            .minByOrNull { abs(it.offset + it.size / 2f - y) }
        drag.projection = over?.let {
            project(
                rows = currentDropRows,
                overId = it.key as String,
                before = y < it.offset + it.size / 2f,
                activeDepth = active.depth,
                indentSteps = (position.x - drag.start.x) / with(density) { OutlineMetrics.Indent.toPx() },
            )
        }
    }

    LaunchedEffect(drag.activeId) {
        if (drag.activeId == null) return@LaunchedEffect
        val edge = with(density) { 72.dp.toPx() }
        val bottomInset = with(density) { 88.dp.toPx() }
        val fastest = with(density) { 14.dp.toPx() }
        while (true) {
            withFrameNanos { }
            val height = listState.layoutInfo.viewportSize.height
            val y = drag.pointer.y
            val speed = when {
                y < edge -> -fastest * ((edge - y) / edge).coerceIn(0f, 1f)
                y > height - bottomInset - edge -> fastest * ((y - (height - bottomInset - edge)) / edge).coerceIn(0f, 1f)
                else -> 0f
            }
            if (speed != 0f && listState.scrollBy(speed) != 0f) project(drag.pointer)
        }
    }

    fun bulletAt(position: Offset): Row? {
        val info = listState.layoutInfo
        val y = position.y + info.viewportStartOffset
        val item = info.visibleItemsInfo.firstOrNull { y >= it.offset && y < it.offset + it.size } ?: return null
        val row = currentRows.firstOrNull { it.node.id == item.key } ?: return null
        val (from, to) = with(density) {
            val start = OutlineMetrics.bulletStart(row.depth).toPx()
            (start - 4.dp.toPx()) to (start + OutlineMetrics.Bullet.toPx() + 4.dp.toPx())
        }
        return row.takeIf { position.x in from..to }
    }

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(zoomedId) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    val target = bulletAt(down.position) ?: return@awaitEachGesture
                    val released = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) break
                        }
                        true
                    }
                    if (released != null) return@awaitEachGesture

                    val id = target.node.id
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (store.isLocked(id)) {
                        onMenu(id)
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            event.changes.forEach { it.consume() }
                            if (event.changes.none { it.pressed }) break
                        }
                        return@awaitEachGesture
                    }

                    drag.start = down.position
                    drag.pointer = down.position
                    drag.activeId = id
                    var moved = false
                    var finished = false
                    try {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            change.consume()
                            if (!change.pressed) {
                                finished = true
                                break
                            }
                            if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) moved = true
                            drag.pointer = change.position
                            if (moved) project(change.position)
                        }
                    } finally {
                        val target = drag.projection
                        drag.reset()
                        if (finished && !moved) {
                            onMenu(id)
                        } else if (finished && target != null) {
                            store.move(id, target.parentId ?: zoomedId, target.index)
                        }
                    }
                }
            },
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().testTag("outline"),
            contentPadding = PaddingValues(
                start = OutlineMetrics.ListPadding,
                end = OutlineMetrics.ListPadding,
                top = 12.dp,
                bottom = 112.dp,
            ),
        ) {
            if (zoomed != null) {
                item(key = HEADER_KEY) { ZoomTitle(store, zoomed, today) }
            }
            if (allRows.isEmpty()) {
                item(key = EMPTY_KEY) { OutlineEmpty(zoomed = zoomed != null, onAddStep = onAddStep) }
            }
            items(rows, key = { it.node.id }) { row ->
                OutlineRow(
                    row = row,
                    store = store,
                    active = activeId == row.node.id,
                    dragging = drag.activeId == row.node.id,
                    onZoomTo = onZoomTo,
                    copyLink = copyLink,
                    modifier = Modifier.animateItem(),
                )
            }
            if (zoomedId == dayId(today)) {
                item(key = DUE_KEY) {
                    DueElsewhere(
                        store = store,
                        groups = listOf(
                            DueGroup("Due today, elsewhere", isoDay(today)),
                            DueGroup("Tomorrow", isoDay(today.plusDays(1))),
                        ),
                        excluding = zoomedId,
                        onZoomTo = onZoomTo,
                    )
                }
            }
        }

        val projection = drag.projection
        if (projection != null && activeRow != null) {
            val info = listState.layoutInfo
            val over = info.visibleItemsInfo.firstOrNull { it.key == projection.overId }
            if (over != null) {
                val y = (if (projection.before) over.offset else over.offset + over.size) - info.viewportStartOffset
                DropIndicator(projection.depth, with(density) { y.toDp() })
            }
        }
        if (activeRow != null && drag.projection != null) {
            DragGhost(
                text = activeRow.node.text,
                y = with(density) { drag.pointer.y.toDp() },
                depth = drag.projection?.depth ?: activeRow.depth,
            )
        }
    }
}

@Composable
private fun ZoomTitle(store: OutlineStore, node: Node, today: LocalDate) {
    val colors = Cascade.colors
    val style = TextStyle(
        fontFamily = Onest,
        fontSize = FontSize.s800,
        lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.4).sp,
        color = colors.ink,
    )
    val modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 20.dp)
    if (store.isLocked(node.id)) {
        Text(relativeDay(node.id, today) ?: node.text, modifier.testTag("zoom-title"), style = style)
        return
    }
    var value by remember(node.id) { mutableStateOf(TextFieldValue(node.text)) }
    val shown = if (value.text == node.text) value else TextFieldValue(node.text, TextRange(node.text.length))
    Box(modifier) {
        if (shown.text.isEmpty()) Text("Untitled", style = style, color = colors.placeholder)
        BasicTextField(
            value = shown,
            onValueChange = {
                value = it
                store.setText(node.id, it.text)
            },
            modifier = Modifier.fillMaxWidth().testTag("zoom-title"),
            textStyle = style,
            cursorBrush = SolidColor(colors.info),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )
    }
}
