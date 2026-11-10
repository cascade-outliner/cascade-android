package app.cascade.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.FontSize
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

private val MONTH_TITLE = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)
private val DAY_DESCRIPTION = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US)
private val CELL = 38.dp

@Composable
fun Calendar(
    value: LocalDate?,
    onPick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    marked: (LocalDate) -> Boolean = { false },
) {
    val colors = Cascade.colors
    var month by remember(value) { mutableStateOf(YearMonth.from(value ?: today)) }
    val firstDay = remember { WeekFields.of(Locale.getDefault()).firstDayOfWeek }
    val weekdays = remember(firstDay) { (0L..6L).map { firstDay.plus(it) } }
    val lead = (month.atDay(1).dayOfWeek.value - firstDay.value + 7) % 7
    val weeks = (lead + month.lengthOfMonth() + 6) / 7

    Column(modifier.width(CELL * 7), verticalArrangement = Arrangement.spacedBy(Space.x0_5)) {
        Row(Modifier.padding(bottom = Space.x1_5), verticalAlignment = Alignment.CenterVertically) {
            Text(
                month.format(MONTH_TITLE),
                Modifier.padding(start = Space.x2),
                fontSize = FontSize.s400,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            CascadeIconButton(R.drawable.ic_caret_left, "Previous month", { month = month.minusMonths(1) }, iconSize = 14.dp)
            CascadeIconButton(R.drawable.ic_caret_right, "Next month", { month = month.plusMonths(1) }, iconSize = 14.dp)
        }
        Row {
            for (weekday in weekdays) {
                Box(Modifier.size(CELL, 24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        weekday.narrow(),
                        color = colors.muted,
                        fontSize = FontSize.s200,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        for (week in 0 until weeks) {
            Row {
                for (slot in 0 until 7) {
                    val day = week * 7 + slot - lead + 1
                    if (day < 1 || day > month.lengthOfMonth()) {
                        Spacer(Modifier.size(CELL))
                        continue
                    }
                    val date = month.atDay(day)
                    val isSelected = date == value
                    val content = if (isSelected) colors.onPrimary else colors.ink
                    Box(
                        Modifier
                            .size(CELL)
                            .padding(Space.x0_5)
                            .clip(Radius.md)
                            .background(if (isSelected) colors.primary else Color.Transparent)
                            .then(
                                if (date == today && !isSelected) {
                                    Modifier.border(1.dp, colors.borderStrong, Radius.md)
                                } else {
                                    Modifier
                                },
                            )
                            .clickable(role = Role.Button) { onPick(date) }
                            .semantics {
                                contentDescription = date.format(DAY_DESCRIPTION)
                                selected = isSelected
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(day.toString(), color = content, fontSize = FontSize.s300)
                        if (marked(date)) {
                            Box(
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 3.dp)
                                    .size(4.dp)
                                    .background(content, Radius.full),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DayOfWeek.narrow(): String = getDisplayName(TextStyle.SHORT, Locale.US).take(2)
