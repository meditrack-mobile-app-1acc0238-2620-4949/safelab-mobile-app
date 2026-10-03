package pe.edu.upc.safelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import pe.edu.upc.safelab.navigation.SafeLabApp
import pe.edu.upc.safelab.ui.theme.SafeLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
