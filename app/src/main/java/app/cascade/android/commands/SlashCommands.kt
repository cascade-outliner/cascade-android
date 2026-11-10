package app.cascade.android.commands

import androidx.annotation.DrawableRes
import app.cascade.android.R
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
    val id: String,
    val group: String,
    val label: String,
    val keywords: List<String> = emptyList(),
    @param:DrawableRes val icon: Int,
    val danger: Boolean = false,
    val capture: Boolean = false,
    val available: (SlashContext) -> Boolean = { true },
    val run: (SlashContext) -> Unit,
)

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

private fun rankOf(command: SlashCommand, query: String): Int? {
    val label = command.label.lowercase()
    if (label.startsWith(query)) return 0
    val words = label.split(Regex("""\s+""")) + command.keywords
    return if (words.any { it.lowercase().startsWith(query) }) 1 else null
}

fun filterSlashCommands(commands: List<SlashCommand>, query: String): List<SlashCommand> {
    val needle = query.trim().lowercase()
    if (needle.isEmpty()) return commands
    val ranked = commands.mapIndexedNotNull { index, command ->
        rankOf(command, needle)?.let { Triple(command, it, index) }
    }
    val groupRank = LinkedHashMap<String, Int>()
    for ((command, rank) in ranked) {
        groupRank[command.group] = minOf(rank, groupRank[command.group] ?: Int.MAX_VALUE)
    }
    val groupOrder = groupRank.keys.toList()
    return ranked.sortedWith(
        compareBy<Triple<SlashCommand, Int, Int>> { groupRank.getValue(it.first.group) }
            .thenBy { groupOrder.indexOf(it.first.group) }
            .thenBy { it.second }
            .thenBy { it.third },
    ).map { it.first }
}

fun groupSlashCommands(commands: List<SlashCommand>): List<Pair<String, List<SlashCommand>>> {
    val groups = ArrayList<Pair<String, MutableList<SlashCommand>>>()
    for (command in commands) {
        val last = groups.lastOrNull()
        if (last?.first == command.group) last.second.add(command) else groups.add(command.group to mutableListOf(command))
    }
    return groups
}

data class SlashTrigger(val query: String, val index: Int)

private val TRIGGER = Regex("""(?:^|\s)/([^\s/]*)$""")

fun matchSlashTrigger(text: String): SlashTrigger? {
    val match = TRIGGER.find(text) ?: return null
    val query = match.groupValues[1]
    return SlashTrigger(query, match.range.last + 1 - query.length - 1)
}
