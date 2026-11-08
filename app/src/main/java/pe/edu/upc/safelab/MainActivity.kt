package pe.edu.upc.safelab

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pe.edu.upc.safelab.shared.infrastructure.http.SafeLabApi
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import pe.edu.upc.safelab.navigation.SafeLabApp
import pe.edu.upc.safelab.shared.infrastructure.data.MultiTenantStore
import pe.edu.upc.safelab.ui.theme.SafeLabTheme

class MainActivity : ComponentActivity() {
    override fun onResume() {
        super.onResume()
        if (SafeLabApi.authenticated) lifecycleScope.launch {
            try { MultiTenantStore.refreshFromBackend() } catch (_: Exception) { /* Mantener pantalla visible hasta poder reconectar */ }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MultiTenantStore.initialize(applicationContext)

        setContent {
            var darkTheme by rememberSaveable { mutableStateOf(false) }

            SafeLabTheme(darkTheme = darkTheme) {
                SafeLabApp(
                    isDarkTheme = darkTheme,
                    onThemeToggle = { darkTheme = !darkTheme }
                )
            }
        }
    }
}