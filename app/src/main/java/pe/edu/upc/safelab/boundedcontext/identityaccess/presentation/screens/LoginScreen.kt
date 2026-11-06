package pe.edu.upc.safelab.boundedcontext.identityaccess.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.safelab.R
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository.AuthenticationResult
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository.LocalIdentityAccessRepository
import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.model.AuthenticatedUser
import pe.edu.upc.safelab.common.tr

@Composable
fun LoginScreen(
    language: String,
    isDarkTheme: Boolean,
    onLanguageToggle: () -> Unit,
    onThemeToggle: () -> Unit,
    onOpenRegister: () -> Unit,
    onAuthenticated: (AuthenticatedUser) -> Unit,
    modifier: Modifier = Modifier
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val repository = remember { LocalIdentityAccessRepository() }

    AuthContainer(modifier = modifier) {
        AuthTopActions(language, isDarkTheme, onLanguageToggle, onThemeToggle)
        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .size(82.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.safelab_shield),
                contentDescription = "SafeLab logo",
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "SafeLab",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = tr(language, "Clinical cold chain monitoring", "Monitoreo de la cadena de frío clínica"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = tr(language, "Sign in", "Iniciar sesión"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )

                OutlinedTextField(
                    value = identifier,
                    onValueChange = {
                        identifier = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "Email or username", "Correo o usuario")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(15.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "Password", "Contraseña")) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(15.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        when (val result = repository.authenticate(identifier, password)) {
                            is AuthenticationResult.Success -> {
                                errorMessage = null
                                onAuthenticated(result.user)
                            }
                            AuthenticationResult.PendingActivation -> {
                                errorMessage = tr(
                                    language,
                                    "This account is pending activation. Verify your email and complete the activation flow.",
                                    "Esta cuenta está pendiente de activación. Verifica tu correo y completa el flujo de activación."
                                )
                            }
                            AuthenticationResult.InvalidCredentials -> {
                                errorMessage = tr(
                                    language,
                                    "Invalid credentials. Verify your email or username and password.",
                                    "Credenciales inválidas. Verifica tu correo o usuario y tu contraseña."
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = tr(language, "Sign in", "Iniciar sesión"), fontWeight = FontWeight.Black, fontSize = 16.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = { }) {
                        Text(tr(language, "Forgot password?", "¿Olvidaste tu contraseña?"))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr(language, "Don't have an account?", "¿No tienes cuenta?"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = tr(language, " Register", " Registrarse"),
                        modifier = Modifier.clickable(onClick = onOpenRegister),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AuthContainer(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF4F7FF), MaterialTheme.colorScheme.background, Color(0xFFEDE9FE))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .background(Color(0xFF4F35E8).copy(alpha = 0.08f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
fun AuthTopActions(
    language: String,
    isDarkTheme: Boolean,
    onLanguageToggle: () -> Unit,
    onThemeToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isDarkTheme) "☀" else "☾",
            modifier = Modifier.clickable(onClick = onThemeToggle).padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        Surface(
            modifier = Modifier.clickable(onClick = onLanguageToggle),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = language,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }
    }
}