package roro.stellar.manager.ui.navigation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import roro.stellar.manager.ui.navigation.routes.MainScreen

private val DockHeight = 64.dp
private val DockRadius = 32.dp

@Composable
fun StandardBottomNavigation(
    selectedIndex: Int,
    onItemClick: (Int) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.red + scheme.background.green + scheme.background.blue < 1.5f
    val dock = if (dark) scheme.surfaceContainerHigh.copy(alpha = 0.86f)
    else scheme.surface.copy(alpha = 0.86f)
    val stroke = scheme.outline.copy(alpha = if (dark) 0.28f else 0.16f)

    Row(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .fillMaxWidth()
            .height(DockHeight)
            .clip(RoundedCornerShape(DockRadius))
            .background(dock)
            .border(0.8.dp, stroke, RoundedCornerShape(DockRadius))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MainScreen.entries.forEachIndexed { index, screen ->
            val selected = selectedIndex == index
            val color = if (selected) scheme.onSurface else scheme.onSurfaceVariant
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(28.dp))
                    .background(if (selected) scheme.onSurface.copy(alpha = 0.10f) else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onItemClick(index) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = if (selected) screen.iconFilled else screen.icon,
                    contentDescription = stringResource(screen.labelRes),
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(screen.labelRes),
                    color = color,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
fun StandardNavigationRail(
    selectedIndex: Int,
    onItemClick: (Int) -> Unit
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MainScreen.entries.forEachIndexed { index, screen ->
                val isSelected = selectedIndex == index

                NavigationRailItem(
                    icon = {
                        Icon(
                            imageVector = if (isSelected) screen.iconFilled else screen.icon,
                            contentDescription = stringResource(screen.labelRes),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(text = stringResource(screen.labelRes))
                    },
                    selected = isSelected,
                    onClick = { onItemClick(index) }
                )
            }
        }
    }
}
