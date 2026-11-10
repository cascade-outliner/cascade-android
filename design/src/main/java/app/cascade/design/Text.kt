package app.cascade.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cascade.design.theme.BorderWidth
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.FontSize

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        modifier,
        color = Cascade.colors.muted,
        fontSize = FontSize.s100,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.8.sp,
    )
}

@Composable
fun Kbd(text: String, modifier: Modifier = Modifier) {
    val colors = Cascade.colors
    val shape = RoundedCornerShape(7.dp)
    Text(
        text,
        modifier
            .background(colors.canvas, shape)
            .border(BorderWidth.thin, colors.border, shape)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        color = colors.muted,
        fontFamily = FontFamily.Monospace,
        fontSize = FontSize.s200,
    )
}
