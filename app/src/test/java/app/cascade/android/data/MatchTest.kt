package app.cascade.android.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchTest {
    private fun node(text: String, updatedAt: Long = 0) = Node(text, null, "a0", text, updatedAt = updatedAt)

    @Test
    fun foldingIgnoresCaseAndAccents() {
        val hit = searchNodes(listOf(node("Un Café crème")), "cafe", 5).hits.single()
        assertEquals(listOf(3..6), hit.ranges)
    }

    @Test
    fun everyTermMustMatch() {
        val nodes = listOf(node("Buy milk"), node("Buy eggs"))
        assertEquals(listOf("Buy milk"), searchNodes(nodes, "buy mi", 5).hits.map { it.node.text })
        assertEquals(0, searchNodes(nodes, "buy bread", 5).total)
    }

    @Test
    fun wordStartsAndWholeWordsRankFirstThenShorterText() {
        val nodes = listOf(node("remilk"), node("milkshake recipe"), node("milk"), node("the milk run"))
        assertEquals(
            listOf("milk", "the milk run", "milkshake recipe", "remilk"),
            searchNodes(nodes, "milk", 10).hits.map { it.node.text },
        )
    }

    @Test
    fun tiesGoToTheMostRecentlyEdited() {
        val nodes = listOf(node("milk a", 1), node("milk b", 2))
        assertEquals(listOf("milk b", "milk a"), searchNodes(nodes, "milk", 10).hits.map { it.node.text })
    }

    @Test
    fun limitKeepsTheTotal() {
        val result = searchNodes((1..30).map { node("milk $it") }, "milk", 8)
        assertEquals(8, result.hits.size)
        assertEquals(30, result.total)
    }

    @Test
    fun rangesMergeAndSurviveSurrogatePairs() {
        val folded = fold("😀 Ångström")
        val match = checkNotNull(matchTerms(terms("ang strom"), folded))
        assertEquals(listOf(3..10), match.ranges)
        assertEquals(listOf(3..5, 7..11), matchTerms(terms("ang strom"), fold("😀 Ång ström"))?.ranges)
        assertNull(matchTerms(emptyList(), folded))
        assertTrue(terms("  A a  b ").let { it == listOf("a", "b") })
    }
}
