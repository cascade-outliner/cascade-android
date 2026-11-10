package app.cascade.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.cascade.design.theme.BorderWidth
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.FontSize
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space

@Composable
fun Stepper(steps: List<String>, current: Int, modifier: Modifier = Modifier) {
    val colors = Cascade.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(Space.x2), verticalAlignment = Alignment.CenterVertically) {
        steps.forEachIndexed { index, label ->
            if (index > 0) Box(Modifier.size(16.dp, 1.dp).background(colors.border))
            val reached = index <= current
            Row(horizontalArrangement = Arrangement.spacedBy(Space.x1_5), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(22.dp)
                        .background(if (reached) colors.primary else Color.Transparent, Radius.full)
                        .border(BorderWidth.thin, if (reached) colors.primary else colors.border, Radius.full),
                    contentAlignment = Alignment.Center,
                ) {
                    if (index < current) {
                        CascadeIcon(R.drawable.ic_check_bold, size = 11.dp, tint = colors.onPrimary)
                    } else {
                        Text(
                            (index + 1).toString(),
                            color = if (reached) colors.onPrimary else colors.muted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = FontSize.s200,
                        )
                    }
                }
                if (index == current) Text(label, fontSize = FontSize.s300, fontWeight = FontWeight.Medium)
            }
        }
    }
}
