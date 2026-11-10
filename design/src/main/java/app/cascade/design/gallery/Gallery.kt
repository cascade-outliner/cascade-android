package app.cascade.design.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import app.cascade.design.ButtonVariant
import app.cascade.design.Calendar
import app.cascade.design.CaptureBar
import app.cascade.design.CascadeButton
import app.cascade.design.CascadeIcon
import app.cascade.design.CascadeIconButton
import app.cascade.design.ChoiceChip
import app.cascade.design.EmptyStateCard
import app.cascade.design.Kbd
import app.cascade.design.LogoMark
import app.cascade.design.PaletteGroup
import app.cascade.design.PaletteItem
import app.cascade.design.Pill
import app.cascade.design.PillTone
import app.cascade.design.R
import app.cascade.design.SectionLabel
import app.cascade.design.SheetDivider
import app.cascade.design.SheetItem
import app.cascade.design.SheetSection
import app.cascade.design.SlashMenuItem
import app.cascade.design.SlashMenuList
import app.cascade.design.Stepper
import app.cascade.design.outliner.Breadcrumbs
import app.cascade.design.outliner.Bullet
import app.cascade.design.outliner.Chevron
import app.cascade.design.outliner.Crumb
import app.cascade.design.outliner.DaySwitcher
import app.cascade.design.outliner.OutlinerContent
import app.cascade.design.outliner.OutlinerLabel
import app.cascade.design.outliner.OutlinerRow
import app.cascade.design.outliner.PeriodStrip
import app.cascade.design.outliner.Strip
import app.cascade.design.outliner.StripItem
import app.cascade.design.outliner.TaskMarker
import app.cascade.design.outliner.ZoomTitle
import app.cascade.design.theme.BorderWidth
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.CascadeTheme
import app.cascade.design.theme.FontSize
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space
import java.time.LocalDate

class GallerySection(val name: String, val content: @Composable () -> Unit)

private data class SampleItem(
    override val id: String,
    override val group: String,
    override val label: String,
    override val icon: Int,
    override val danger: Boolean = false,
) : SlashMenuItem

private val sampleItems = listOf(
    SampleItem("task", "Convert", "Turn into task", R.drawable.ic_circle),
    SampleItem("today", "Due date", "Due today", R.drawable.ic_calendar_blank),
    SampleItem("tomorrow", "Due date", "Due tomorrow", R.drawable.ic_calendar_blank),
    SampleItem("zoom", "Node", "Zoom in", R.drawable.ic_magnifying_glass_plus),
    SampleItem("delete", "Node", "Delete", R.drawable.ic_trash, danger = true),
)

private val sampleDay: LocalDate = LocalDate.of(2026, 11, 10)

@Composable
private fun Swatch(name: String, color: Color) {
    Column(Modifier.width(76.dp), verticalArrangement = Arrangement.spacedBy(Space.x1)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(color, Radius.sm)
                .border(BorderWidth.thin, Cascade.colors.border, Radius.sm),
        )
        Text(name, color = Cascade.colors.muted, fontSize = FontSize.s100, maxLines = 1)
    }
}

@Composable
private fun TypeSample(name: String, size: TextUnit, weight: FontWeight = FontWeight.Normal) {
    Row(horizontalArrangement = Arrangement.spacedBy(Space.x3), verticalAlignment = Alignment.CenterVertically) {
        Text(name, Modifier.width(44.dp), color = Cascade.colors.muted, fontSize = FontSize.s200)
        Text("Everything is a line", fontSize = size, fontWeight = weight)
    }
}

@Composable
private fun SpaceSample(name: String, size: Dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(Space.x3), verticalAlignment = Alignment.CenterVertically) {
        Text(name, Modifier.width(44.dp), color = Cascade.colors.muted, fontSize = FontSize.s200)
        Box(Modifier.size(size, Space.x3).background(Cascade.colors.info, Radius.full))
    }
}

