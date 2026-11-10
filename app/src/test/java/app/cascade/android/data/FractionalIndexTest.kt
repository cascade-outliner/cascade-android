package app.cascade.android.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FractionalIndexTest {
    @Test
    fun matchesTheWebAppsKeys() {
        val lines = checkNotNull(javaClass.getResourceAsStream("/fractional-index-vectors.csv"))
            .bufferedReader().readLines().filter { it.isNotBlank() }
        assertTrue(lines.size > 500)
        for (line in lines) {
            val (a, b, expected) = line.split(",")
            val before = a.ifEmpty { null }
            val after = b.ifEmpty { null }
            if (expected == "!") {
                assertThrows(line, IllegalArgumentException::class.java) { FractionalIndex.between(before, after) }
            } else {
                assertEquals(line, expected, FractionalIndex.between(before, after))
            }
        }
    }

    @Test
    fun rejectsKeysOutOfOrder() {
        assertThrows(IllegalArgumentException::class.java) { FractionalIndex.between("a1", "a0") }
        assertThrows(IllegalArgumentException::class.java) { FractionalIndex.between("a0", "a0") }
    }
}
