package pe.edu.upc.safelab.boundedcontext.identityaccess.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository.LocalIdentityAccessRepository
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository.RegistrationRequest
import pe.edu.upc.safelab.common.tr

@Composable
fun RegisterScreen(
    language: String,
    isDarkTheme: Boolean,
    onLanguageToggle: () -> Unit,
    onThemeToggle: () -> Unit,
    onBackToLogin: () -> Unit,
    onRegistered: (String, String, RegistrationRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { LocalIdentityAccessRepository() }
    var firstNames by remember { mutableStateOf("") }
    var lastNames by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    AuthContainer(modifier = modifier) {
        AuthTopActions(language, isDarkTheme, onLanguageToggle, onThemeToggle)

        Text(
            text = tr(language, "Create account", "Crear cuenta"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 16.dp, bottom = 18.dp)
        )

        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = firstNames,
                    onValueChange = { firstNames = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "First names", "Nombres")) },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp)
                )
                OutlinedTextField(
                    value = lastNames,
                    onValueChange = { lastNames = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "Last names", "Apellidos")) },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.filter(Char::isDigit).take(15); errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "Mobile phone", "Celular")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.trim(); errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "Email", "Correo")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "Password", "Contraseña")) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "Confirm password", "Confirmar contraseña")) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp)
                )

                Text(
                    text = tr(
                        language,
                        "Password must have at least 10 characters and include letters, numbers and one of these symbols: @ % & $",
                        "La contraseña debe tener al menos 10 caracteres e incluir letras, números y uno de estos símbolos: @ % & $"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (errorMessage != null) {
                    Text(errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val validation = validateRegistration(
                            firstNames = firstNames,
                            lastNames = lastNames,
                            phone = phone,
                            email = email,
                            password = password,
                            confirmPassword = confirmPassword,
                            language = language
                        )

                        if (validation != null) {
                            errorMessage = validation
                        } else {
                            val request = RegistrationRequest(
                                firstNames = firstNames,
                                lastNames = lastNames,
                                phone = phone,
                                email = email,
                                password = password
                            )
                            val result = repository.register(request)
                            if (result.successful) {
                                if (result.userId != null && result.generatedUsername != null) {
                                    onRegistered(result.userId, result.generatedUsername, request)
                                }
                                successMessage = tr(
                                    language,
                                    "Account created. Check your email to verify and activate your account. Access will remain pending until the activation flow is completed.",
                                    "Cuenta creada. Revisa tu correo para verificar y activar tu cuenta. El acceso seguirá pendiente hasta completar el flujo de activación."
                                )
                            } else {
                                errorMessage = tr(language, result.message, when(result.message) {
                                    "A user with this email already exists." -> "Ya existe un usuario con este correo."
                                    "The generated username is already in use. Try another name combination." -> "El usuario generado ya está en uso. Intenta con otra combinación de nombres."
                                    else -> result.message
                                })
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(tr(language, "Create account", "Crear cuenta"), fontWeight = FontWeight.Black, fontSize = 16.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr(language, "Already have an account?", "¿Ya tienes cuenta?"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = onBackToLogin) {
                        Text(tr(language, "Sign in", "Iniciar sesión"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (successMessage != null) {
        AlertDialog(
            onDismissRequest = {
                successMessage = null
                onBackToLogin()
            },
            confirmButton = {
                TextButton(onClick = {
                    successMessage = null
                    onBackToLogin()
                }) {
                    Text(tr(language, "Continue", "Continuar"))
                }
            },
            title = { Text(tr(language, "Verification required", "Verificación requerida"), fontWeight = FontWeight.Bold) },
            text = { Text(successMessage.orEmpty()) }
        )
    }
}

private fun validateRegistration(
    firstNames: String,
    lastNames: String,
    phone: String,
    email: String,
    password: String,
    confirmPassword: String,
    language: String
): String? {
    val nameRegex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,}$")
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@(?:gmail|hotmail|outlook|yahoo|icloud|protonmail|live)\\.com$")
    val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@%&$])[A-Za-z\\d@%&$]{10,}$")

    return when {
        !nameRegex.matches(firstNames.trim()) -> tr(language, "Enter valid first names.", "Ingresa nombres válidos.")
        !nameRegex.matches(lastNames.trim()) -> tr(language, "Enter valid last names.", "Ingresa apellidos válidos.")
        phone.length !in 9..15 -> tr(language, "Enter a valid mobile phone number.", "Ingresa un número de celular válido.")
        !emailRegex.matches(email.trim().lowercase()) -> tr(language, "Enter a valid email from a supported provider.", "Ingresa un correo válido de un proveedor admitido.")
        !passwordRegex.matches(password) -> tr(language, "Password does not meet the required format.", "La contraseña no cumple con el formato requerido.")
        password != confirmPassword -> tr(language, "Passwords do not match.", "Las contraseñas no coinciden.")
        else -> null
    }
}