@OptIn(ExperimentalLayoutApi::class)
val gallerySections: List<GallerySection> = listOf(
    GallerySection("Colors") {
        val c = Cascade.colors
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.x2), verticalArrangement = Arrangement.spacedBy(Space.x2)) {
            Swatch("canvas", c.canvas)
            Swatch("white", c.white)
            Swatch("surface", c.surface)
            Swatch("ink", c.ink)
            Swatch("primary", c.primary)
            Swatch("muted", c.muted)
            Swatch("placeholder", c.placeholder)
            Swatch("border", c.border)
            Swatch("inkSubtle", c.inkSubtle)
            Swatch("info", c.info)
            Swatch("infoMuted", c.infoMuted)
            Swatch("highlight", c.highlight)
            Swatch("danger", c.danger)
        }
    },
    GallerySection("Typography") {
        Column(verticalArrangement = Arrangement.spacedBy(Space.x1_5)) {
            TypeSample("s800", FontSize.s800, FontWeight.SemiBold)
            TypeSample("s700", FontSize.s700, FontWeight.SemiBold)
            TypeSample("s600", FontSize.s600)
            TypeSample("s400", FontSize.s400)
            TypeSample("s300", FontSize.s300)
            TypeSample("s200", FontSize.s200, FontWeight.Medium)
            TypeSample("s100", FontSize.s100, FontWeight.Medium)
        }
    },
    GallerySection("Space and radius") {
        Column(verticalArrangement = Arrangement.spacedBy(Space.x1_5)) {
            SpaceSample("x1", Space.x1)
            SpaceSample("x2", Space.x2)
            SpaceSample("x3", Space.x3)
            SpaceSample("x4", Space.x4)
            SpaceSample("x6", Space.x6)
            SpaceSample("x8", Space.x8)
            Row(Modifier.padding(top = Space.x2), horizontalArrangement = Arrangement.spacedBy(Space.x3)) {
                for ((name, shape) in listOf("sm" to Radius.sm, "md" to Radius.md, "lg" to Radius.lg, "xl" to Radius.xl, "full" to Radius.full)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Space.x1)) {
                        Box(Modifier.size(48.dp).background(Cascade.colors.primaryMuted, shape).border(BorderWidth.thin, Cascade.colors.border, shape))
                        Text(name, color = Cascade.colors.muted, fontSize = FontSize.s100)
                    }
                }
            }
        }
    },
    GallerySection("Buttons") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.x2), verticalArrangement = Arrangement.spacedBy(Space.x2)) {
            CascadeButton({}, variant = ButtonVariant.Primary) { Text("Primary") }
            CascadeButton({}) { Text("Secondary") }
            CascadeButton({}, variant = ButtonVariant.Ghost) { Text("Ghost") }
            CascadeButton({}, variant = ButtonVariant.Primary, enabled = false) { Text("Disabled") }
            CascadeButton({}) {
                CascadeIcon(R.drawable.ic_plus_bold, size = 13.dp)
                Text("With icon")
            }
            CascadeIconButton(R.drawable.ic_magnifying_glass, "Search", {}, bordered = true)
            CascadeIconButton(R.drawable.ic_plus_bold, "Add", {}, background = Cascade.colors.primaryMuted, tint = Cascade.colors.primary)
            CascadeIconButton(R.drawable.ic_arrow_up_bold, "Send", {}, background = Cascade.colors.primary, tint = Cascade.colors.onPrimary)
        }
    },
    GallerySection("Pills, labels, keys") {
        Column(verticalArrangement = Arrangement.spacedBy(Space.x3)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.x2), verticalAlignment = Alignment.CenterVertically) {
                Pill("Neutral")
                Pill("Today", tone = PillTone.Primary)
                Pill("Nov 17", tone = PillTone.Info)
                Kbd("Hold")
                Kbd("Drag")
                LogoMark()
            }
            SectionLabel("Section label")
            Stepper(listOf("How it works", "Start with", "Heads up"), current = 1)
        }
    },
    GallerySection("Outliner rows") {
        Column {
            var open by remember { mutableStateOf(true) }
            var done by remember { mutableStateOf(false) }
            var text by remember { mutableStateOf("Email the press list") }
            OutlinerRow(depth = 0) {
                Chevron(open = open, visible = true, onClick = { open = !open })
                Bullet(collapsed = !open, onClick = {})
                OutlinerLabel("Launch prep", Modifier.weight(1f))
            }
            if (open) {
                OutlinerRow(depth = 1, active = true) {
                    Chevron(open = false, visible = false, onClick = {})
                    Bullet(collapsed = false, onClick = {})
                    TaskMarker(done = done, onToggle = { done = !done }, modifier = Modifier.padding(top = Space.x1))
                    OutlinerContent<SampleItem>(text, { text = it }, Modifier.weight(1f))
                    Pill("Today", Modifier.padding(start = Space.x2, top = Space.x2_5), tone = PillTone.Primary)
                }
                OutlinerRow(depth = 1) {
                    Chevron(open = false, visible = false, onClick = {})
                    Bullet(collapsed = false, onClick = {})
                    TaskMarker(done = true, onToggle = {}, modifier = Modifier.padding(top = Space.x1))
                    OutlinerLabel("Write the announcement", Modifier.weight(1f))
                }
            }
            OutlinerRow(depth = 0, dragging = true) {
                Chevron(open = false, visible = true, onClick = {})
                Bullet(collapsed = true, onClick = {})
                OutlinerLabel("Reading list (dragging)", Modifier.weight(1f))
            }
        }
    },
    GallerySection("Zoom title and breadcrumbs") {
        Column {
            Breadcrumbs(
                listOf(Crumb("a", "Daily nodes"), Crumb("b", "2026"), Crumb("c", "November"), Crumb("d", "Today")),
                onHome = {},
                onCrumb = {},
            )
            ZoomTitle("Email the press list", readOnly = true)
        }
    },
    GallerySection("Day switcher and period strip") {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Space.x2)) {
            var selected by remember { mutableIntStateOf(1) }
            DaySwitcher(
                label = "Today",
                active = true,
                date = sampleDay,
                marked = { it.dayOfMonth % 4 == 0 },
                onToday = {},
                onOlder = {},
                onNewer = {},
                onPick = {},
            )
            PeriodStrip(
                Strip(
                    kind = "day",
                    selected = selected,
                    items = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").mapIndexed { index, name ->
                        StripItem(name, (9 + index).toString(), marked = index % 3 == 1, onPick = { selected = index })
                    },
                    onShift = {},
                ),
            )
        }
    },
    GallerySection("Calendar") {
        var value by remember { mutableStateOf(sampleDay) }
        Calendar(value = value, onPick = { value = it }, today = sampleDay.minusDays(3), marked = { it.dayOfMonth % 5 == 0 })
    },
    GallerySection("Capture bar") {
        CaptureBar<SampleItem>(onSubmit = { _, _ -> }, focusRequester = remember { FocusRequester() }, slashItems = sampleItems)
    },
    GallerySection("Slash menu") {
        Box(Modifier.width(248.dp).background(Cascade.colors.white, Radius.lg).border(BorderWidth.thin, Cascade.colors.border, Radius.lg)) {
            SlashMenuList(sampleItems, onPick = {})
        }
    },
    GallerySection("Sheet") {
        Column(Modifier.background(Cascade.colors.white, Radius.xl).padding(Space.x3)) {
            var task by remember { mutableStateOf(true) }
            SheetSection("Convert into")
            Row(Modifier.padding(horizontal = Space.x3), horizontalArrangement = Arrangement.spacedBy(Space.x2)) {
                ChoiceChip("Text", selected = !task, onClick = { task = false }, icon = R.drawable.ic_dot)
                ChoiceChip("Task", selected = task, onClick = { task = true }, icon = R.drawable.ic_circle)
                ChoiceChip("Disabled", selected = false, onClick = {}, enabled = false)
            }
            SheetDivider()
            SheetItem("Zoom in", R.drawable.ic_magnifying_glass_plus, {})
            SheetItem("Duplicate with children", R.drawable.ic_copy, {}, enabled = false)
            SheetItem("Delete", R.drawable.ic_trash, {}, danger = true)
        }
    },
    GallerySection("Palette") {
        Column {
            PaletteGroup("Nodes")
            PaletteItem("Book the venue", R.drawable.ic_circle, {}, ranges = listOf(9..13), detail = "Launch prep")
            PaletteItem("Venue shortlist", R.drawable.ic_dot, {}, ranges = listOf(0..4))
            PaletteGroup("Create")
            PaletteItem("New node “venue”", R.drawable.ic_plus_bold, {})
        }
    },
    GallerySection("Empty state") {
        EmptyStateCard(
            R.drawable.ic_list_dashes,
            "Nothing here yet",
            "Start typing to make the first node.",
            action = "Write the first line",
        )
    },
)

