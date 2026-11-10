package app.cascade.design.outliner

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.cascade.design.CascadeIcon
import app.cascade.design.R
import app.cascade.design.theme.BorderWidth
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space

object OutlinerMetrics {
    val ListPadding: Dp = Space.x2
    val RowInset: Dp = Space.x0_5
    val Indent: Dp = Space.x3_5
    val Chevron: Dp = 26.dp
    val Bullet: Dp = 30.dp
    val MinRowHeight: Dp = 40.dp
    val RowShape = RoundedCornerShape(14.dp)

    fun bulletStart(depth: Int): Dp = ListPadding + RowInset + Indent * depth + Chevron
}

@Composable
fun OutlinerRow(
    depth: Int,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    dragging: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = Cascade.colors
    val guide = colors.border
    Row(
        modifier
            .fillMaxWidth()
            .alpha(if (dragging) 0.35f else 1f)
            .drawBehind {
                val step = OutlinerMetrics.Indent.toPx()
                val first = OutlinerMetrics.RowInset.toPx() + OutlinerMetrics.Chevron.toPx() / 2
                for (level in 0 until depth) {
                    val x = first + level * step
                    drawLine(guide, Offset(x, 0f), Offset(x, size.height), strokeWidth = BorderWidth.thin.toPx())
                }
            }
            .padding(start = OutlinerMetrics.RowInset + OutlinerMetrics.Indent * depth, end = OutlinerMetrics.RowInset)
            .clip(OutlinerMetrics.RowShape)
            .background(if (active) colors.white else Color.Transparent)
            .heightIn(min = OutlinerMetrics.MinRowHeight)
            .padding(end = Space.x2_5)
            .testTag("outliner-row"),
        verticalAlignment = Alignment.Top,
        content = content,
    )
}

@Composable
fun Chevron(open: Boolean, visible: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val rotation by animateFloatAsState(if (open) 90f else 0f, label = "chevron")
    Box(
        modifier
            .size(OutlinerMetrics.Chevron, OutlinerMetrics.MinRowHeight)
            .then(
                if (visible) {
                    Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = onClick,
                        )
                        .semantics { contentDescription = if (open) "Collapse" else "Expand" }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (visible) {
            CascadeIcon(R.drawable.ic_caret_right_bold, Modifier.rotate(rotation), size = 12.dp, tint = Cascade.colors.muted)
        }
    }
}

@Composable
fun Bullet(collapsed: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Cascade.colors
    Box(
        modifier
            .size(OutlinerMetrics.Bullet, OutlinerMetrics.MinRowHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = "Zoom in. Hold for more, drag to move" }
            .testTag("outliner-bullet"),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(20.dp).background(if (collapsed) colors.inkSubtleHover else colors.inkSubtle, Radius.full),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(6.dp).background(colors.muted, Radius.full))
        }
    }
}
