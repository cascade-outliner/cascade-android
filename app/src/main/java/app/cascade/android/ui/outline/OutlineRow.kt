package app.cascade.android.ui.outline

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cascade.android.R
import app.cascade.android.commands.SlashContext
import app.cascade.android.commands.filterSlashCommands
import app.cascade.android.commands.matchSlashTrigger
import app.cascade.android.commands.slashCommands
import app.cascade.android.data.OutlineStore
import app.cascade.android.data.Row as OutlineRowData
import app.cascade.android.data.Task
import app.cascade.android.data.dueLabel
import app.cascade.android.data.isoDay
import app.cascade.android.data.relativeDay
import app.cascade.android.ui.components.CascadeIcon
import app.cascade.android.ui.components.Pill
import app.cascade.android.ui.components.PillTone
import app.cascade.android.ui.components.TaskMarker
import app.cascade.android.ui.theme.Cascade
import app.cascade.android.ui.theme.FontSize
import app.cascade.android.ui.theme.Onest
import java.time.LocalDate

object OutlineMetrics {
    val ListPadding: Dp = 8.dp
    val RowInset: Dp = 2.dp
    val Indent: Dp = 14.dp
    val Chevron: Dp = 26.dp
    val Bullet: Dp = 30.dp
    val MinRowHeight: Dp = 40.dp

    fun bulletStart(depth: Int): Dp = ListPadding + RowInset + Indent * depth + Chevron
}

