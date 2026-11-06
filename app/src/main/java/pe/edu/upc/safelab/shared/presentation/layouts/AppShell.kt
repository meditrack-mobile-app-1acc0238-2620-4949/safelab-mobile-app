package pe.edu.upc.safelab.shared.presentation.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pe.edu.upc.safelab.shared.domain.model.MobileTab
import pe.edu.upc.safelab.shared.presentation.components.AppBottomBar
import pe.edu.upc.safelab.shared.presentation.components.AppTopBar

// A single Scaffold owns the native mobile navigation, so every module has the same layout.
@Composable
fun AppShell(
    selectedTab: MobileTab,
    title: String,
    language: String,
    isDarkTheme: Boolean,
    onTabSelected: (MobileTab) -> Unit,
    onBack: (() -> Unit)?,
    onLanguageToggle: () -> Unit,
    onThemeToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(title, language, isDarkTheme, onBack, onLanguageToggle, onThemeToggle)
        },
        bottomBar = {
            AppBottomBar(selectedTab, language, onTabSelected)
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            content()
        }
    }
}