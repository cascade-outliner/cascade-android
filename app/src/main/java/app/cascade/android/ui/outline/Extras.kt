package app.cascade.android.ui.outline

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cascade.android.R
import app.cascade.android.data.Node
import app.cascade.android.data.OutlineStore
import app.cascade.android.data.Task
import app.cascade.android.ui.components.ButtonVariant
import app.cascade.android.ui.components.CascadeButton
import app.cascade.android.ui.components.CascadeIcon
import app.cascade.android.ui.components.TaskMarker
import app.cascade.android.ui.theme.Cascade
import app.cascade.android.ui.theme.FontSize

@Composable
private fun EmptyCard(
    @DrawableRes icon: Int,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    val colors = Cascade.colors
    Column(
        modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(40.dp).background(colors.surface, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
            CascadeIcon(icon, size = 18.dp, tint = colors.primary)
        }
        Text(title, fontSize = FontSize.s700, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Text(
            description,
            Modifier.widthIn(max = 260.dp),
            color = colors.muted,
            fontSize = FontSize.s300,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            CascadeButton(onAction, Modifier.padding(top = 4.dp), variant = ButtonVariant.Secondary) { Text(action) }
        }
    }
}

@Composable
fun NodeNotFound(onBack: () -> Unit, modifier: Modifier = Modifier) {
    EmptyCard(
        R.drawable.ic_link_break,
        "Node not found",
        "This node was deleted or the link is out of date. Its children, if it had any, went with it.",
        modifier.testTag("node-not-found"),
        action = "Back to the outline",
        onAction = onBack,
    )
}

@Composable
fun OutlineEmpty(zoomed: Boolean, onAddStep: () -> Unit, modifier: Modifier = Modifier) {
    if (zoomed) {
        EmptyCard(
            R.drawable.ic_magnifying_glass_plus,
            "No children",
            "You have zoomed into a node with nothing inside it. Add steps, or zoom back out.",
            modifier,
            action = "Add a step",
            onAction = onAddStep,
        )
        return
    }
    Column(modifier.testTag("outline-empty")) {
        EmptyCard(
            R.drawable.ic_list_dashes,
            "Nothing here yet",
            "Start typing to make the first node. Indent it from its menu to nest it under another.",
            action = "Write the first line",
            onAction = onAddStep,
        )
        EmptyCard(
            R.drawable.ic_tree_structure,
            "Zoom into anything",
            "Tap a bullet to open that node as its own page. The breadcrumb takes you back out.",
        )
        EmptyCard(
            R.drawable.ic_hand_tap,
            "Hold a bullet for more",
            "Turn a node into a task, duplicate it or delete it from its menu. Keep holding and drag to move it.",
        )
    }
}

class DueGroup(val label: String, val day: String)

@Composable
fun DueElsewhere(
    store: OutlineStore,
    groups: List<DueGroup>,
    excluding: String,
    onZoomTo: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Cascade.colors
    store.version
    val filled = groups.map { it to store.dueOn(it.day, excluding) }.filter { it.second.isNotEmpty() }
    if (filled.isEmpty()) return

    Column(modifier.padding(horizontal = 8.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        for ((group, nodes) in filled) {
            Column(Modifier.testTag("due-elsewhere")) {
                Row(
                    Modifier.padding(horizontal = 10.dp).padding(bottom = 8.dp).semantics { heading() },
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        group.label.uppercase(),
                        color = colors.muted,
                        fontSize = FontSize.s200,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.9.sp,
                    )
                    Text(nodes.size.toString(), color = colors.placeholder, fontSize = FontSize.s200)
                }
                HorizontalDivider(Modifier.padding(bottom = 4.dp), color = colors.border)
                for (node in nodes) DueItem(store, node, onZoomTo)
            }
        }
    }
}

@Composable
private fun DueItem(store: OutlineStore, node: Node, onZoomTo: (String?) -> Unit) {
    val colors = Cascade.colors
    val parent = node.parentId?.let(store::get)
    val done = node.task?.done == true
    Row(
        Modifier.fillMaxWidth().padding(start = if (node.task != null) 4.dp else 10.dp, end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (node.task != null) {
            TaskMarker(done = done, onToggle = { store.setTask(node.id, Task(done = !done)) })
        }
        Text(
            node.text,
            Modifier.weight(1f).padding(vertical = 9.dp),
            color = if (done) colors.muted else colors.ink,
            textDecoration = if (done) TextDecoration.LineThrough else null,
        )
        Row(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button) { onZoomTo(node.parentId) }
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                parent?.let { nodeLabel(it) } ?: "Home",
                Modifier.widthIn(max = 120.dp),
                color = colors.muted,
                fontSize = FontSize.s200,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            CascadeIcon(R.drawable.ic_arrow_up_right, size = 11.dp, tint = colors.muted)
        }
    }
}
