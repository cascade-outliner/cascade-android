package app.cascade.android.data

typealias Children = Map<String?, List<Node>>

fun indexChildren(nodes: Collection<Node>): Children =
    nodes.groupBy { it.parentId }.mapValues { (_, siblings) -> siblings.sortedBy { it.order } }

fun Children.of(parentId: String?): List<Node> = this[parentId] ?: emptyList()

fun Children.indexOf(node: Node): Int = of(node.parentId).indexOfFirst { it.id == node.id }

fun Children.orderAt(parentId: String?, index: Int? = null, excluding: String? = null): String {
    val siblings = of(parentId).filter { it.id != excluding }
    val at = index?.coerceIn(0, siblings.size) ?: siblings.size
    return FractionalIndex.between(siblings.getOrNull(at - 1)?.order, siblings.getOrNull(at)?.order)
}

fun isDescendant(nodes: Map<String, Node>, id: String, ancestorId: String): Boolean {
    val seen = HashSet<String>()
    var current = nodes[id]?.parentId
    while (current != null && seen.add(current)) {
        if (current == ancestorId) return true
        current = nodes[current]?.parentId
    }
    return false
}

fun ancestorsOf(nodes: Map<String, Node>, id: String): List<Node> {
    val chain = ArrayList<Node>()
    val seen = HashSet<String>()
    var current = nodes[id]?.parentId
    while (current != null && seen.add(current)) {
        val node = nodes[current] ?: break
        chain.add(node)
        current = node.parentId
    }
    return chain.asReversed()
}

fun Children.descendantsOf(id: String): Set<String> {
    val collected = LinkedHashSet<String>()
    val stack = ArrayDeque(listOf(id))
    while (stack.isNotEmpty()) {
        val current = stack.removeLast()
        if (!collected.add(current)) continue
        of(current).forEach { stack.addLast(it.id) }
    }
    return collected
}

fun Children.rows(rootId: String?): List<Row> {
    val rows = ArrayList<Row>()
    val seen = HashSet<String>()
    rootId?.let(seen::add)
    fun walk(parentId: String?, depth: Int) {
        for (node in of(parentId)) {
            if (!seen.add(node.id)) continue
            val childCount = of(node.id).size
            rows.add(Row(node, depth, childCount))
            if (childCount > 0 && !node.collapsed) walk(node.id, depth + 1)
        }
    }
    walk(rootId, 0)
    return rows
}

fun Children.documentOrder(ids: Set<String>): List<String> {
    val ordered = ArrayList<String>()
    val seen = HashSet<String>()
    fun walk(parentId: String?) {
        for (child in of(parentId)) {
            if (!seen.add(child.id)) continue
            if (child.id in ids) ordered.add(child.id)
            walk(child.id)
        }
    }
    walk(null)
    return ordered
}

fun breakCycles(nodes: MutableMap<String, Node>): List<Node> {
    val fixed = ArrayList<Node>()
    val acyclic = HashSet<String>()
    for (start in nodes.values.toList()) {
        val chain = LinkedHashSet<String>()
        var node: Node? = nodes[start.id]
        while (node != null && node.id !in acyclic) {
            if (!chain.add(node.id)) {
                val detached = node.copy(parentId = null)
                nodes[detached.id] = detached
                fixed.add(detached)
                break
            }
            node = node.parentId?.let(nodes::get)
        }
        acyclic.addAll(chain)
    }
    return fixed
}
