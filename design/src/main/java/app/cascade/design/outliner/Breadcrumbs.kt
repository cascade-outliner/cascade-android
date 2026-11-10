package app.cascade.design.outliner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.cascade.design.CascadeIcon
import app.cascade.design.CascadeIconButton
import app.cascade.design.R
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.FontSize
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space

private const val MAX_VISIBLE_CRUMBS = 3

data class Crumb(val id: String, val label: String)

@Composable
fun Breadcrumbs(
    crumbs: List<Crumb>,
    onHome: () -> Unit,
    onCrumb: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Cascade.colors
    val visible = crumbs.takeLast(MAX_VISIBLE_CRUMBS)
    val scroll = rememberScrollState()
    LaunchedEffect(crumbs.lastOrNull()?.id) { scroll.scrollTo(scroll.maxValue) }

    @Composable
    fun Separator() = CascadeIcon(R.drawable.ic_caret_right_bold, size = 10.dp, tint = colors.placeholder)

    Row(
        modifier.horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(Space.x0_5),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CascadeIconButton(
            R.drawable.ic_house_simple_bold,
            "Zoom out to root",
            onHome,
            size = 32.dp,
            iconSize = 13.dp,
        )
        if (crumbs.size > visible.size) {
            Separator()
            Text("…", Modifier.padding(horizontal = Space.x1_5), color = colors.muted, fontSize = FontSize.s300)
        }
        for (crumb in visible) {
            Separator()
            Text(
                crumb.label,
                Modifier
                    .clip(Radius.sm)
                    .clickable(role = Role.Button) { onCrumb(crumb.id) }
                    .padding(horizontal = Space.x2, vertical = 7.dp)
                    .widthIn(max = 160.dp),
                color = colors.muted,
                fontSize = FontSize.s300,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

