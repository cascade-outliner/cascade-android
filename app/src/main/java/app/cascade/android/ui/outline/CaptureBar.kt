package app.cascade.android.ui.outline

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import app.cascade.android.R
import app.cascade.android.commands.SlashCommand
import app.cascade.android.commands.captureSlashCommands
import app.cascade.android.commands.filterSlashCommands
import app.cascade.android.commands.matchSlashTrigger
import app.cascade.android.ui.components.CascadeIcon
import app.cascade.android.ui.components.SquareIconButton
import app.cascade.android.ui.theme.Cascade
import app.cascade.android.ui.theme.FontSize
import app.cascade.android.ui.theme.Onest

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CaptureBar(
    onSubmit: (text: String, picked: List<SlashCommand>) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    placeholder: String = "Capture a thought…",
) {
    val colors = Cascade.colors
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    val picked = remember { mutableStateListOf<SlashCommand>() }
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
        filterSlashCommands(captureSlashCommands, trigger.query)
    } else {
        emptyList()
    }

    fun pick(command: SlashCommand) {
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
        shape = RoundedCornerShape(24.dp),
        color = colors.white,
        shadowElevation = 10.dp,
    ) {
        Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp)) {
            if (picked.isNotEmpty()) {
                FlowRow(
                    Modifier.padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    for (command in picked) {
                        Row(
                            Modifier
                                .height(28.dp)
                                .background(colors.primaryMuted, RoundedCornerShape(12.dp))
                                .padding(start = 8.dp, end = 2.dp)
                                .testTag("capture-bar-chip"),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                                    .clip(RoundedCornerShape(8.dp))
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
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                Box {
                    SquareIconButton(
                        R.drawable.ic_plus_bold,
                        "Insert command",
                        { plusOpen = !plusOpen },
                        Modifier.testTag("capture-bar-plus"),
                        background = colors.primaryMuted,
                        tint = colors.primary,
                    )
                    if (plusOpen) {
                        SlashMenu(
                            commands = captureSlashCommands,
                            onDismiss = { plusOpen = false },
                            onPick = {
                                pick(it)
                                plusOpen = false
                                focusRequester.requestFocus()
                            },
                        )
                    } else if (offered.isNotEmpty() && trigger != null) {
                        SlashMenu(
                            commands = offered,
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
                Box(Modifier.weight(1f).heightIn(min = 36.dp, max = 160.dp).padding(vertical = 6.dp)) {
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
                SquareIconButton(
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
