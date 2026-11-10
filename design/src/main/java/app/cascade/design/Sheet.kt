package app.cascade.design

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.cascade.design.theme.BorderWidth
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.FontSize
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSheetState(): SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    state: SheetState = rememberSheetState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = Cascade.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = colors.white,
        contentColor = colors.ink,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = Space.x3, end = Space.x3, bottom = Space.x3),
        ) {
            if (title != null) {
                Text(
                    title,
                    Modifier.padding(horizontal = Space.x3).padding(bottom = Space.x3),
                    fontSize = FontSize.s600,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            content()
        }
    }
}

@Composable
fun SheetSection(label: String, modifier: Modifier = Modifier) {
    SectionLabel(label, modifier.padding(start = Space.x3, end = Space.x3, top = Space.x3, bottom = Space.x2))
}

@Composable
fun SheetDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier.padding(vertical = Space.x2), color = Cascade.colors.border)
}

@Composable
fun SheetItem(
    label: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    danger: Boolean = false,
) {
    val colors = Cascade.colors
    val tint = if (danger) colors.danger else colors.ink
    Row(
        modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clip(Radius.md)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = Space.x3, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(Space.x3_5),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            CascadeIcon(icon, size = 18.dp, tint = if (danger) tint else colors.muted)
        }
        Text(label, color = tint, fontSize = FontSize.s600)
    }
}

@Composable
fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
) {
    val colors = Cascade.colors
    val content = if (selected) colors.onPrimary else colors.ink
    Row(
        modifier
            .clip(Radius.md)
            .background(if (selected) colors.primary else Color.Transparent)
            .border(BorderWidth.thin, if (selected) colors.primary else colors.border, Radius.md)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(horizontal = Space.x3, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(Space.x1_5),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) CascadeIcon(icon, size = 14.dp, tint = content)
        Text(label, color = content, fontSize = FontSize.s400, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}
