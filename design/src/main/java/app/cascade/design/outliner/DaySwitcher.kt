package app.cascade.design.outliner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.cascade.design.AnchoredPopup
import app.cascade.design.Calendar
import app.cascade.design.CascadeIconButton
import app.cascade.design.R
import app.cascade.design.theme.BorderWidth
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.FontSize
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space
import java.time.LocalDate

@Composable
fun DaySwitcher(
    label: String,
    active: Boolean,
    date: LocalDate,
    marked: (LocalDate) -> Boolean,
    onToday: () -> Unit,
    onOlder: () -> Unit,
    onNewer: () -> Unit,
    onPick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Cascade.colors
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            Modifier.border(BorderWidth.thin, colors.border, Radius.md).padding(Space.x0_5),
            horizontalArrangement = Arrangement.spacedBy(Space.x0_5),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CascadeIconButton(R.drawable.ic_caret_left, "Previous day", onOlder, size = 32.dp, iconSize = 13.dp)
            Text(
                label,
                Modifier
                    .clip(Radius.sm)
                    .background(if (active) colors.primaryMuted else Color.Transparent)
                    .clickable(role = Role.Button, onClick = onToday)
                    .semantics { selected = active }
                    .padding(horizontal = Space.x2_5, vertical = Space.x1_5),
                color = if (active) colors.primary else colors.ink,
                fontSize = FontSize.s300,
                maxLines = 1,
            )
            CascadeIconButton(R.drawable.ic_caret_right, "Next day", onNewer, size = 32.dp, iconSize = 13.dp)
            CascadeIconButton(R.drawable.ic_calendar_blank, "Pick a day", { open = true }, size = 32.dp, iconSize = 14.dp)
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
                    modifier = Modifier.padding(Space.x3),
                )
            }
        }
    }
}

