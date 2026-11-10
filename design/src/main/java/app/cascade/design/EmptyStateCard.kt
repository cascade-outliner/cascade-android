package app.cascade.design

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.FontSize
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space

@Composable
fun EmptyStateCard(
    @DrawableRes icon: Int,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    val colors = Cascade.colors
    Column(
        modifier.fillMaxWidth().padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Space.x2_5),
    ) {
        Box(Modifier.size(40.dp).background(colors.surface, Radius.lg), contentAlignment = Alignment.Center) {
            CascadeIcon(icon, size = 18.dp, tint = colors.primary)
        }
        Text(title, fontSize = FontSize.s700, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Text(
            description,
            Modifier.widthIn(max = 260.dp),
            color = colors.muted,
            fontSize = FontSize.s300,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            CascadeButton(onAction, Modifier.padding(top = Space.x1)) { Text(action) }
        }
    }
}
