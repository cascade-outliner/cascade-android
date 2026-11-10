package app.cascade.design

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.cascade.design.theme.Cascade
import app.cascade.design.theme.FontSize
import app.cascade.design.theme.Radius
import app.cascade.design.theme.Space

@Composable
fun <T : SlashMenuItem> SlashMenu(items: List<T>, onPick: (T) -> Unit, onDismiss: () -> Unit) {
    AnchoredPopup(onDismiss = onDismiss) {
        SlashMenuList(items, onPick, Modifier.width(248.dp).heightIn(max = 264.dp).verticalScroll(rememberScrollState()))
    }
}

@Composable
fun <T : SlashMenuItem> SlashMenuList(items: List<T>, onPick: (T) -> Unit, modifier: Modifier = Modifier) {
    val colors = Cascade.colors
    Column(modifier.padding(Space.x1_5).testTag("slash-menu")) {
        for ((group, entries) in groupSlashMenuItems(items)) {
            SectionLabel(group, Modifier.padding(start = Space.x2_5, end = Space.x2_5, top = Space.x2, bottom = Space.x1))
            for (command in entries) {
                val tint = if (command.danger) colors.danger else colors.ink
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(Radius.md)
                        .clickable(role = Role.Button) { onPick(command) }
                        .padding(Space.x2_5),
                    horizontalArrangement = Arrangement.spacedBy(Space.x2_5),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                        CascadeIcon(command.icon, size = 16.dp, tint = if (command.danger) tint else colors.muted)
                    }
                    Text(command.label, color = tint, fontSize = FontSize.s400)
                }
            }
        }
    }
}
