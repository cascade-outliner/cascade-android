package app.cascade.android.data

data class Task(val done: Boolean)

data class Node(
    val id: String,
    val parentId: String?,
    val order: String,
    val text: String,
    val collapsed: Boolean = false,
    val task: Task? = null,
    val due: String? = null,
    val updatedAt: Long = 0,
)

data class Row(
    val node: Node,
    val depth: Int,
    val childCount: Int,
)
