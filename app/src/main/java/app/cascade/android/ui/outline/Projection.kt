package app.cascade.android.ui.outline

import app.cascade.android.data.Row
import kotlin.math.roundToInt

data class Projection(
    val overId: String,
    val before: Boolean,
    val depth: Int,
    val parentId: String?,
    val index: Int,
    val nesting: Boolean,
)

fun withoutDescendants(rows: List<Row>, id: String?): List<Row> {
    val start = if (id == null) -1 else rows.indexOfFirst { it.node.id == id }
    if (start < 0) return rows
    var end = start + 1
    while (end < rows.size && rows[end].depth > rows[start].depth) end++
    return rows.subList(0, start + 1) + rows.subList(end, rows.size)
}

fun project(rows: List<Row>, overId: String, before: Boolean, activeDepth: Int, indentSteps: Float): Projection? {
    val overIndex = rows.indexOfFirst { it.node.id == overId }
    if (overIndex < 0) return null
    val gap = if (before) overIndex else overIndex + 1
    val previous = rows.getOrNull(gap - 1)
    val next = rows.getOrNull(gap)
    val maxDepth = if (previous != null) previous.depth + 1 else 0
    val minDepth = next?.depth ?: 0
    val depth = (activeDepth + indentSteps.roundToInt()).coerceIn(minDepth, maxOf(minDepth, maxDepth))

    var parent: Row? = null
    var index = 0
    for (i in gap - 1 downTo 0) {
        val row = rows[i]
        if (row.depth == depth) {
            index++
        } else if (row.depth < depth) {
            parent = row
            break
        }
    }
    if (parent?.node?.collapsed == true) index = parent.childCount
    return Projection(
        overId = overId,
        before = before,
        depth = depth,
        parentId = parent?.node?.id,
        index = index,
        nesting = previous != null && depth == previous.depth + 1,
    )
}
