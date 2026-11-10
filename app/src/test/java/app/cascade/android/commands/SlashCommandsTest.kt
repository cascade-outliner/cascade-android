package app.cascade.android.commands

import app.cascade.android.data.OutlineStore
import app.cascade.android.data.Task
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SlashCommandsTest {
    private val store = OutlineStore().apply { loaded(emptyList()) }
    private val today = LocalDate.of(2026, 11, 9)
    private var zoomed: String? = null
    private var copied: String? = null

    private fun context(id: String) = SlashContext(
        store = store,
        node = store.get(id)!!,
        childCount = store.childCount(id),
        zoomTo = { zoomed = it },
        copyLink = { copied = nodeLink(it) },
        today = today,
    )

    private fun run(commandId: String, id: String) = slashCommands.first { it.id == commandId }.run(context(id))

    private fun available(id: String) = slashCommands.filter { it.available(context(id)) }.map { it.id }

    @Test
    fun idsAreUnique() {
        assertEquals(slashCommands.size, slashCommands.map { it.id }.toSet().size)
    }

    @Test
    fun captureOffersOnlyWhatMakesSenseOnANewNode() {
        assertEquals(
            listOf("turn-into-task", "due-today", "due-tomorrow", "due-next-week", "zoom-in"),
            captureSlashCommands.map { it.id },
        )
    }

    @Test
    fun onlyCommandsThatApplyAreOffered() {
        val a = store.create(text = "a")
        val b = store.create(text = "b")
        assertEquals(
            listOf("turn-into-task", "due-today", "due-tomorrow", "due-next-week", "duplicate", "zoom-in", "copy-link", "delete"),
            available(a),
        )
        run("turn-into-task", b)
        run("due-tomorrow", b)
        run("indent", b)
        assertEquals(Task(done = false), store.get(b)?.task)
        assertEquals("2026-11-10", store.get(b)?.due)
        assertEquals(a, store.parentOf(b))
        assertTrue(available(b).containsAll(listOf("turn-into-text", "complete-task", "remove-due", "outdent")))
        assertTrue("duplicate-with-children" in available(a))
        run("complete-task", b)
        assertTrue("reopen-task" in available(b))
    }

    @Test
    fun nodeCommandsZoomCopyAndDelete() {
        val a = store.create(text = "a")
        run("zoom-in", a)
        assertEquals(a, zoomed)
        run("copy-link", a)
        assertEquals("cascade://node/$a", copied)
        run("delete", a)
        assertNull(store.get(a))
    }
}
