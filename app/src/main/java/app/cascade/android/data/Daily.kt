package app.cascade.android.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

const val DAILY_ROOT = "daily"
private const val PREFIX = "$DAILY_ROOT-"
private val DAY = Regex("""^daily-\d{4}-\d{2}-\d{2}$""")
private val SHORT_DATE = DateTimeFormatter.ofPattern("MMM d", Locale.US)

private fun pad(n: Int) = n.toString().padStart(2, '0')

fun isoDay(date: LocalDate): String = "${date.year.toString().padStart(4, '0')}-${pad(date.monthValue)}-${pad(date.dayOfMonth)}"

fun fromIsoDay(day: String): LocalDate? = runCatching { LocalDate.parse(day) }.getOrNull()

fun dayId(date: LocalDate): String = PREFIX + isoDay(date)

fun isDayId(id: String): Boolean = DAY.matches(id) && fromIsoDay(id.substring(PREFIX.length)) != null

fun isDailyNode(id: String): Boolean = id == DAILY_ROOT || id.startsWith(PREFIX)

fun dayDate(id: String): LocalDate = LocalDate.parse(id.substring(PREFIX.length))

fun yearId(year: Int): String = "$PREFIX$year"

fun monthId(year: Int, month: Int): String = "${yearId(year)}-${pad(month)}"

fun monthName(month: Int, style: TextStyle = TextStyle.FULL): String =
    java.time.Month.of(month).getDisplayName(style, Locale.US)

private fun ensure(store: OutlineStore, id: String, parentId: String?, text: String): String {
    if (store.get(id) != null) return id
    val siblings = store.childrenOf(parentId)
    var index = siblings.size
    for ((at, node) in siblings.withIndex()) {
        if (!isDailyNode(node.id)) continue
        if (node.id > id) {
            index = at
            break
        }
        index = at + 1
    }
    return store.create(parentId, id = id, index = index, text = text)
}

fun openYear(store: OutlineStore, year: Int): String {
    ensure(store, DAILY_ROOT, null, "Daily nodes")
    return ensure(store, yearId(year), DAILY_ROOT, year.toString())
}

fun openMonth(store: OutlineStore, year: Int, month: Int): String {
    openYear(store, year)
    return ensure(store, monthId(year, month), yearId(year), monthName(month))
}

fun openDay(store: OutlineStore, date: LocalDate): String {
    openMonth(store, date.year, date.monthValue)
    return ensure(store, dayId(date), monthId(date.year, date.monthValue), dayTitle(date))
}

fun dayTitle(date: LocalDate): String {
    val day = date.dayOfMonth
    val suffix = when {
        day % 100 in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
    return "${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)}, $day$suffix"
}

fun relativeDay(id: String, today: LocalDate = LocalDate.now()): String? {
    if (!isDayId(id)) return null
    return when (id) {
        dayId(today.minusDays(1)) -> "Yesterday"
        dayId(today) -> "Today"
        dayId(today.plusDays(1)) -> "Tomorrow"
        else -> null
    }
}

fun dayLabel(id: String, today: LocalDate = LocalDate.now()): String? {
    if (!isDayId(id)) return null
    return relativeDay(id, today) ?: dayDate(id).format(SHORT_DATE)
}

fun shiftDay(id: String?, by: Long, today: LocalDate = LocalDate.now()): LocalDate {
    val base = if (id != null && isDayId(id)) dayDate(id) else today
    return base.plusDays(by)
}

fun dueLabel(due: String, today: LocalDate = LocalDate.now()): String = dayLabel(PREFIX + due, today) ?: due
