package app.cascade.android.ui.outline

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cascade.android.R
import app.cascade.android.data.OutlineStore
import app.cascade.android.data.Task
import app.cascade.android.data.fromIsoDay
import app.cascade.android.data.isoDay
import app.cascade.android.ui.components.Calendar
import app.cascade.android.ui.components.CascadeIcon
import app.cascade.android.ui.theme.Cascade
import app.cascade.android.ui.theme.FontSize
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
    val colors = Cascade.colors
    store.version
    val node = store.get(nodeId)
    if (node == null) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }
    val today = LocalDate.now()
    val locked = store.isLocked(nodeId)
    val childCount = store.childCount(nodeId)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.white,
        contentColor = colors.ink,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                .testTag("node-menu"),
        ) {
            Text(
                nodeLabel(node, today),
                Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp),
                fontSize = FontSize.s600,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            SectionLabel("Convert into")
            Row(
                Modifier.padding(horizontal = 12.dp).alpha(if (locked) 0.4f else 1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Choice("Text", R.drawable.ic_dot, selected = node.task == null, enabled = !locked) {
                    store.setTask(nodeId, null)
                    close()
                }
                Choice("Task", R.drawable.ic_circle, selected = node.task != null, enabled = !locked) {
                    if (node.task == null) store.setTask(nodeId, Task(done = false))
                    close()
                }
            }

            SectionLabel("Due date")
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
                    .alpha(if (locked) 0.4f else 1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val due = node.due
                Choice("Today", null, selected = due == isoDay(today), enabled = !locked) { setDue(today) }
                Choice("Tomorrow", null, selected = due == isoDay(today.plusDays(1)), enabled = !locked) {
                    setDue(today.plusDays(1))
                }
                Choice("Next week", null, selected = due == isoDay(today.plusDays(7)), enabled = !locked) {
                    setDue(today.plusDays(7))
                }
                Choice("Pick a day", R.drawable.ic_calendar_blank, selected = calendarOpen, enabled = !locked) {
                    calendarOpen = !calendarOpen
                }
                if (due != null) {
                    Choice("Remove", R.drawable.ic_calendar_x, selected = false, enabled = !locked) { setDue(null) }
                }
            }
            if (calendarOpen && !locked) {
                Box(Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.Center) {
                    Calendar(value = node.due?.let(::fromIsoDay), today = today, onPick = { setDue(it) })
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = colors.border)

            Item("Zoom in", R.drawable.ic_magnifying_glass_plus) { close { onZoomTo(nodeId) } }
            Item("Duplicate", R.drawable.ic_copy, enabled = !locked) {
                store.duplicate(nodeId)
                close()
            }
            Item("Duplicate with children", R.drawable.ic_copy, enabled = !locked && childCount > 0) {
                store.duplicateWithChildren(nodeId)
                close()
            }
            Item("Indent", R.drawable.ic_arrow_line_right, enabled = store.canIndent(nodeId)) {
                store.indent(nodeId)
                close()
            }
            Item("Outdent", R.drawable.ic_arrow_line_left, enabled = store.canOutdent(nodeId)) {
                store.outdent(nodeId)
                close()
            }
            Item("Copy link", R.drawable.ic_link_simple) {
                copyLink(nodeId)
                close()
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = colors.border)
            Item("Delete", R.drawable.ic_trash, danger = true) { close { store.remove(nodeId) } }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
        color = Cascade.colors.muted,
        fontSize = FontSize.s100,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.8.sp,
    )
}

@Composable
private fun Choice(label: String, @DrawableRes icon: Int?, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = Cascade.colors
    val shape = RoundedCornerShape(12.dp)
    val content = if (selected) colors.onPrimary else colors.ink
    Row(
        Modifier
            .clip(shape)
            .background(if (selected) colors.primary else Color.Transparent)
            .border(1.dp, if (selected) colors.primary else colors.border, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) CascadeIcon(icon, size = 14.dp, tint = content)
        Text(label, color = content, fontSize = FontSize.s400, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
private fun Item(
    label: String,
    @DrawableRes icon: Int,
    enabled: Boolean = true,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = Cascade.colors
    val tint = if (danger) colors.danger else colors.ink
    Row(
        Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            CascadeIcon(icon, size = 18.dp, tint = if (danger) tint else colors.muted)
        }
        Text(label, color = tint, fontSize = FontSize.s600)
    }
}