@Composable
fun OutlineRow(
    row: OutlineRowData,
    store: OutlineStore,
    active: Boolean,
    dragging: Boolean,
    onZoomTo: (String?) -> Unit,
    copyLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Cascade.colors
    val node = row.node
    val today = LocalDate.now()
    val locked = store.isLocked(node.id)
    var focused by remember { mutableStateOf(false) }
    val guide = colors.border

    Row(
        modifier
            .fillMaxWidth()
            .alpha(if (dragging) 0.35f else 1f)
            .drawBehind {
                val step = OutlineMetrics.Indent.toPx()
                val first = OutlineMetrics.RowInset.toPx() + OutlineMetrics.Chevron.toPx() / 2
                for (level in 0 until row.depth) {
                    val x = first + level * step
                    drawLine(guide, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
                }
            }
            .padding(start = OutlineMetrics.RowInset + OutlineMetrics.Indent * row.depth, end = OutlineMetrics.RowInset)
            .clip(RoundedCornerShape(14.dp))
            .background(if (active || focused) colors.white else Color.Transparent)
            .heightIn(min = OutlineMetrics.MinRowHeight)
            .padding(end = 10.dp)
            .testTag("outliner-row"),
        verticalAlignment = Alignment.Top,
    ) {
        Chevron(
            open = !node.collapsed,
            visible = row.childCount > 0,
            onClick = { store.setCollapsed(node.id, !node.collapsed) },
        )
        Bullet(collapsed = node.collapsed && row.childCount > 0, onClick = { onZoomTo(node.id) })
        if (node.task != null) {
            TaskMarker(
                done = node.task.done,
                onToggle = { store.setTask(node.id, Task(done = !node.task.done)) },
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        val textStyle = TextStyle(
            fontFamily = Onest,
            fontSize = FontSize.s600,
            lineHeight = 22.sp,
            color = colors.ink,
        )
        Box(Modifier.weight(1f).padding(start = 4.dp, top = 9.dp, bottom = 9.dp)) {
            if (locked) {
                Text(relativeDay(node.id, today) ?: node.text, style = textStyle, modifier = Modifier.testTag("outliner-content"))
            } else {
                var value by remember(node.id) { mutableStateOf(TextFieldValue(node.text)) }
                val shown = if (value.text == node.text) value else TextFieldValue(node.text, TextRange(node.text.length))
                var dismissed by remember { mutableStateOf<Int?>(null) }
                val trigger = if (focused && shown.selection.collapsed) {
                    matchSlashTrigger(shown.text.substring(0, shown.selection.start))
                } else {
                    null
                }
                LaunchedEffect(trigger?.index) { if (trigger?.index != dismissed) dismissed = null }

                fun context() = store.get(node.id)?.let {
                    SlashContext(store, it, store.childCount(node.id), onZoomTo, copyLink)
                }

                val commands = if (trigger != null && trigger.index != dismissed) {
                    val now = context()
                    if (now == null) emptyList() else filterSlashCommands(slashCommands.filter { it.available(now) }, trigger.query)
                } else {
                    emptyList()
                }

                BasicTextField(
                    value = shown,
                    onValueChange = {
                        value = it
                        store.setText(node.id, it.text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focused = it.isFocused }
                        .testTag("outliner-content"),
                    textStyle = textStyle,
                    cursorBrush = SolidColor(colors.info),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                if (commands.isNotEmpty() && trigger != null) {
                    SlashMenu(
                        commands = commands,
                        onDismiss = { dismissed = trigger.index },
                        onPick = { command ->
                            val cursor = shown.selection.start
                            val text = shown.text.removeRange(trigger.index, cursor)
                            value = TextFieldValue(text, TextRange(trigger.index))
                            store.setText(node.id, text)
                            context()?.let(command.run)
                        },
                    )
                }
            }
        }
        if (node.due != null) {
            Pill(
                dueLabel(node.due, today),
                Modifier.padding(start = 8.dp, top = 10.dp).testTag("due-pill"),
                tone = if (node.due <= isoDay(today)) PillTone.Primary else PillTone.Info,
            )
        }
    }
}

@Composable
private fun Chevron(open: Boolean, visible: Boolean, onClick: () -> Unit) {
    val rotation by animateFloatAsState(if (open) 90f else 0f, label = "chevron")
    Box(
        Modifier
            .size(OutlineMetrics.Chevron, OutlineMetrics.MinRowHeight)
            .then(
                if (visible) {
                    Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = onClick,
                        )
                        .semantics { contentDescription = if (open) "Collapse" else "Expand" }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (visible) {
            CascadeIcon(R.drawable.ic_caret_right_bold, Modifier.rotate(rotation), size = 12.dp, tint = Cascade.colors.muted)
        }
    }
}

@Composable
private fun Bullet(collapsed: Boolean, onClick: () -> Unit) {
    val colors = Cascade.colors
    Box(
        Modifier
            .size(OutlineMetrics.Bullet, OutlineMetrics.MinRowHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = "Zoom in. Hold for more, drag to move" }
            .testTag("outliner-bullet"),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(20.dp).background(if (collapsed) colors.inkSubtleHover else colors.inkSubtle, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(6.dp).background(colors.muted, CircleShape))
        }
    }
}

@Composable
fun DropIndicator(depth: Int, y: Dp, modifier: Modifier = Modifier) {
    val color = Cascade.colors.primary
    Row(
        modifier
            .offset(x = OutlineMetrics.bulletStart(depth) + 1.dp, y = y - 4.dp)
            .fillMaxWidth()
            .padding(end = OutlineMetrics.bulletStart(depth) + OutlineMetrics.ListPadding + 1.dp)
            .height(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Box(Modifier.weight(1f).height(2.dp).background(color, RoundedCornerShape(1.dp)))
    }
}

@Composable
fun DragGhost(text: String, y: Dp, depth: Int, modifier: Modifier = Modifier) {
    val colors = Cascade.colors
    androidx.compose.material3.Surface(
        modifier = modifier
            .offset(x = OutlineMetrics.bulletStart(depth), y = y - 20.dp)
            .padding(end = OutlineMetrics.ListPadding * 2)
            .width(260.dp),
        shape = RoundedCornerShape(14.dp),
        color = colors.white,
        shadowElevation = 10.dp,
    ) {
        Row(Modifier.height(40.dp).padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(OutlineMetrics.Bullet, 40.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(6.dp).background(colors.muted, CircleShape))
            }
            Text(text.ifBlank { "Untitled" }, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    }
}
