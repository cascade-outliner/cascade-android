package app.cascade.android.commands

import androidx.annotation.DrawableRes
import app.cascade.design.R
import app.cascade.design.SlashMenuItem
import app.cascade.android.data.Node
import app.cascade.android.data.OutlineStore
import app.cascade.android.data.Task
import app.cascade.android.data.isoDay
import java.time.LocalDate

class SlashContext(
    val store: OutlineStore,
    val node: Node,
    val childCount: Int,
    val zoomTo: (String?) -> Unit,
    val copyLink: (String) -> Unit,
    val today: LocalDate = LocalDate.now(),
)

class SlashCommand(
    override val id: String,
    override val group: String,
    override val label: String,
    override val keywords: List<String> = emptyList(),
    @param:DrawableRes override val icon: Int,
    override val danger: Boolean = false,
    val capture: Boolean = false,
    val available: (SlashContext) -> Boolean = { true },
    val run: (SlashContext) -> Unit,
) : SlashMenuItem

private const val CONVERT = "Convert"
private const val DUE = "Due date"
private const val ORGANIZE = "Organize"
private const val NODE = "Node"

val slashCommands: List<SlashCommand> = listOf(
    SlashCommand(
        id = "turn-into-task",
        group = CONVERT,
        label = "Turn into task",
        keywords = listOf("todo", "checkbox"),
        icon = R.drawable.ic_circle,
        capture = true,
        available = { it.node.task == null && !it.store.isLocked(it.node.id) },
        run = { it.store.setTask(it.node.id, Task(done = false)) },
    ),
    SlashCommand(
        id = "turn-into-text",
        group = CONVERT,
        label = "Turn into text",
        keywords = listOf("plain", "note"),
        icon = R.drawable.ic_dot,
        available = { it.node.task != null },
        run = { it.store.setTask(it.node.id, null) },
    ),
    SlashCommand(
        id = "complete-task",
        group = CONVERT,
        label = "Complete task",
        keywords = listOf("done", "finish", "check"),
        icon = R.drawable.ic_check_circle_fill,
        available = { it.node.task?.done == false },
        run = { it.store.setTask(it.node.id, Task(done = true)) },
    ),
    SlashCommand(
        id = "reopen-task",
        group = CONVERT,
        label = "Reopen task",
        keywords = listOf("undone", "uncheck"),
        icon = R.drawable.ic_circle,
        available = { it.node.task?.done == true },
        run = { it.store.setTask(it.node.id, Task(done = false)) },
    ),
    SlashCommand(
        id = "due-today",
        group = DUE,
        label = "Due today",
        keywords = listOf("date", "deadline"),
        icon = R.drawable.ic_calendar_blank,
        capture = true,
        run = { it.store.setDue(it.node.id, isoDay(it.today)) },
    ),
    SlashCommand(
        id = "due-tomorrow",
        group = DUE,
        label = "Due tomorrow",
        keywords = listOf("date", "deadline"),
        icon = R.drawable.ic_calendar_blank,
        capture = true,
        run = { it.store.setDue(it.node.id, isoDay(it.today.plusDays(1))) },
    ),
    SlashCommand(
        id = "due-next-week",
        group = DUE,
        label = "Due next week",
        keywords = listOf("date", "deadline"),
        icon = R.drawable.ic_calendar_blank,
        capture = true,
        run = { it.store.setDue(it.node.id, isoDay(it.today.plusDays(7))) },
    ),
    SlashCommand(
        id = "remove-due",
        group = DUE,
        label = "Remove due date",
        keywords = listOf("clear", "unschedule"),
        icon = R.drawable.ic_calendar_x,
        available = { it.node.due != null },
        run = { it.store.setDue(it.node.id, null) },
    ),
    SlashCommand(
        id = "indent",
        group = ORGANIZE,
        label = "Indent",
        keywords = listOf("nest", "right"),
        icon = R.drawable.ic_arrow_line_right,
        available = { it.store.canIndent(it.node.id) },
        run = { it.store.indent(it.node.id) },
    ),
    SlashCommand(
        id = "outdent",
        group = ORGANIZE,
        label = "Outdent",
        keywords = listOf("unnest", "left"),
        icon = R.drawable.ic_arrow_line_left,
        available = { it.store.canOutdent(it.node.id) },
        run = { it.store.outdent(it.node.id) },
    ),
    SlashCommand(
        id = "duplicate",
        group = ORGANIZE,
        label = "Duplicate",
        keywords = listOf("copy", "clone"),
        icon = R.drawable.ic_copy,
        run = { it.store.duplicate(it.node.id) },
    ),
    SlashCommand(
        id = "duplicate-with-children",
        group = ORGANIZE,
        label = "Duplicate with children",
        keywords = listOf("copy", "clone", "subtree"),
        icon = R.drawable.ic_copy,
        available = { it.childCount > 0 },
        run = { it.store.duplicateWithChildren(it.node.id) },
    ),
    SlashCommand(
        id = "zoom-in",
        group = NODE,
        label = "Zoom in",
        keywords = listOf("open", "focus"),
        icon = R.drawable.ic_magnifying_glass_plus,
        capture = true,
        run = { it.zoomTo(it.node.id) },
    ),
    SlashCommand(
        id = "copy-link",
        group = NODE,
        label = "Copy link",
        keywords = listOf("url", "share"),
        icon = R.drawable.ic_link_simple,
        run = { it.copyLink(it.node.id) },
    ),
    SlashCommand(
        id = "delete",
        group = NODE,
        label = "Delete",
        keywords = listOf("remove", "trash"),
        icon = R.drawable.ic_trash,
        danger = true,
        run = { it.store.remove(it.node.id) },
    ),
)

val captureSlashCommands: List<SlashCommand> = slashCommands.filter { it.capture }

fun nodeLink(id: String): String = "cascade://node/$id"
