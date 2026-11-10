package app.cascade.design

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SlashMenuItemsTest {
    private data class Item(
        override val id: String,
        override val group: String,
        override val label: String,
        override val keywords: List<String> = emptyList(),
        override val icon: Int = 0,
    ) : SlashMenuItem

    private val items = listOf(
        Item("task", "Convert", "Turn into task", listOf("todo")),
        Item("text", "Convert", "Turn into text"),
        Item("due-today", "Due date", "Due today", listOf("date")),
        Item("remove-due", "Due date", "Remove due date"),
        Item("duplicate", "Organize", "Duplicate", listOf("copy")),
        Item("delete", "Node", "Delete", listOf("remove")),
    )

    @Test
    fun triggerNeedsAStartOrASpaceBeforeIt() {
        assertEquals(SlashTrigger("", 0), matchSlashTrigger("/"))
        assertEquals(SlashTrigger("ta", 9), matchSlashTrigger("Buy milk /ta"))
        assertNull(matchSlashTrigger("a/b"))
        assertNull(matchSlashTrigger("/due tomorrow"))
        assertNull(matchSlashTrigger("Buy milk"))
    }

    @Test
    fun filterMatchesLabelsWordsAndKeywords() {
        assertEquals(items, filterSlashMenuItems(items, " "))
        assertEquals(listOf("task", "text"), filterSlashMenuItems(items, "turn").map { it.id })
        assertEquals(listOf("task"), filterSlashMenuItems(items, "todo").map { it.id })
        assertEquals(listOf("due-today", "remove-due", "duplicate"), filterSlashMenuItems(items, "du").map { it.id })
        assertEquals(listOf("remove-due", "delete"), filterSlashMenuItems(items, "rem").map { it.id })
        assertTrue(filterSlashMenuItems(items, "zzz").isEmpty())
    }

    @Test
    fun groupsStayTogetherInOrder() {
        assertEquals(listOf("Convert", "Due date", "Organize", "Node"), groupSlashMenuItems(items).map { it.first })
    }
}
