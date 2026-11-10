package app.cascade.design.outliner

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import app.cascade.design.CascadeIcon
import app.cascade.design.R
import app.cascade.design.theme.BorderWidth
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.Radius

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
                .background(fill, Radius.full)
                .border(BorderWidth.thick, if (done) colors.primary else colors.borderStrong, Radius.full),
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
