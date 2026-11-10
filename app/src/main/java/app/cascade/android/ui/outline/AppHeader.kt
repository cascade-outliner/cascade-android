package app.cascade.android.ui.outline

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.cascade.android.R
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
import app.cascade.android.ui.components.AnchoredPopup
import app.cascade.android.ui.components.Calendar
import app.cascade.android.ui.components.CascadeIcon
import app.cascade.android.ui.components.LogoMark
import app.cascade.android.ui.components.SquareIconButton
import app.cascade.android.ui.theme.Cascade
import app.cascade.android.ui.theme.FontSize
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private const val MAX_VISIBLE_ANCESTORS = 3
private val PERIOD = Regex("""^daily-(\d{4})(?:-(\d{2}))?(-\d{2})?$""")

class StripItem(val name: String, val label: String, val marked: Boolean, val onPick: () -> Unit)

class Strip(val kind: String, val selected: Int, val items: List<StripItem>, val onShift: (Int) -> Unit)

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
            Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LogoMark(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
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
            SquareIconButton(R.drawable.ic_magnifying_glass, "Search", onSearch, bordered = true)
        }
        if (zoomed != null) {
            Breadcrumbs(store.ancestorsOf(zoomed.id), onZoomTo, Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp))
        }
        if (strip != null) PeriodStrip(strip)
        HorizontalDivider(color = colors.border)
    }
}

@Composable
private fun DaySwitcher(
    label: String,
    active: Boolean,
    date: LocalDate,
    marked: (LocalDate) -> Boolean,
    onToday: () -> Unit,
    onOlder: () -> Unit,
    onNewer: () -> Unit,
    onPick: (LocalDate) -> Unit,
) {
    val colors = Cascade.colors
    var open by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    Box {
        Row(
            Modifier.border(1.dp, colors.border, shape).padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SquareIconButton(R.drawable.ic_caret_left, "Previous day", onOlder, size = 32.dp, iconSize = 13.dp)
            Text(
                label,
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) colors.primaryMuted else Color.Transparent)
                    .clickable(role = Role.Button, onClick = onToday)
                    .semantics { selected = active }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                color = if (active) colors.primary else colors.ink,
                fontSize = FontSize.s300,
                maxLines = 1,
            )
            SquareIconButton(R.drawable.ic_caret_right, "Next day", onNewer, size = 32.dp, iconSize = 13.dp)
            SquareIconButton(R.drawable.ic_calendar_blank, "Pick a day", { open = true }, size = 32.dp, iconSize = 14.dp)
        }
        if (open) {
            AnchoredPopup(onDismiss = { open = false }, focusable = true, alignEnd = true) {
                Calendar(
                    value = date,
                    marked = marked,
                    onPick = {
                        open = false
                        onPick(it)
                    },
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
    }
}

@Composable
private fun Breadcrumbs(ancestors: List<Node>, onZoomTo: (String?) -> Unit, modifier: Modifier = Modifier) {
    val colors = Cascade.colors
    val visible = ancestors.takeLast(MAX_VISIBLE_ANCESTORS)
    val scroll = rememberScrollState()
    LaunchedEffect(ancestors.lastOrNull()?.id) { scroll.scrollTo(scroll.maxValue) }

    @Composable
    fun Separator() = CascadeIcon(R.drawable.ic_caret_right_bold, size = 10.dp, tint = colors.placeholder)

    Row(
        modifier.horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SquareIconButton(
            R.drawable.ic_house_simple_bold,
            "Zoom out to root",
            { onZoomTo(null) },
            size = 32.dp,
            iconSize = 13.dp,
        )
        if (ancestors.size > visible.size) {
            Separator()
            Text("…", Modifier.padding(horizontal = 6.dp), color = colors.muted, fontSize = FontSize.s300)
        }
        for (ancestor in visible) {
            Separator()
            Text(
                nodeLabel(ancestor),
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button) { onZoomTo(ancestor.id) }
                    .padding(horizontal = 8.dp, vertical = 7.dp)
                    .widthIn(max = 160.dp),
                color = colors.muted,
                fontSize = FontSize.s300,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PeriodStrip(strip: Strip) {
    val colors = Cascade.colors
    val position by animateFloatAsState(
        strip.selected.toFloat(),
        spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow),
        label = "period-strip",
    )
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SquareIconButton(R.drawable.ic_caret_left, "Previous", { strip.onShift(-1) }, size = 40.dp, iconSize = 14.dp)
        BoxWithConstraints(Modifier.weight(1f, fill = false).widthIn(max = 64.dp * strip.items.size).height(58.dp)) {
            val slot = maxWidth / strip.items.size
            Box(
                Modifier
                    .offset(x = slot * position)
                    .width(slot)
                    .fillMaxHeight()
                    .padding(horizontal = if (strip.items.size > 7) 1.dp else 3.dp)
                    .background(colors.primary, RoundedCornerShape(if (strip.items.size > 7) 12.dp else 20.dp)),
            )
            Row(Modifier.fillMaxWidth().fillMaxHeight()) {
                strip.items.forEachIndexed { index, item ->
                    val isSelected = index == strip.selected
                    val content = if (isSelected) colors.onPrimary else colors.ink
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(role = Role.Button, onClick = item.onPick)
                            .semantics(mergeDescendants = true) { selected = isSelected },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                    ) {
                        Text(
                            item.name,
                            color = content.copy(alpha = 0.7f),
                            fontSize = if (strip.items.size > 7) FontSize.s100 * 0.8f else FontSize.s100,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                        )
                        Text(
                            item.label,
                            color = content,
                            fontSize = if (strip.items.size > 7) FontSize.s200 else FontSize.s500,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                        Box(
                            Modifier
                                .size(4.dp)
                                .background(if (item.marked) content else Color.Transparent, CircleShape),
                        )
                    }
                }
            }
        }
        SquareIconButton(R.drawable.ic_caret_right, "Next", { strip.onShift(1) }, size = 40.dp, iconSize = 14.dp)
    }
}
