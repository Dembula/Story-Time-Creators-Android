package online.storytime.creators.features.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.storytime.creators.core.Session
import online.storytime.creators.core.theme.STColor
import online.storytime.creators.core.theme.stIcon

/**
 * Shown when a marketplace-style tool was launched from within a project so the
 * creator can jump back to the originating phase hub — mirrors iOS ToolReturnBar.
 */
@Composable
fun ToolReturnBar() {
    val router = Session.router
    val tool = router.selectedTool ?: return
    val returnTo = router.toolReturnDestination ?: return
    if (!tool.isMarketplaceStyle) return

    Row(
        Modifier
            .fillMaxWidth()
            .background(STColor.surfaceElevated)
            .clickable { router.leaveToolDetail() }
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(stIcon("chevron.left"), null, tint = STColor.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "Back to ${returnTo.title}",
            color = STColor.primary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
        )
    }
}
