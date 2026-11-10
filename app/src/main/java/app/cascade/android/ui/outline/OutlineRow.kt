package app.cascade.android.ui.outline

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.cascade.android.commands.SlashContext
import app.cascade.android.commands.slashCommands
import app.cascade.android.data.OutlineStore
import app.cascade.android.data.Row
import app.cascade.android.data.Task
import app.cascade.android.data.dueLabel
import app.cascade.android.data.isoDay
import app.cascade.android.data.relativeDay
import app.cascade.design.Pill
import app.cascade.design.PillTone
import app.cascade.design.outliner.Bullet
import app.cascade.design.outliner.Chevron
import app.cascade.design.outliner.OutlinerContent
import app.cascade.design.outliner.OutlinerLabel
import app.cascade.design.outliner.OutlinerRow
import app.cascade.design.outliner.TaskMarker
import app.cascade.design.theme.Space
import java.time.LocalDate

@Composable
fun OutlineRow(
    row: Row,
    store: OutlineStore,
    active: Boolean,
    dragging: Boolean,
    onZoomTo: (String?) -> Unit,
    copyLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val node = row.node
    val today = LocalDate.now()
    var focused by remember { mutableStateOf(false) }

    fun context() = store.get(node.id)?.let { SlashContext(store, it, store.childCount(node.id), onZoomTo, copyLink) }

    OutlinerRow(depth = row.depth, modifier = modifier, active = active || focused, dragging = dragging) {
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
                modifier = Modifier.padding(top = Space.x1),
            )
        }
        if (store.isLocked(node.id)) {
            OutlinerLabel(relativeDay(node.id, today) ?: node.text, Modifier.weight(1f))
        } else {
            OutlinerContent(
                text = node.text,
                onTextChange = { store.setText(node.id, it) },
                modifier = Modifier.weight(1f),
                slashItems = if (focused) context()?.let { now -> slashCommands.filter { it.available(now) } }.orEmpty() else emptyList(),
                onSlashSelect = { command -> context()?.let(command.run) },
                onFocusChange = { focused = it },
            )
        }
        if (node.due != null) {
            Pill(
                dueLabel(node.due, today),
                Modifier.padding(start = Space.x2, top = Space.x2_5).testTag("due-pill"),
                tone = if (node.due <= isoDay(today)) PillTone.Primary else PillTone.Info,
            )
        }
    }
}
