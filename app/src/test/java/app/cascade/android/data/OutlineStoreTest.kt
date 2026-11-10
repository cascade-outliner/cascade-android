package app.cascade.android.data

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OutlineStoreTest {
    private val persistence = MemoryPersistence()
    private var clock = 0L
    private var ids = 0
    private val store = OutlineStore(persistence, now = { ++clock }, newId = { "n${++ids}" }).apply { loaded(emptyList()) }

    private fun outline(rootId: String? = null) =
        store.rows(rootId).joinToString("\n") { "  ".repeat(it.depth) + it.node.text }

    @Test
    fun newNodesAppendInOrder() {
        store.create(text = "a")
        store.create(text = "b")
        store.create(text = "first", index = 0)
        assertEquals("first\na\nb", outline())
    }

    @Test
    fun indentNestsUnderThePreviousSiblingAndOutdentUndoesIt() {
        val a = store.create(text = "a")
        val b = store.create(text = "b")
        val c = store.create(text = "c")
        assertFalse(store.canIndent(a))
        assertFalse(store.canOutdent(a))
        assertTrue(store.indent(b))
        assertEquals("a\n  b\nc", outline())
        assertEquals(1, store.rows().first().childCount)
        assertTrue(store.outdent(b))
        assertEquals("a\nb\nc", outline())
        assertTrue(store.indent(c))
        assertTrue(store.indent(c).not())
    }

    @Test
    fun outdentLandsRightAfterTheFormerParent() {
        val a = store.create(text = "a")
        store.create(text = "z")
        store.create(a, text = "a1")
        val a2 = store.create(a, text = "a2")
        store.outdent(a2)
        assertEquals("a\n  a1\na2\nz", outline())
    }

    @Test
    fun collapsedChildrenAreHiddenButCounted() {
        val a = store.create(text = "a")
        store.create(a, text = "a1")
        store.setCollapsed(a, true)
        assertEquals("a", outline())
        assertEquals(1, store.rows().single().childCount)
    }

    @Test
    fun moveRefusesToNestANodeUnderItself() {
        val a = store.create(text = "a")
        val a1 = store.create(a, text = "a1")
        val a11 = store.create(a1, text = "a11")
        assertFalse(store.move(a, a11))
        assertFalse(store.move(a, a))
        assertFalse(store.move(a, "missing"))
        assertTrue(store.move(a11, null, 0))
        assertEquals("a11\na\n  a1", outline())
    }

    @Test
    fun moveWithinTheSameParentIgnoresItsOwnSlot() {
        val a = store.create(text = "a")
        store.create(text = "b")
        store.create(text = "c")
        store.move(a, null, 2)
        assertEquals("b\nc\na", outline())
        store.move(a, null, 0)
        assertEquals("a\nb\nc", outline())
    }

    @Test
    fun duplicateCopiesTheNodeRightAfterIt() {
        val a = store.create(text = "a")
        store.create(a, text = "a1")
        store.create(text = "b")
        store.setTask(a, Task(done = true))
        store.setDue(a, "2026-11-09")
        val copy = checkNotNull(store.duplicate(a))
        assertEquals("a\n  a1\na\nb", outline())
        assertEquals(Task(done = true), store.get(copy)?.task)
        assertEquals("2026-11-09", store.get(copy)?.due)
    }

    @Test
    fun duplicateWithChildrenCopiesTheSubtree() {
        val a = store.create(text = "a")
        val a1 = store.create(a, text = "a1")
        store.create(a1, text = "a11")
        store.create(a, text = "a2")
        store.create(text = "b")
        store.duplicateWithChildren(a)
        assertEquals("a\n  a1\n    a11\n  a2\na\n  a1\n    a11\n  a2\nb", outline())
    }

    @Test
    fun removeTakesDescendantsAlong() {
        val a = store.create(text = "a")
        val a1 = store.create(a, text = "a1")
        store.create(a1, text = "a11")
        store.create(text = "b")
        store.remove(a)
        assertEquals("b", outline())
        assertEquals(1, store.size)
        assertEquals(listOf("b"), persistence.nodes.values.map { it.text })
    }

    @Test
    fun everyChangeIsPersistedAndLoadsBack() {
        val a = store.create(text = "a")
        val b = store.create(text = "b")
        store.indent(b)
        store.setText(b, "bee")
        store.setTask(b, Task(done = false))
        store.setDue(b, "2026-01-02")

        val reloaded = OutlineStore(persistence).apply { loaded(persistence.load()) }
        assertEquals("a\n  bee", reloaded.rows().joinToString("\n") { "  ".repeat(it.depth) + it.node.text })
        assertEquals(store.get(b), reloaded.get(b))
        assertEquals(a, reloaded.parentOf(b))
    }

    @Test
    fun loadingBreaksParentCycles() {
        val cyclic = OutlineStore(persistence)
        cyclic.loaded(
            listOf(
                Node("x", "y", "a0", "x"),
                Node("y", "x", "a0", "y"),
                Node("z", null, "a1", "z"),
            ),
        )
        assertEquals(3, cyclic.rows().size)
    }

    @Test
    fun dailyNodesAreFixed() {
        val day = openDay(store, LocalDate.of(2026, 9, 27))
        assertTrue(store.isLocked(day))
        store.setText(day, "renamed")
        store.setTask(day, Task(done = false))
        store.setDue(day, "2026-01-01")
        assertFalse(store.move(day, null))
        assertNull(store.duplicate(day))
        assertEquals(Node(day, "daily-2026-09", store.get(day)!!.order, "Sunday, 27th", updatedAt = store.get(day)!!.updatedAt), store.get(day))
        store.remove(day)
        assertNull(store.get(day))
    }

    @Test
    fun aDayNoteAppearsWithItsFirstChild() {
        val id = dayId(LocalDate.of(2026, 9, 27))
        assertNull(store.get(id))
        store.create(id, text = "wrote something")
        assertEquals("Daily nodes\n  2026\n    September\n      Sunday, 27th\n        wrote something", outline())
    }

    @Test
    fun dueOnListsInOutlineOrderAndSkipsTheExcludedSubtree() {
        val a = store.create(text = "a")
        val a1 = store.create(a, text = "a1")
        val b = store.create(text = "b")
        val today = store.create(text = "today")
        val inside = store.create(today, text = "inside")
        for (id in listOf(b, a1, inside)) store.setDue(id, "2026-11-09")
        store.setDue(a, "2026-11-10")
        assertEquals(listOf("a1", "b"), store.dueOn("2026-11-09", excluding = today).map { it.text })
        assertEquals(listOf("a1", "b", "inside"), store.dueOn("2026-11-09").map { it.text })
    }

    @Test
    fun searchCanStayWithinOrOutsideANode() {
        val a = store.create(text = "Groceries")
        store.create(a, text = "Buy milk")
        store.create(text = "Milk the cow")
        assertEquals(listOf("Buy milk"), store.search("milk", within = a).hits.map { it.node.text })
        assertEquals(listOf("Milk the cow"), store.search("milk", excluding = a).hits.map { it.node.text })
        assertEquals(2, store.search("milk").total)
        assertNotNull(store.search("").hits)
        assertEquals(0, store.search("  ").total)
    }

    @Test
    fun versionMovesWithEveryChange() {
        val before = store.version
        val a = store.create(text = "a")
        store.setText(a, "a")
        assertEquals(before + 1, store.version)
        store.setText(a, "b")
        assertEquals(before + 2, store.version)
    }
}
