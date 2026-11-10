package app.cascade.android.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.cascade.android.R
import app.cascade.android.ui.theme.Cascade
import app.cascade.android.ui.theme.FontSize

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
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        color = content,
        fontSize = FontSize.s200,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
    )
}

@Composable
fun TaskMarker(done: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Cascade.colors
    val fill by animateColorAsState(if (done) colors.primary else Color.Transparent, label = "task-fill")
    val check by animateFloatAsState(
        if (done) 1f else 0f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "task-check",
    )
    Box(
        modifier
            .size(32.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Checkbox,
                onClick = onToggle,
            )
            .semantics {
                contentDescription = if (done) "Mark as not done" else "Mark as done"
                stateDescription = if (done) "Done" else "Not done"
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(20.dp)
                .background(fill, CircleShape)
                .border(1.5.dp, if (done) colors.primary else colors.borderStrong, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CascadeIcon(
                R.drawable.ic_check_bold,
                Modifier.graphicsLayer {
                    scaleX = 0.3f + 0.7f * check
                    scaleY = 0.3f + 0.7f * check
                    alpha = check.coerceIn(0f, 1f)
                },
                size = 12.dp,
                tint = colors.canvas,
            )
        }
    }
}

enum class ButtonVariant { Primary, Secondary, Ghost }

@Composable
fun CascadeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Secondary,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
    content: @Composable () -> Unit,
) {
    val colors = Cascade.colors
    val shape = RoundedCornerShape(12.dp)
    val background = when (variant) {
        ButtonVariant.Primary -> colors.primary
        ButtonVariant.Secondary -> colors.white
        ButtonVariant.Ghost -> Color.Transparent
    }
    val contentColor = if (variant == ButtonVariant.Primary) colors.onPrimary else colors.ink
    Row(
        modifier
            .alpha(if (enabled) 1f else 0.4f)
            .clip(shape)
            .background(background)
            .then(if (variant == ButtonVariant.Secondary) Modifier.border(1.dp, colors.border, shape) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = 40.dp)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.ProvideTextStyle(
            androidx.compose.material3.MaterialTheme.typography.labelLarge.copy(color = contentColor),
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides contentColor,
                content = content,
            )
        }
    }
}

@Composable
fun SquareIconButton(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    background: Color = Color.Transparent,
    tint: Color = Cascade.colors.muted,
    bordered: Boolean = false,
    size: Dp = 36.dp,
    iconSize: Dp = 16.dp,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier
            .size(size)
            .clip(shape)
            .background(background)
            .then(if (bordered) Modifier.border(1.dp, Cascade.colors.border, shape) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        CascadeIcon(icon, size = iconSize, tint = tint)
    }
}
