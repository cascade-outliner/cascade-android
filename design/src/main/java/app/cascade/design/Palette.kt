package app.cascade.design

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.FontSize
import app.cascade.design.theme.Onest
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space

@Composable
fun PaletteField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    onSubmit: () -> Unit = {},
) {
    val colors = Cascade.colors
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    Row(
        modifier.padding(start = Space.x4, end = Space.x2, top = Space.x2, bottom = Space.x2),
        horizontalArrangement = Arrangement.spacedBy(Space.x3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CascadeIcon(R.drawable.ic_magnifying_glass, size = 18.dp, tint = colors.muted)
        val style = TextStyle(fontFamily = Onest, fontSize = FontSize.s700, color = colors.ink)
        Box(Modifier.weight(1f).padding(vertical = Space.x2_5)) {
            if (query.isEmpty()) Text(placeholder, style = style, color = colors.placeholder, maxLines = 1)
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth().focusRequester(focus).testTag("search-input"),
                textStyle = style,
                singleLine = true,
                cursorBrush = SolidColor(colors.info),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
            )
        }
        CascadeIconButton(R.drawable.ic_x_bold, "Close search", onClose, size = 40.dp, iconSize = 14.dp)
    }
}

@Composable
fun PaletteGroup(label: String, modifier: Modifier = Modifier) {
    SectionLabel(label, modifier.padding(start = Space.x3, end = Space.x3, top = Space.x3_5, bottom = Space.x1_5))
}

@Composable
fun PaletteItem(
    label: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ranges: List<IntRange> = emptyList(),
    detail: String? = null,
) {
    val colors = Cascade.colors
    val mark = SpanStyle(fontWeight = FontWeight.SemiBold, background = colors.infoMuted, color = colors.info)
    val text = buildAnnotatedString {
        append(label)
        for (range in ranges) addStyle(mark, range.first, range.last + 1)
    }
    Row(
        modifier
            .fillMaxWidth()
            .clip(Radius.md)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Space.x3, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(Space.x3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
            CascadeIcon(icon, size = 16.dp, tint = colors.muted)
        }
        Column(Modifier.weight(1f)) {
            Text(text, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (!detail.isNullOrEmpty()) {
                Text(detail, color = colors.muted, fontSize = FontSize.s200, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
