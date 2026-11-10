package app.cascade.android.data

import java.util.UUID
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class OutlineStore(
    private val persistence: OutlinePersistence = MemoryPersistence(),
    private val now: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() },
) {
    private val nodes = HashMap<String, Node>()
    private var tree: Children? = null

    var version by mutableIntStateOf(0)
        private set
    var ready by mutableStateOf(false)
        private set

    private val listeners = ArrayList<() -> Unit>()

    val size: Int get() = nodes.size

    fun addListener(listener: () -> Unit): () -> Unit {
        listeners.add(listener)
        return { listeners.remove(listener) }
    }

    fun loaded(stored: List<Node>) {
        for (node in stored) {
            val current = nodes[node.id]
            if (current == null || current.updatedAt <= node.updatedAt) nodes[node.id] = node
        }
        val fixed = breakCycles(nodes).map { put(it.copy(updatedAt = now())) }
        if (fixed.isNotEmpty()) persistence.write(fixed, emptyList())
        ready = true
        changed()
    }

    fun get(id: String): Node? = nodes[id]

    fun rows(rootId: String? = null): List<Row> = children().rows(rootId)

    fun childrenOf(parentId: String?): List<Node> = children().of(parentId)

    fun childCount(id: String): Int = children().of(id).size

    fun hasChildren(id: String): Boolean = children().of(id).isNotEmpty()

    fun parentOf(id: String): String? = nodes[id]?.parentId

    fun ancestorsOf(id: String): List<Node> = ancestorsOf(nodes, id)

    fun search(query: String, limit: Int = 20, within: String? = null, excluding: String? = null): SearchResult {
        val candidates: Collection<Node> = when {
            within != null -> children().descendantsOf(within).filter { it != within }.mapNotNull(nodes::get)
            excluding != null -> children().descendantsOf(excluding).let { skip -> nodes.values.filter { it.id !in skip } }
            else -> nodes.values
        }
        return searchNodes(candidates, query, limit)
    }

    fun isLocked(id: String): Boolean = isDailyNode(id)

    fun create(parentId: String? = null, id: String = newId(), index: Int? = null, text: String = ""): String {
        if (parentId != null && parentId !in nodes && isDayId(parentId)) {
            openDay(this, dayDate(parentId))
        }
        require(parentId == null || parentId in nodes) { "create: unknown parent $parentId" }
        val node = put(
            Node(
                id = id,
                parentId = parentId,
                order = children().orderAt(parentId, index),
                text = text,
                updatedAt = now(),
            ),
        )
        commit(listOf(node))
        return node.id
    }

    fun setText(id: String, text: String) {
        val node = nodes[id] ?: return
        if (isLocked(id) || node.text == text) return
        commit(listOf(put(node.copy(text = text, updatedAt = now()))))
    }

    fun setCollapsed(id: String, collapsed: Boolean) {
        val node = nodes[id] ?: return
        commit(listOf(put(node.copy(collapsed = collapsed, updatedAt = now()))))
    }

    fun setTask(id: String, task: Task?) {
        val node = nodes[id] ?: return
        if (isLocked(id)) return
        commit(listOf(put(node.copy(task = task, updatedAt = now()))))
    }

    fun setDue(id: String, due: String?) {
        val node = nodes[id] ?: return
        if (isLocked(id)) return
        commit(listOf(put(node.copy(due = due, updatedAt = now()))))
    }

    fun dueOn(day: String, excluding: String? = null): List<Node> {
        val skip = excluding?.let { children().descendantsOf(it) } ?: emptySet()
        val due = nodes.values.filter { it.due == day && it.id !in skip }.mapTo(HashSet()) { it.id }
        return children().documentOrder(due).mapNotNull(nodes::get)
    }

    fun duplicate(id: String): String? {
        val node = nodes[id] ?: return null
        if (isLocked(id)) return null
        val tree = children()
        val copy = put(
            node.copy(
                id = newId(),
                order = tree.orderAt(node.parentId, tree.indexOf(node) + 1),
                collapsed = false,
                updatedAt = now(),
            ),
        )
        commit(listOf(copy))
        return copy.id
    }

    fun duplicateWithChildren(id: String): String? {
        val node = nodes[id] ?: return null
        if (isLocked(id)) return null
        val tree = children()
        val clones = ArrayList<Node>()

        fun clone(source: Node, parentId: String?, order: String): Node {
            val copy = put(source.copy(id = newId(), parentId = parentId, order = order, updatedAt = now()))
            clones.add(copy)
            var previous: String? = null
            for (child in tree.of(source.id)) {
                previous = FractionalIndex.between(previous, null)
                clone(child, copy.id, previous)
            }
            return copy
        }

        val root = clone(node, node.parentId, tree.orderAt(node.parentId, tree.indexOf(node) + 1))
        commit(clones)
        return root.id
    }

    fun canIndent(id: String): Boolean {
        val node = nodes[id] ?: return false
        return !isLocked(id) && children().indexOf(node) > 0
    }

    fun indent(id: String): Boolean {
        val node = nodes[id] ?: return false
        val siblings = children().of(node.parentId)
        val previous = siblings.getOrNull(siblings.indexOfFirst { it.id == id } - 1) ?: return false
        if (!move(id, previous.id)) return false
        if (previous.collapsed) setCollapsed(previous.id, false)
        return true
    }

    fun canOutdent(id: String): Boolean = !isLocked(id) && nodes[id]?.parentId != null

    fun outdent(id: String): Boolean {
        val node = nodes[id] ?: return false
        val parent = node.parentId?.let(nodes::get) ?: return false
        return move(id, parent.parentId, children().indexOf(parent) + 1)
    }

    fun move(id: String, newParentId: String?, index: Int? = null): Boolean {
        val node = nodes[id] ?: return false
        if (isLocked(id)) return false
        if (newParentId != null &&
            (newParentId == id || newParentId !in nodes || isDescendant(nodes, newParentId, id))
        ) {
            return false
        }
        val order = children().orderAt(newParentId, index, excluding = id)
        commit(listOf(put(node.copy(parentId = newParentId, order = order, updatedAt = now()))))
        return true
    }

    fun remove(id: String) = removeMany(listOf(id))

    fun removeMany(ids: Iterable<String>) {
        val tree = children()
        val all = LinkedHashSet<String>()
        for (id in ids) {
            if (id in nodes) all.addAll(tree.descendantsOf(id))
        }
        if (all.isEmpty()) return
        all.forEach(nodes::remove)
        commit(emptyList(), all.toList())
    }

    fun clearAll() {
        val ids = nodes.keys.toList()
        nodes.clear()
        commit(emptyList(), ids)
    }

    private fun children(): Children = tree ?: indexChildren(nodes.values).also { tree = it }

    private fun put(node: Node): Node {
        nodes[node.id] = node
        tree = null
        return node
    }

    private fun changed() {
        tree = null
        version++
        listeners.toList().forEach { it() }
    }

    private fun commit(put: List<Node>, delete: List<String> = emptyList()) {
        changed()
        persistence.write(put, delete)
    }
}
