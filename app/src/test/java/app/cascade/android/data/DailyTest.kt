package app.cascade.android.data

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyTest {
    private val store = OutlineStore().apply { loaded(emptyList()) }
    private val today = LocalDate.of(2026, 9, 28)

    @Test
    fun idsFollowTheWebApp() {
        assertEquals("daily-2026-09-07", dayId(LocalDate.of(2026, 9, 7)))
        assertEquals("daily-2026-09", monthId(2026, 9))
        assertEquals("daily-2026", yearId(2026))
        assertTrue(isDayId("daily-2026-09-07"))
        assertFalse(isDayId("daily-2026-09"))
        assertFalse(isDayId("daily-2026-13-45"))
        assertTrue(isDailyNode("daily"))
        assertTrue(isDailyNode("daily-2026"))
        assertFalse(isDailyNode("dailyish"))
    }

    @Test
    fun titlesUseOrdinals() {
        assertEquals("Monday, 28th", dayTitle(today))
        assertEquals("Tuesday, 1st", dayTitle(LocalDate.of(2026, 9, 1)))
        assertEquals("Wednesday, 2nd", dayTitle(LocalDate.of(2026, 9, 2)))
        assertEquals("Thursday, 3rd", dayTitle(LocalDate.of(2026, 9, 3)))
        assertEquals("Friday, 11th", dayTitle(LocalDate.of(2026, 9, 11)))
        assertEquals("Saturday, 12th", dayTitle(LocalDate.of(2026, 9, 12)))
        assertEquals("Sunday, 13th", dayTitle(LocalDate.of(2026, 9, 13)))
        assertEquals("Tuesday, 22nd", dayTitle(LocalDate.of(2026, 9, 22)))
    }

    @Test
    fun labelsAreRelativeAroundToday() {
        assertEquals("Today", relativeDay(dayId(today), today))
        assertEquals("Yesterday", relativeDay(dayId(today.minusDays(1)), today))
        assertEquals("Tomorrow", relativeDay(dayId(today.plusDays(1)), today))
        assertNull(relativeDay(dayId(today.plusDays(2)), today))
        assertNull(relativeDay("daily-2026", today))
        assertEquals("Sep 30", dayLabel(dayId(today.plusDays(2)), today))
        assertEquals("Today", dueLabel("2026-09-28", today))
        assertEquals("Oct 5", dueLabel("2026-10-05", today))
        assertEquals("someday", dueLabel("someday", today))
    }

    @Test
    fun shiftDayStepsFromTheNoteOrFromToday() {
        assertEquals(LocalDate.of(2026, 10, 1), shiftDay("daily-2026-09-30", 1, today))
        assertEquals(today.minusDays(1), shiftDay(null, -1, today))
        assertEquals(today.plusDays(7), shiftDay("not-a-day", 7, today))
    }

    @Test
    fun daysAreCreatedInCalendarOrderAtEveryLevel() {
        openDay(store, LocalDate.of(2026, 9, 27))
        openDay(store, LocalDate.of(2025, 12, 31))
        openDay(store, LocalDate.of(2026, 9, 3))
        openDay(store, LocalDate.of(2026, 2, 14))
        store.create(DAILY_ROOT, text = "loose note")
        openDay(store, LocalDate.of(2027, 1, 1))
        assertEquals(
            listOf(
                "Daily nodes",
                " 2025", "  December", "   Wednesday, 31st",
                " 2026", "  February", "   Saturday, 14th", "  September", "   Thursday, 3rd", "   Sunday, 27th",
                " 2027", "  January", "   Friday, 1st",
                " loose note",
            ),
            store.rows().map { " ".repeat(it.depth) + it.node.text },
        )
    }

    @Test
    fun openingTwiceReusesTheNote() {
        val first = openDay(store, today)
        val size = store.size
        assertEquals(first, openDay(store, today))
        assertEquals(size, store.size)
    }
}
