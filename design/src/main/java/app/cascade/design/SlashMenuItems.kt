package app.cascade.design

import androidx.annotation.DrawableRes

interface SlashMenuItem {
    val id: String
    val label: String
    val group: String
    val keywords: List<String> get() = emptyList()

    @get:DrawableRes
    val icon: Int
    val danger: Boolean get() = false
}

private val WHITESPACE = Regex("""\s+""")

private fun rankOf(item: SlashMenuItem, query: String): Int? {
    val label = item.label.lowercase()
    if (label.startsWith(query)) return 0
    val words = label.split(WHITESPACE) + item.keywords
    return if (words.any { it.lowercase().startsWith(query) }) 1 else null
}

fun <T : SlashMenuItem> filterSlashMenuItems(items: List<T>, query: String): List<T> {
    val needle = query.trim().lowercase()
    if (needle.isEmpty()) return items
    val ranked = items.mapIndexedNotNull { index, item -> rankOf(item, needle)?.let { Triple(item, it, index) } }
    val groupRank = LinkedHashMap<String, Int>()
    for ((item, rank) in ranked) {
        groupRank[item.group] = minOf(rank, groupRank[item.group] ?: Int.MAX_VALUE)
    }
    val groupOrder = groupRank.keys.toList()
    return ranked.sortedWith(
        compareBy<Triple<T, Int, Int>> { groupRank.getValue(it.first.group) }
            .thenBy { groupOrder.indexOf(it.first.group) }
            .thenBy { it.second }
            .thenBy { it.third },
    ).map { it.first }
}

fun <T : SlashMenuItem> groupSlashMenuItems(items: List<T>): List<Pair<String, List<T>>> {
    val groups = ArrayList<Pair<String, MutableList<T>>>()
    for (item in items) {
        val last = groups.lastOrNull()
        if (last?.first == item.group) last.second.add(item) else groups.add(item.group to mutableListOf(item))
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
