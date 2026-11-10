package app.cascade.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.Elevation
import app.cascade.design.theme.FontSize
import app.cascade.design.theme.Onest
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T : SlashMenuItem> CaptureBar(
    onSubmit: (text: String, picked: List<T>) -> Unit,
    focusRequester: FocusRequester,
    slashItems: List<T> = emptyList(),
    modifier: Modifier = Modifier,
    placeholder: String = "Capture a thought…",
) {
    val colors = Cascade.colors
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    val picked = remember { mutableStateListOf<T>() }
    var focused by remember { mutableStateOf(false) }
    var plusOpen by remember { mutableStateOf(false) }
    var dismissed by remember { mutableStateOf<Int?>(null) }
    val hasText = value.text.isNotBlank()
    val currentOnSubmit by rememberUpdatedState(onSubmit)

    val trigger = if (focused && value.selection.collapsed) {
        matchSlashTrigger(value.text.substring(0, value.selection.start))
    } else {
        null
    }
    LaunchedEffect(trigger?.index) { if (trigger?.index != dismissed) dismissed = null }
    val offered = if (trigger != null && trigger.index != dismissed) {
        filterSlashMenuItems(slashItems, trigger.query)
    } else {
        emptyList()
    }

    fun pick(command: T) {
        if (command !in picked) picked.add(command)
    }

    val submit: () -> Unit = {
        val text = value.text.trim()
        if (text.isNotEmpty()) {
            currentOnSubmit(text, picked.toList())
            picked.clear()
            value = TextFieldValue("")
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth().testTag("capture-bar"),
        shape = Radius.xl,
        color = colors.white,
        shadowElevation = Elevation.float,
    ) {
        Column(Modifier.padding(start = Space.x3, end = Space.x3, top = Space.x2_5, bottom = Space.x2_5)) {
            if (picked.isNotEmpty()) {
                FlowRow(
                    Modifier.padding(bottom = Space.x2),
                    horizontalArrangement = Arrangement.spacedBy(Space.x1_5),
                    verticalArrangement = Arrangement.spacedBy(Space.x1_5),
                ) {
                    for (command in picked) {
                        Row(
                            Modifier
                                .height(28.dp)
                                .background(colors.primaryMuted, Radius.md)
                                .padding(start = Space.x2, end = Space.x0_5)
                                .testTag("capture-bar-chip"),
                            horizontalArrangement = Arrangement.spacedBy(Space.x1),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CascadeIcon(command.icon, size = 13.dp, tint = colors.primary)
                            Text(
                                command.label,
                                color = colors.primary,
                                fontSize = FontSize.s300,
                                fontWeight = FontWeight.Medium,
                            )
                            Box(
                                Modifier
                                    .size(24.dp)
                                    .clip(Radius.sm)
                                    .clickable(role = Role.Button) { picked.remove(command) }
                                    .semantics { contentDescription = "Remove ${command.label}" },
                                contentAlignment = Alignment.Center,
                            ) {
                                CascadeIcon(R.drawable.ic_x_bold, size = 11.dp, tint = colors.primary)
                            }
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Space.x2_5), verticalAlignment = Alignment.Bottom) {
                if (slashItems.isNotEmpty()) Box {
                    CascadeIconButton(
                        R.drawable.ic_plus_bold,
                        "Insert command",
                        { plusOpen = !plusOpen },
                        Modifier.testTag("capture-bar-plus"),
                        background = colors.primaryMuted,
                        tint = colors.primary,
                    )
                    if (plusOpen) {
                        SlashMenu(
                            items = slashItems,
                            onDismiss = { plusOpen = false },
                            onPick = {
                                pick(it)
                                plusOpen = false
                                focusRequester.requestFocus()
                            },
                        )
                    } else if (offered.isNotEmpty() && trigger != null) {
                        SlashMenu(
                            items = offered,
                            onDismiss = { dismissed = trigger.index },
                            onPick = {
                                pick(it)
                                val cursor = value.selection.start
                                value = TextFieldValue(value.text.removeRange(trigger.index, cursor), TextRange(trigger.index))
                            },
                        )
                    }
                }
                val textStyle = TextStyle(fontFamily = Onest, fontSize = FontSize.s600, lineHeight = 24.sp, color = colors.ink)
                Box(Modifier.weight(1f).heightIn(min = 36.dp, max = 160.dp).padding(vertical = Space.x1_5)) {
                    if (value.text.isEmpty()) {
                        Text(placeholder, style = textStyle, color = colors.placeholder, maxLines = 1)
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = { value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { focused = it.isFocused }
                            .onPreviewKeyEvent { event ->
                                when {
                                    event.type != KeyEventType.KeyDown -> false
                                    event.key == Key.Enter && !event.isShiftPressed -> {
                                        submit()
                                        true
                                    }
                                    event.key == Key.Backspace && value.text.isEmpty() && picked.isNotEmpty() -> {
                                        picked.removeAt(picked.lastIndex)
                                        true
                                    }
                                    else -> false
                                }
                            }
                            .semantics { contentDescription = "Add a node" }
                            .testTag("capture-bar-input"),
                        textStyle = textStyle,
                        cursorBrush = SolidColor(colors.info),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                    )
                }
                CascadeIconButton(
                    R.drawable.ic_arrow_up_bold,
                    "Add",
                    submit,
                    Modifier.testTag("capture-bar-submit"),
                    enabled = hasText,
                    background = if (hasText) colors.primary else colors.primaryMuted,
                    tint = if (hasText) colors.onPrimary else colors.primary,
                )
            }
        }
    }
}
