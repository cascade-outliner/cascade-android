package app.cascade.android.ui.outline

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import app.cascade.android.data.OutlineStore
import app.cascade.android.data.Task
import app.cascade.android.data.fromIsoDay
import app.cascade.android.data.isoDay
import app.cascade.design.Calendar
import app.cascade.design.ChoiceChip
import app.cascade.design.DISABLED_ALPHA
import app.cascade.design.R
import app.cascade.design.Sheet
import app.cascade.design.SheetDivider
import app.cascade.design.SheetItem
import app.cascade.design.SheetSection
import app.cascade.design.rememberSheetState
import app.cascade.design.theme.Space
import java.time.LocalDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeMenuSheet(
    store: OutlineStore,
    nodeId: String,
    onDismiss: () -> Unit,
    onZoomTo: (String?) -> Unit,
    copyLink: (String) -> Unit,
) {
    store.version
    val node = store.get(nodeId)
    if (node == null) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }
    val today = LocalDate.now()
    val locked = store.isLocked(nodeId)
    val childCount = store.childCount(nodeId)
    val sheetState = rememberSheetState()
    val scope = rememberCoroutineScope()
    var calendarOpen by remember { mutableStateOf(false) }

    fun close(then: () -> Unit = {}) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onDismiss()
            then()
        }
    }

    fun setDue(date: LocalDate?) {
        store.setDue(nodeId, date?.let(::isoDay))
        close()
    }

    Sheet(
        onDismiss = onDismiss,
        modifier = Modifier.testTag("node-menu"),
        title = nodeLabel(node, today),
        state = sheetState,
    ) {
        val choices = Modifier.padding(horizontal = Space.x3).alpha(if (locked) DISABLED_ALPHA else 1f)

        SheetSection("Convert into")
        Row(choices, horizontalArrangement = Arrangement.spacedBy(Space.x2)) {
            ChoiceChip(
                "Text",
                selected = node.task == null,
                icon = R.drawable.ic_dot,
                enabled = !locked,
                onClick = {
                    store.setTask(nodeId, null)
                    close()
                },
            )
            ChoiceChip(
                "Task",
                selected = node.task != null,
                icon = R.drawable.ic_circle,
                enabled = !locked,
                onClick = {
                    if (node.task == null) store.setTask(nodeId, Task(done = false))
                    close()
                },
            )
        }

        SheetSection("Due date")
        Row(
            Modifier.horizontalScroll(rememberScrollState()).then(choices),
            horizontalArrangement = Arrangement.spacedBy(Space.x2),
        ) {
            val due = node.due
            ChoiceChip("Today", selected = due == isoDay(today), enabled = !locked, onClick = { setDue(today) })
            ChoiceChip(
                "Tomorrow",
                selected = due == isoDay(today.plusDays(1)),
                enabled = !locked,
                onClick = { setDue(today.plusDays(1)) },
            )
            ChoiceChip(
                "Next week",
                selected = due == isoDay(today.plusDays(7)),
                enabled = !locked,
                onClick = { setDue(today.plusDays(7)) },
            )
            ChoiceChip(
                "Pick a day",
                selected = calendarOpen,
                icon = R.drawable.ic_calendar_blank,
                enabled = !locked,
                onClick = { calendarOpen = !calendarOpen },
            )
            if (due != null) {
                ChoiceChip(
                    "Remove",
                    selected = false,
                    icon = R.drawable.ic_calendar_x,
                    enabled = !locked,
                    onClick = { setDue(null) },
                )
            }
        }
        if (calendarOpen && !locked) {
            Box(Modifier.fillMaxWidth().padding(top = Space.x3), contentAlignment = Alignment.Center) {
                Calendar(value = node.due?.let(::fromIsoDay), today = today, onPick = { setDue(it) })
            }
        }

        SheetDivider(Modifier.padding(top = Space.x1))

        SheetItem("Zoom in", R.drawable.ic_magnifying_glass_plus, onClick = { close { onZoomTo(nodeId) } })
        SheetItem(
            "Duplicate",
            R.drawable.ic_copy,
            enabled = !locked,
            onClick = {
                store.duplicate(nodeId)
                close()
            },
        )
        SheetItem(
            "Duplicate with children",
            R.drawable.ic_copy,
            enabled = !locked && childCount > 0,
            onClick = {
                store.duplicateWithChildren(nodeId)
                close()
            },
        )
        SheetItem(
            "Indent",
            R.drawable.ic_arrow_line_right,
            enabled = store.canIndent(nodeId),
            onClick = {
                store.indent(nodeId)
                close()
            },
        )
        SheetItem(
            "Outdent",
            R.drawable.ic_arrow_line_left,
            enabled = store.canOutdent(nodeId),
            onClick = {
                store.outdent(nodeId)
                close()
            },
        )
        SheetItem(
            "Copy link",
            R.drawable.ic_link_simple,
            onClick = {
                copyLink(nodeId)
                close()
            },
        )
        SheetDivider()
        SheetItem("Delete", R.drawable.ic_trash, danger = true, onClick = { close { store.remove(nodeId) } })
    }
}
