package app.cascade.android.data

interface OutlinePersistence {
    fun load(): List<Node>

    fun write(put: List<Node>, delete: List<String>)
}

class MemoryPersistence(initial: List<Node> = emptyList()) : OutlinePersistence {
    val nodes = LinkedHashMap<String, Node>().apply { initial.forEach { put(it.id, it) } }

    override fun load(): List<Node> = nodes.values.toList()

    override fun write(put: List<Node>, delete: List<String>) {
        put.forEach { nodes[it.id] = it }
        delete.forEach(nodes::remove)
    }
}
