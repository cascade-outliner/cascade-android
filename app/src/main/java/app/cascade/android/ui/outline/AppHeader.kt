package app.cascade.android.ui.outline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.cascade.design.R
import app.cascade.android.data.Node
import app.cascade.android.data.OutlineStore
import app.cascade.android.data.dayDate
import app.cascade.android.data.dayId
import app.cascade.android.data.dayLabel
import app.cascade.android.data.monthId
import app.cascade.android.data.monthName
import app.cascade.android.data.openMonth
import app.cascade.android.data.openYear
import app.cascade.android.data.relativeDay
import app.cascade.android.data.shiftDay
import app.cascade.android.data.yearId
import app.cascade.design.LogoMark
import app.cascade.design.CascadeIconButton
import app.cascade.design.theme.Cascade
import app.cascade.design.outliner.Breadcrumbs
import app.cascade.design.outliner.Crumb
import app.cascade.design.outliner.DaySwitcher
import app.cascade.design.outliner.PeriodStrip
import app.cascade.design.outliner.Strip
import app.cascade.design.outliner.StripItem
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val PERIOD = Regex("""^daily-(\d{4})(?:-(\d{2}))?(-\d{2})?$""")

fun nodeLabel(node: Node, today: LocalDate = LocalDate.now()): String =
    relativeDay(node.id, today) ?: node.text.ifBlank { "Untitled" }

private fun periodStrip(store: OutlineStore, zoomedId: String?, onZoomTo: (String?) -> Unit): Strip? {
    val match = zoomedId?.let(PERIOD::matchEntire) ?: return null
    val year = match.groupValues[1].toInt()
    val month = match.groupValues[2].toIntOrNull()
    val isDay = match.groupValues[3].isNotEmpty()
    return when {
        isDay -> {
            if (!app.cascade.android.data.isDayId(zoomedId)) return null
            val date = dayDate(zoomedId)
            val index = date.dayOfWeek.value - 1
            Strip(
                kind = "day",
                selected = index,
                items = (0 until 7).map { i ->
                    val day = date.plusDays((i - index).toLong())
                    StripItem(
                        name = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US),
                        label = day.dayOfMonth.toString(),
                        marked = store.hasChildren(dayId(day)),
                        onPick = { onZoomTo(dayId(day)) },
                    )
                },
                onShift = { by -> onZoomTo(dayId(shiftDay(zoomedId, 7L * by))) },
            )
        }
        month != null -> {
            if (month !in 1..12) return null
            Strip(
                kind = "month",
                selected = month - 1,
                items = (1..12).map { m ->
                    StripItem(
                        name = year.toString(),
                        label = monthName(m, TextStyle.SHORT),
                        marked = store.hasChildren(monthId(year, m)),
                        onPick = { onZoomTo(openMonth(store, year, m)) },
                    )
                },
                onShift = { by -> onZoomTo(openMonth(store, year + by, month)) },
            )
        }
        else -> Strip(
            kind = "year",
            selected = 3,
            items = (0 until 7).map { i ->
                val y = year - 3 + i
                StripItem(
                    name = "Year",
                    label = y.toString(),
                    marked = store.hasChildren(yearId(y)),
                    onPick = { onZoomTo(openYear(store, y)) },
                )
            },
            onShift = { by -> onZoomTo(openYear(store, year + 7 * by)) },
        )
    }
}

@Composable
fun AppHeader(
    store: OutlineStore,
    zoomedId: String?,
    onZoomTo: (String?) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Cascade.colors
    store.version
    val today = LocalDate.now()
    val zoomed = zoomedId?.let(store::get)
    val label = zoomedId?.let { dayLabel(it, today) }
    val strip = periodStrip(store, zoomedId, onZoomTo)

    Column(modifier.fillMaxWidth().background(colors.canvas)) {
        Row(
            Modifier.padding(horizontal = Space.x4, vertical = Space.x2_5),
            horizontalArrangement = Arrangement.spacedBy(Space.x3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LogoMark(
                Modifier
                    .clip(Radius.sm)
                    .clickable(role = Role.Button) { onZoomTo(null) }
                    .semantics { contentDescription = "Cascade home" },
                size = 32.dp,
            )
            Spacer(Modifier.weight(1f))
            DaySwitcher(
                label = label ?: "Today",
                active = zoomedId == dayId(today),
                date = if (zoomedId != null && label != null) dayDate(zoomedId) else today,
                marked = { store.hasChildren(dayId(it)) },
                onToday = { onZoomTo(dayId(today)) },
                onOlder = { onZoomTo(dayId(shiftDay(zoomedId, -1, today))) },
                onNewer = { onZoomTo(dayId(shiftDay(zoomedId, 1, today))) },
                onPick = { onZoomTo(dayId(it)) },
            )
            CascadeIconButton(R.drawable.ic_magnifying_glass, "Search", onSearch, bordered = true)
        }
        if (zoomed != null) {
            Breadcrumbs(
                crumbs = store.ancestorsOf(zoomed.id).map { Crumb(it.id, nodeLabel(it, today)) },
                onHome = { onZoomTo(null) },
                onCrumb = onZoomTo,
                modifier = Modifier.padding(start = Space.x3, end = Space.x3, bottom = Space.x2),
            )
        }
        if (strip != null) PeriodStrip(strip)
        HorizontalDivider(color = colors.border)
    }
}
