package pe.edu.upc.safelab.boundedcontext.identityaccess.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.model.AuthenticatedUser
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository.RegistrationRequest

@Composable
fun IdentityAccessEntryScreen(
    language: String,
    isDarkTheme: Boolean,
    onLanguageToggle: () -> Unit,
    onThemeToggle: () -> Unit,
    onAuthenticated: (AuthenticatedUser) -> Unit,
    onRegistered: (String, String, RegistrationRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by rememberSaveable { mutableStateOf("login") }

    if (mode == "register") {
        RegisterScreen(
            language = language,
            isDarkTheme = isDarkTheme,
            onLanguageToggle = onLanguageToggle,
            onThemeToggle = onThemeToggle,
            onBackToLogin = { mode = "login" },
            onRegistered = onRegistered,
            modifier = modifier
        )
    } else {
        LoginScreen(
            language = language,
            isDarkTheme = isDarkTheme,
            onLanguageToggle = onLanguageToggle,
            onThemeToggle = onThemeToggle,
            onOpenRegister = { mode = "register" },
            onAuthenticated = onAuthenticated,
            modifier = modifier
        )
    }
}