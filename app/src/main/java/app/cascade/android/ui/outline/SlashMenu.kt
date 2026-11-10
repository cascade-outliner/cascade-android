package app.cascade.android.ui.outline

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cascade.android.commands.SlashCommand
import app.cascade.android.commands.groupSlashCommands
import app.cascade.android.ui.components.AnchoredPopup
import app.cascade.android.ui.components.CascadeIcon
import app.cascade.android.ui.theme.Cascade
import app.cascade.android.ui.theme.FontSize

@Composable
fun SlashMenu(commands: List<SlashCommand>, onPick: (SlashCommand) -> Unit, onDismiss: () -> Unit) {
    val colors = Cascade.colors
    AnchoredPopup(onDismiss = onDismiss) {
        Column(
            Modifier
                .width(248.dp)
                .heightIn(max = 264.dp)
                .verticalScroll(rememberScrollState())
                .padding(6.dp)
                .testTag("slash-menu"),
        ) {
            for ((group, items) in groupSlashCommands(commands)) {
                Text(
                    group.uppercase(),
                    Modifier.padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 4.dp),
                    color = colors.muted,
                    fontSize = FontSize.s100,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp,
                )
                for (command in items) {
                    val tint = if (command.danger) colors.danger else colors.ink
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(role = Role.Button) { onPick(command) }
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
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
}
