package pe.edu.upc.safelab.boundedcontext.identityaccess.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.shared.infrastructure.data.MultiTenantStore
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository.RegistrationRequest
import pe.edu.upc.safelab.common.tr

/** Two registration modes, just as on SafeLab Web: company or invitation. */
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
    var type by remember { mutableStateOf("company") }
    var company by remember { mutableStateOf("") }
    var ruc by remember { mutableStateOf("") }
    var segment by remember { mutableStateOf("laboratory") }
    var corporateEmail by remember { mutableStateOf("") }
    var invitation by remember { mutableStateOf("") }
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var assignedUsername by remember { mutableStateOf<String?>(null) }
    val invited = MultiTenantStore.invitation(invitation)

    AuthContainer(modifier = modifier) {
        AuthTopActions(language, isDarkTheme, onLanguageToggle, onThemeToggle)
        Spacer(Modifier.height(15.dp))
        Text(tr(language, "Create your SafeLab account", "Crea tu cuenta SafeLab"),
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black,
            color = if (isDarkTheme) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onBackground)
        Text(tr(language, "Register your organization or join an existing one.",
            "Registra tu empresa o únete a una existente."),
            color = if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFFC6D4F0)
                    else MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(13.dp))
        Surface(shape = RoundedCornerShape(25.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (type == "company") Button(onClick = { type = "company" }, modifier = Modifier.weight(1f)) {
                        Text(tr(language, "Register a company", "Registrar empresa"))
                    } else OutlinedButton(onClick = { type = "company" }, modifier = Modifier.weight(1f)) {
                        Text(tr(language, "Register a company", "Registrar empresa"))
                    }
                    if (type == "invitation") Button(onClick = { type = "invitation" }, modifier = Modifier.weight(1f)) {
                        Text(tr(language, "I have an invitation", "Tengo una invitación"))
                    } else OutlinedButton(onClick = { type = "invitation" }, modifier = Modifier.weight(1f)) {
                        Text(tr(language, "I have an invitation", "Tengo una invitación"))
                    }
                }
                if (type == "company") {
                    Text(tr(language, "Company information", "Información empresarial"), fontWeight = FontWeight.Bold)
                    Field(company, { company = it }, tr(language, "Legal company name", "Razón social"))
                    Field(ruc, { ruc = it.filter(Char::isDigit).take(11) }, "RUC", KeyboardType.Number)
                    Text(tr(language, "Business segment", "Segmento empresarial"), style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = segment == "laboratory", onClick = { segment = "laboratory" },
                            label = { Text(tr(language, "Clinical laboratory", "Laboratorio clínico")) })
                        FilterChip(selected = segment == "pharmaceutical", onClick = { segment = "pharmaceutical" },
                            label = { Text(tr(language, "Pharmaceutical", "Farmacéutica")) })
                    }
                    Field(corporateEmail, { corporateEmail = it.trim() },
                        tr(language, "Corporate verification email", "Correo de verificación empresarial"), KeyboardType.Email)
                } else {
                    Text(tr(language, "Invitation details", "Datos de la invitación"), fontWeight = FontWeight.Bold)
                    Field(invitation, { invitation = it.uppercase().trim() },
                        tr(language, "Invitation code", "Código de invitación"))
                    if (invited != null) {
                        Text(MultiTenantStore.orgName(invited.optString("organizationId")), color = MaterialTheme.colorScheme.primary)
                        Text("${tr(language, "Role", "Rol")}: ${MultiTenantStore.roleName(invited.optString("role"), language)}")
                        Text("${tr(language, "Email", "Correo")}: ${invited.optString("email")}")
                    }
                }
                HorizontalDivider()
                Text(tr(language, "Personal information", "Información personal"), fontWeight = FontWeight.Bold)
                Field(first, { first = it.take(70) }, tr(language, "First names", "Nombres"))
                Field(last, { last = it.take(70) }, tr(language, "Last names", "Apellidos"))
                Field(phone, { phone = it.filter(Char::isDigit).take(15) }, tr(language, "Mobile phone", "Celular"), KeyboardType.Phone)
                if (type == "company") {
                    Field(email, { email = it.trim() }, tr(language, "Email", "Correo"), KeyboardType.Email)
                } else {
                    Text(tr(language, "The invitation email is used automatically.",
                        "El correo de la invitación se utiliza automáticamente."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedTextField(value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "Password", "Contraseña")) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation())
                OutlinedTextField(value = confirmation, onValueChange = { confirmation = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "Confirm password", "Confirmar contraseña")) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation())
                Row { Checkbox(checked = showPassword, onCheckedChange = { showPassword = it })
                    Text(tr(language, "Show password", "Mostrar contraseña"), modifier = Modifier.padding(top = 13.dp)) }
                Text(tr(language, "10+ characters, including a letter, a number and one of @ % & $.",
                    "10 o más caracteres: una letra, un número y uno de @ % & $."),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.error)
                Button(onClick = {
                    message = ""
                    val names = Regex("^[\\p{L} ]{2,70}$")
                    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
                    val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[@%&$])[A-Za-z0-9@%&$]{10,}$")
                    val emailToCheck = if (type == "company") email else invited?.optString("email").orEmpty()
                    when {
                        !names.matches(first.trim()) || !names.matches(last.trim()) ->
                            message = tr(language, "Enter valid names (letters only).", "Ingresa nombres válidos (solo letras).")
                        phone.length !in 9..15 -> message = tr(language, "Invalid phone number.", "Número de celular inválido.")
                        !emailRegex.matches(emailToCheck) -> message = tr(language, "Invalid email.", "Correo inválido.")
                        !passwordRegex.matches(password) || confirmation != password ->
                            message = tr(language, "Check the password rules and confirmation.", "Verifica las reglas y confirmación de contraseña.")
                        type == "invitation" && invited == null -> message = tr(language, "Invalid or expired invitation.", "Invitación inválida o vencida.")
                        type == "company" && (ruc.length != 11 || company.trim().length < 4 || !emailRegex.matches(corporateEmail)) ->
                            message = tr(language, "Review the company details, RUC and corporate email.",
                                "Verifica la empresa, el RUC y el correo corporativo.")
                        else -> {
                            val username = if (type == "company") MultiTenantStore.registerCompany(
                                company, ruc, segment, corporateEmail, first, last, phone, email, password
                            ) else MultiTenantStore.registerEmployee(invitation, first, last, phone, password)
                            if (username == null) message = tr(language, "Registration could not be completed. Check your details.",
                                "No se pudo completar el registro. Verifica tus datos.")
                            else assignedUsername = username
                        }
                    }
                }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text(tr(language, "Create account", "Crear cuenta"), fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
                    Text(tr(language, "Already have an account? Sign in", "¿Ya tienes cuenta? Iniciar sesión"))
                }
            }
        }
    }
    if (assignedUsername != null) AlertDialog(
        onDismissRequest = { assignedUsername = null; onBackToLogin() },
        title = { Text(tr(language, "Account pending activation", "Cuenta pendiente de activación")) },
        text = { Text(tr(language,
            "Your company or employee account requires verification. Username: ",
            "Tu cuenta empresarial o de empleado requiere verificación. Usuario: ") + assignedUsername) },
        confirmButton = { TextButton(onClick = { assignedUsername = null; onBackToLogin() }) {
            Text(tr(language, "Continue", "Continuar"))
        }}
    )
}

@Composable
private fun Field(value: String, onChange: (String) -> Unit, label: String, keyboard: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(value = value, onValueChange = onChange, modifier = Modifier.fillMaxWidth(),
        label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(14.dp))
}
