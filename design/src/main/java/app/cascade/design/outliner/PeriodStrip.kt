package app.cascade.design.outliner

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.cascade.design.CascadeIconButton
import app.cascade.design.R
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.FontSize
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space

class StripItem(val name: String, val label: String, val marked: Boolean, val onPick: () -> Unit)

class Strip(val kind: String, val selected: Int, val items: List<StripItem>, val onShift: (Int) -> Unit)

@Composable
fun PeriodStrip(strip: Strip, modifier: Modifier = Modifier) {
    val colors = Cascade.colors
    val position by animateFloatAsState(
        strip.selected.toFloat(),
        spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow),
        label = "period-strip",
    )
    Row(
        modifier.fillMaxWidth().padding(horizontal = Space.x1, vertical = Space.x2),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CascadeIconButton(R.drawable.ic_caret_left, "Previous", { strip.onShift(-1) }, size = 40.dp, iconSize = 14.dp)
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
                            .clip(Radius.lg)
                            .clickable(role = Role.Button, onClick = item.onPick)
                            .semantics(mergeDescendants = true) { selected = isSelected },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Space.x0_5, Alignment.CenterVertically),
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
                                .background(if (item.marked) content else Color.Transparent, Radius.full),
                        )
                    }
                }
            }
        }
        CascadeIconButton(R.drawable.ic_caret_right, "Next", { strip.onShift(1) }, size = 40.dp, iconSize = 14.dp)
    }
}
