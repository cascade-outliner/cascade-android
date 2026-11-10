package app.cascade.design.outliner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.Elevation
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space

@Composable
fun DropIndicator(depth: Int, y: Dp, modifier: Modifier = Modifier) {
    val color = Cascade.colors.primary
    Row(
        modifier
            .offset(x = OutlinerMetrics.bulletStart(depth) + 1.dp, y = y - 4.dp)
            .fillMaxWidth()
            .padding(end = OutlinerMetrics.bulletStart(depth) + OutlinerMetrics.ListPadding + 1.dp)
            .height(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).background(color, Radius.full))
        Box(Modifier.weight(1f).height(2.dp).background(color, RoundedCornerShape(1.dp)))
    }
}

@Composable
fun DragGhost(text: String, y: Dp, depth: Int, modifier: Modifier = Modifier) {
    val colors = Cascade.colors
    Surface(
        modifier = modifier
            .offset(x = OutlinerMetrics.bulletStart(depth), y = y - 20.dp)
            .padding(end = OutlinerMetrics.ListPadding * 2)
            .width(260.dp),
        shape = OutlinerMetrics.RowShape,
        color = colors.white,
        shadowElevation = Elevation.float,
    ) {
        Row(Modifier.height(40.dp).padding(horizontal = Space.x1_5), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(OutlinerMetrics.Bullet, 40.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(6.dp).background(colors.muted, Radius.full))
            }
            Text(text.ifBlank { "Untitled" }, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
