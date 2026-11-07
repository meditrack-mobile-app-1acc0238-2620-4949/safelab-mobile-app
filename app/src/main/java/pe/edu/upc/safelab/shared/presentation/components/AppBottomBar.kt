package pe.edu.upc.safelab.shared.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.shared.domain.model.MobileTab

@Composable
fun AppBottomBar(
    selectedTab: MobileTab,
    language: String,
    onTabSelected: (MobileTab) -> Unit,
    allowedTabs: Set<MobileTab> = MobileTab.entries.toSet()
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 5.dp
    ) {
        MobileTab.entries.filter { it in allowedTabs }.forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        painter = painterResource(tab.icon),
                        contentDescription = null, // Label below describes the destination.
                        modifier = Modifier.size(23.dp)
                    )
                },
                label = {
                    Text(tab.label(language), fontWeight = FontWeight.SemiBold, maxLines = 1)
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF4338CA),
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = Color(0xFFEDE9FE),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}