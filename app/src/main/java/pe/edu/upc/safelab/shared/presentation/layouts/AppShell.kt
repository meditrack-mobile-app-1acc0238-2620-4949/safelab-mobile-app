package pe.edu.upc.safelab.shared.presentation.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.edu.upc.safelab.shared.domain.model.AppModule
import pe.edu.upc.safelab.shared.presentation.components.AppSidebar
import pe.edu.upc.safelab.shared.presentation.components.AppTopBar

@Composable
fun AppShell(
    selectedModule: AppModule,
    language: String,
    isDarkTheme: Boolean,
    onModuleSelected: (AppModule) -> Unit,
    onLanguageToggle: () -> Unit,
    onThemeToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color.Transparent
            ) {
                AppSidebar(
                    selectedModule = selectedModule,
                    onModuleSelected = { module ->
                        onModuleSelected(module)
                        scope.launch { drawerState.close() }
                    },
                    onSignOut = {
                        onModuleSelected(AppModule.IDENTITY_ACCESS)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AppTopBar(
                    currentModuleTitle = selectedModule.shortLabel,
                    language = language,
                    isDarkTheme = isDarkTheme,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onLanguageToggle = onLanguageToggle,
                    onThemeToggle = onThemeToggle
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(Modifier)
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    content()
                }
            }
        }
    }
}