@Composable
fun GallerySectionFrame(section: GallerySection, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().background(Cascade.colors.canvas).padding(Space.x4),
        verticalArrangement = Arrangement.spacedBy(Space.x3),
    ) {
        Text(section.name, fontSize = FontSize.s700, fontWeight = FontWeight.SemiBold)
        section.content()
    }
}

@Composable
fun DesignGallery(modifier: Modifier = Modifier) {
    var dark by remember { mutableStateOf<Boolean?>(null) }
    val content = @Composable {
        LazyColumn(
            modifier.fillMaxSize().background(Cascade.colors.canvas).windowInsetsPadding(WindowInsets.safeDrawing),
            contentPadding = PaddingValues(bottom = Space.x8),
        ) {
            item {
                Row(Modifier.padding(Space.x4), horizontalArrangement = Arrangement.spacedBy(Space.x2)) {
                    ChoiceChip("System", selected = dark == null, onClick = { dark = null })
                    ChoiceChip("Light", selected = dark == false, onClick = { dark = false })
                    ChoiceChip("Dark", selected = dark == true, onClick = { dark = true })
                }
            }
            items(gallerySections, key = { it.name }) { GallerySectionFrame(it) }
        }
    }
    when (val forced = dark) {
        null -> CascadeTheme(content = content)
        else -> CascadeTheme(dark = forced, content = content)
    }
}

private class SectionProvider : PreviewParameterProvider<GallerySection> {
    override val values = gallerySections.asSequence()
}

@PreviewLightDark
@Composable
private fun GalleryPreview(@PreviewParameter(SectionProvider::class) section: GallerySection) {
    CascadeTheme { GallerySectionFrame(section) }
}
