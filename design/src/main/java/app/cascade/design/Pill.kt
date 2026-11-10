package app.cascade.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.FontSize
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space

enum class PillTone { Neutral, Primary, Info }

@Composable
fun Pill(text: String, modifier: Modifier = Modifier, tone: PillTone = PillTone.Neutral) {
    val colors = Cascade.colors
    val (background, content) = when (tone) {
        PillTone.Neutral -> colors.inkSubtle to colors.muted
        PillTone.Primary -> colors.surface to colors.primary
        PillTone.Info -> colors.infoMuted to colors.info
    }
    Text(
        text,
        modifier
            .clip(Radius.full)
            .background(background)
            .padding(horizontal = Space.x2, vertical = Space.x0_5),
        color = content,
        fontSize = FontSize.s200,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
    )
}
