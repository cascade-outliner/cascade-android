package app.cascade.design.outliner

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cascade.design.SlashMenu
import app.cascade.design.SlashMenuItem
import app.cascade.design.filterSlashMenuItems
import app.cascade.design.matchSlashTrigger
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.FontSize
import app.cascade.design.theme.Onest
import app.cascade.design.theme.Space

@Composable
private fun contentStyle() = TextStyle(
    fontFamily = Onest,
    fontSize = FontSize.s600,
    lineHeight = 22.sp,
    color = Cascade.colors.ink,
)

@Composable
fun OutlinerLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier.padding(start = Space.x1, top = 9.dp, bottom = 9.dp).testTag("outliner-content"),
        style = contentStyle(),
    )
}

@Composable
fun <T : SlashMenuItem> OutlinerContent(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    slashItems: List<T> = emptyList(),
    onSlashSelect: (T) -> Unit = {},
    onFocusChange: (Boolean) -> Unit = {},
) {
    var value by remember { mutableStateOf(TextFieldValue(text)) }
    val shown = if (value.text == text) value else TextFieldValue(text, TextRange(text.length))
    var focused by remember { mutableStateOf(false) }
    var dismissed by remember { mutableStateOf<Int?>(null) }
    val trigger = if (focused && shown.selection.collapsed) {
        matchSlashTrigger(shown.text.substring(0, shown.selection.start))
    } else {
        null
    }
    LaunchedEffect(trigger?.index) { if (trigger?.index != dismissed) dismissed = null }
    val offered = if (trigger != null && trigger.index != dismissed) {
        filterSlashMenuItems(slashItems, trigger.query)
    } else {
        emptyList()
    }

    Box(modifier.padding(start = Space.x1, top = 9.dp, bottom = 9.dp)) {
        BasicTextField(
            value = shown,
            onValueChange = {
                value = it
                onTextChange(it.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    focused = it.isFocused
                    onFocusChange(it.isFocused)
                }
                .testTag("outliner-content"),
            textStyle = contentStyle(),
            cursorBrush = SolidColor(Cascade.colors.info),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )
        if (offered.isNotEmpty() && trigger != null) {
            SlashMenu(
                items = offered,
                onDismiss = { dismissed = trigger.index },
                onPick = { item ->
                    val without = shown.text.removeRange(trigger.index, shown.selection.start)
                    value = TextFieldValue(without, TextRange(trigger.index))
                    onTextChange(without)
                    onSlashSelect(item)
                },
            )
        }
    }
}

@Composable
fun ZoomTitle(
    text: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    onTextChange: (String) -> Unit = {},
) {
    val colors = Cascade.colors
    val style = TextStyle(
        fontFamily = Onest,
        fontSize = FontSize.s800,
        lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.4).sp,
        color = colors.ink,
    )
    val padded = modifier.fillMaxWidth().padding(start = Space.x3, end = Space.x3, top = Space.x3, bottom = Space.x5)
    if (readOnly) {
        Text(text, padded.testTag("zoom-title"), style = style)
        return
    }
    var value by remember { mutableStateOf(TextFieldValue(text)) }
    val shown = if (value.text == text) value else TextFieldValue(text, TextRange(text.length))
    Box(padded) {
        if (shown.text.isEmpty()) Text("Untitled", style = style, color = colors.placeholder)
        BasicTextField(
            value = shown,
            onValueChange = {
                value = it
                onTextChange(it.text)
            },
            modifier = Modifier.fillMaxWidth().testTag("zoom-title"),
            textStyle = style,
            cursorBrush = SolidColor(colors.info),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )
    }
}
