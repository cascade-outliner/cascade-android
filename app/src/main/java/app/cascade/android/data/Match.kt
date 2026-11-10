package app.cascade.android.data

import java.text.Normalizer
import java.util.Locale

data class Match(val score: Double, val ranges: List<IntRange>)

class Folded(val text: String, val map: IntArray)

private val MARKS = Regex("""\p{M}""")
private val WHITESPACE = Regex("""\s+""")

fun fold(text: String): Folded {
    val folded = StringBuilder()
    val map = ArrayList<Int>(text.length + 1)
    var offset = 0
    while (offset < text.length) {
        val codePoint = text.codePointAt(offset)
        val char = String(Character.toChars(codePoint))
        val piece = Normalizer.normalize(char, Normalizer.Form.NFD).replace(MARKS, "").lowercase(Locale.ROOT)
        folded.append(piece)
        repeat(piece.length) { map.add(offset) }
        offset += char.length
    }
    map.add(text.length)
    return Folded(folded.toString(), map.toIntArray())
}

private fun unfold(folded: Folded, start: Int, end: Int): IntRange {
    val map = folded.map
    var after = end
    while (after < map.size - 1 && map[after] == map[end - 1]) after++
    return map[start] until map[after]
}

private fun isWordStart(text: String, index: Int) = index == 0 || !text[index - 1].isLetterOrDigit()

private fun merge(ranges: List<IntRange>): List<IntRange> {
    val merged = ArrayList<IntRange>()
    for (range in ranges.sortedBy { it.first }) {
        val last = merged.lastOrNull()
        if (last != null && range.first <= last.last + 1) {
            merged[merged.size - 1] = last.first..maxOf(last.last, range.last)
        } else {
            merged.add(range)
        }
    }
    return merged
}

fun terms(query: String): List<String> = fold(query).text.split(WHITESPACE).filter { it.isNotEmpty() }.distinct()

fun matchTerms(queryTerms: List<String>, text: Folded): Match? {
    if (queryTerms.isEmpty()) return null
    var score = 0.0
    val ranges = ArrayList<IntRange>()
    for (term in queryTerms) {
        var best = -1
        var at = text.text.indexOf(term)
        while (at != -1) {
            if (best == -1) best = at
            if (isWordStart(text.text, at)) {
                best = at
                break
            }
            at = text.text.indexOf(term, at + 1)
        }
        if (best == -1) return null
        val end = best + term.length
        val wordStart = isWordStart(text.text, best)
        val wordEnd = end == text.text.length || !text.text[end].isLetterOrDigit()
        score += 1 + (if (wordStart) 2 else 0) + (if (wordStart && wordEnd) 1 else 0)
        score -= minOf(best, 999) / 1000.0
        ranges.add(unfold(text, best, end))
    }
    return Match(score, merge(ranges))
}

data class SearchHit(val node: Node, val score: Double, val ranges: List<IntRange>)

data class SearchResult(val hits: List<SearchHit>, val total: Int)

fun searchNodes(nodes: Iterable<Node>, query: String, limit: Int): SearchResult {
    val queryTerms = terms(query)
    val hits = ArrayList<SearchHit>()
    if (queryTerms.isNotEmpty()) {
        for (node in nodes) {
            val match = matchTerms(queryTerms, fold(node.text)) ?: continue
            hits.add(SearchHit(node, match.score, match.ranges))
        }
    }
    hits.sortWith(
        compareByDescending<SearchHit> { it.score }
            .thenBy { it.node.text.length }
            .thenByDescending { it.node.updatedAt },
    )
    return SearchResult(hits.take(limit), hits.size)
}
