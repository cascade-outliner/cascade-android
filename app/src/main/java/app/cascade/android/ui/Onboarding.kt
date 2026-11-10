package app.cascade.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cascade.design.ButtonVariant
import app.cascade.design.CascadeButton
import app.cascade.design.Kbd
import app.cascade.design.LogoMark
import app.cascade.design.Stepper
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.FontSize
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space

private class Template(
    val id: String,
    val title: String,
    val description: String,
    val enabled: Boolean,
    val preview: List<Triple<Int, Boolean, Float>>,
)

private val TEMPLATES = listOf(
    Template("blank", "Blank", "One empty line and a blinking cursor.", true, listOf(Triple(0, false, 0.4f))),
    Template(
        "project", "Project", "Goals, tasks and notes, nested three deep.", false,
        listOf(
            Triple(0, false, 0.5f), Triple(1, true, 0.6f), Triple(1, true, 0.45f),
            Triple(2, false, 0.4f), Triple(0, false, 0.35f), Triple(1, true, 0.55f),
        ),
    ),
    Template(
        "journal", "Journal", "A page per day with today on top.", false,
        listOf(
            Triple(0, false, 0.3f), Triple(1, false, 0.7f), Triple(1, true, 0.5f),
            Triple(0, false, 0.3f), Triple(1, false, 0.6f),
        ),
    ),
)

private val BASICS = listOf(
    "Type" to "Type in the bar at the bottom to add a line.",
    "Drag" to "Hold a bullet and drag to move a line, or nest it under another.",
    "Tap" to "Tap a bullet to zoom into that line as its own page.",
    "Hold" to "Hold a bullet to make a line a task, duplicate or delete it.",
    "Search" to "Open search to find a line and jump anywhere.",
)

private val STEPS = listOf("How it works", "Start with", "Heads up")

@Composable
fun Onboarding(onDone: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Cascade.colors
    var step by rememberSaveable { mutableIntStateOf(0) }
    var template by rememberSaveable { mutableStateOf("blank") }
    val last = STEPS.lastIndex

    BackHandler(enabled = step > 0) { step-- }

    Column(
        modifier
            .fillMaxSize()
            .background(colors.canvas)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(Space.x6)
            .testTag("onboarding"),
        verticalArrangement = Arrangement.spacedBy(Space.x6),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Space.x3_5), verticalAlignment = Alignment.CenterVertically) {
            LogoMark(size = 24.dp)
            Spacer(Modifier.weight(1f))
            Stepper(STEPS, step, Modifier.testTag("onboarding-stepper"))
        }
        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { direction * it / 12 })
                    .togetherWith(fadeOut(tween(90)))
            },
            label = "onboarding-step",
        ) { current ->
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Space.x6),
            ) {
                when (current) {
                    0 -> {
                        Heading("Everything is a line", "Cascade is an outliner. Lines nest inside lines, as deep as you like.")
                        Column(
                            Modifier.fillMaxWidth().background(colors.white, Radius.lg).padding(Space.x2),
                            verticalArrangement = Arrangement.spacedBy(Space.x1),
                        ) {
                            for ((key, text) in BASICS) {
                                Row(
                                    Modifier.padding(horizontal = Space.x2_5, vertical = Space.x2),
                                    horizontalArrangement = Arrangement.spacedBy(Space.x3),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(Modifier.width(64.dp), contentAlignment = Alignment.CenterEnd) { Kbd(key) }
                                    Text(text, Modifier.weight(1f), fontSize = FontSize.s400, lineHeight = 20.sp)
                                }
                            }
                        }
                    }
                    1 -> {
                        Heading("How do you want to start?", "You can change everything later. Templates are just nodes.")
                        Column(verticalArrangement = Arrangement.spacedBy(Space.x3_5)) {
                            for (each in TEMPLATES) {
                                TemplateCard(each, checked = template == each.id) { template = each.id }
                            }
                        }
                    }
                    else -> {
                        Heading("Still in development", "Cascade is early. Expect rough edges.")
                        Column(
                            Modifier.fillMaxWidth().background(colors.white, Radius.lg).padding(Space.x4),
                            verticalArrangement = Arrangement.spacedBy(Space.x2_5),
                        ) {
                            Text(
                                "Your outline is saved on this device: every edit lands here before anything else.",
                                fontSize = FontSize.s400,
                                lineHeight = 20.sp,
                            )
                            Text(
                                "Sync isn't available in this app yet, so for now your notes stay on this device only.",
                                Modifier.testTag("sync-disabled-notice"),
                                fontSize = FontSize.s400,
                                lineHeight = 20.sp,
                            )
                            Text(
                                "Templates, import and more are on the way.",
                                color = colors.muted,
                                fontSize = FontSize.s400,
                                lineHeight = 20.sp,
                            )
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Space.x2_5), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            if (step > 0) {
                CascadeButton({ step-- }, Modifier.testTag("onboarding-back")) { Text("Back") }
            }
            CascadeButton(
                { if (step < last) step++ else onDone() },
                Modifier.testTag(if (step < last) "onboarding-continue" else "onboarding-start"),
                variant = ButtonVariant.Primary,
            ) {
                Text(if (step < last) "Continue" else "Start writing")
            }
        }
    }
}

@Composable
private fun Heading(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Space.x1_5)) {
        Text(title, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.6).sp)
        Text(description, color = Cascade.colors.muted, fontSize = FontSize.s400, lineHeight = 20.sp)
    }
}

@Composable
private fun TemplateCard(template: Template, checked: Boolean, onPick: () -> Unit) {
    val colors = Cascade.colors
    val shape = Radius.lg
    Row(
        Modifier
            .fillMaxWidth()
            .alpha(if (template.enabled) 1f else 0.4f)
            .clip(shape)
            .background(colors.white)
            .border(1.5.dp, if (checked) colors.borderStrong else Color.Transparent, shape)
            .clickable(enabled = template.enabled, role = Role.RadioButton, onClick = onPick)
            .semantics { selected = checked }
            .padding(Space.x3_5)
            .testTag("onboarding-template-${template.id}"),
        horizontalArrangement = Arrangement.spacedBy(Space.x3_5),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier.size(112.dp, 84.dp).background(colors.canvas, RoundedCornerShape(9.dp)).padding(Space.x3),
            verticalArrangement = Arrangement.spacedBy(Space.x1_5),
        ) {
            for ((depth, task, width) in template.preview) {
                Row(
                    Modifier.padding(start = Space.x2_5 * depth),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(6.dp).then(
                            if (task) Modifier.border(1.dp, colors.muted, Radius.full) else Modifier.background(colors.muted, Radius.full),
                        ),
                    )
                    Box(Modifier.fillMaxWidth(width).height(5.dp).background(colors.border, RoundedCornerShape(3.dp)))
                }
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.x1)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.x2), verticalAlignment = Alignment.CenterVertically) {
                Text(template.title, fontWeight = FontWeight.SemiBold)
                if (!template.enabled) Text("Coming soon", color = colors.muted, fontSize = FontSize.s200)
            }
            Text(
                template.description,
                Modifier.widthIn(max = 240.dp),
                color = colors.muted,
                fontSize = FontSize.s300,
                lineHeight = 18.sp,
            )
        }
    }
}
