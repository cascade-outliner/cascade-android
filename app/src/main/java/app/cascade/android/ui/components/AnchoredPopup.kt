package app.cascade.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import app.cascade.android.ui.theme.Cascade

private class AnchoredPositionProvider(
    private val top: Int,
    private val bottom: Int,
    private val margin: Int,
    private val gap: Int,
    private val alignEnd: Boolean,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val lowest = windowSize.height - bottom - margin
        val highest = top + margin
        val below = anchorBounds.bottom + gap
        val above = anchorBounds.top - gap - popupContentSize.height
        val y = when {
            below + popupContentSize.height <= lowest -> below
            above >= highest -> above
            else -> (lowest - popupContentSize.height).coerceAtLeast(highest)
        }
        val wanted = if (alignEnd) anchorBounds.right - popupContentSize.width else anchorBounds.left
        val x = wanted.coerceIn(margin, (windowSize.width - popupContentSize.width - margin).coerceAtLeast(margin))
        return IntOffset(x, y)
    }
}

@Composable
fun AnchoredPopup(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    focusable: Boolean = false,
    alignEnd: Boolean = false,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val top = WindowInsets.statusBars.getTop(density)
    val bottom = maxOf(WindowInsets.ime.getBottom(density), WindowInsets.navigationBars.getBottom(density))
    val provider = remember(top, bottom, alignEnd, density) {
        with(density) { AnchoredPositionProvider(top, bottom, 8.dp.roundToPx(), 6.dp.roundToPx(), alignEnd) }
    }
    Popup(
        popupPositionProvider = provider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = focusable),
    ) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            color = Cascade.colors.white,
            contentColor = Cascade.colors.ink,
            border = BorderStroke(1.dp, Cascade.colors.border),
            shadowElevation = 12.dp,
            content = content,
        )
    }
}
