package pe.edu.upc.safelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import pe.edu.upc.safelab.ui.theme.SafeLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SafeLabTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Intentionally blank in develop.
                    // Feature branches will implement and connect the BC screens.
                }
            }
        }
    }
}
