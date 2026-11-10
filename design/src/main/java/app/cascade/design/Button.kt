package app.cascade.design

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.cascade.design.theme.BorderWidth
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space

enum class ButtonVariant { Primary, Secondary, Ghost }

const val DISABLED_ALPHA = 0.4f

@Composable
fun CascadeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Secondary,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = Space.x3_5, vertical = 9.dp),
    content: @Composable () -> Unit,
) {
    val colors = Cascade.colors
    val background = when (variant) {
        ButtonVariant.Primary -> colors.primary
        ButtonVariant.Secondary -> colors.white
        ButtonVariant.Ghost -> Color.Transparent
    }
    val contentColor = if (variant == ButtonVariant.Primary) colors.onPrimary else colors.ink
    Row(
        modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clip(Radius.md)
            .background(background)
            .then(
                if (variant == ButtonVariant.Secondary) {
                    Modifier.border(BorderWidth.thin, colors.border, Radius.md)
                } else {
                    Modifier
                },
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = 40.dp)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(Space.x1_5, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProvideTextStyle(MaterialTheme.typography.labelLarge.copy(color = contentColor)) {
            CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
        }
    }
}

@Composable
fun CascadeIconButton(
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
    Box(
        modifier
            .size(size)
            .clip(Radius.md)
            .background(background)
            .then(if (bordered) Modifier.border(BorderWidth.thin, Cascade.colors.border, Radius.md) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        CascadeIcon(icon, size = iconSize, tint = tint)
    }
}
