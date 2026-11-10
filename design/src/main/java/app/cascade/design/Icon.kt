package app.cascade.design

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.cascade.design.theme.Cascade

@Composable
fun CascadeIcon(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    tint: Color = Cascade.colors.ink,
    contentDescription: String? = null,
) {
    Icon(painterResource(icon), contentDescription, modifier.size(size), tint)
}

@Composable
fun LogoMark(modifier: Modifier = Modifier, size: Dp = 28.dp) {
    val fill = Cascade.colors.primary
    val bars = Cascade.colors.canvas
    Canvas(modifier.size(size)) {
        val unit = this.size.width / 24f
        drawRoundRect(fill, cornerRadius = CornerRadius(6 * unit))
        fun bar(x: Float, y: Float, width: Float) = drawRoundRect(
            bars,
            topLeft = Offset(x * unit, y * unit),
            size = Size(width * unit, 2 * unit),
            cornerRadius = CornerRadius(unit),
        )
        bar(4.75f, 6.75f, 12.5f)
        bar(7.25f, 11f, 9.25f)
        bar(9.5f, 15.25f, 6.75f)
    }
}
