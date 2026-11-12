package pe.edu.upc.safelab.boundedcontext.userprofiles.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pe.edu.upc.safelab.R
import pe.edu.upc.safelab.shared.infrastructure.data.MultiTenantStore
import pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model.ProfileChanges
import pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model.ProfileSaveResult
import pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model.UserProfile
import pe.edu.upc.safelab.common.buildAvatarInitials
import pe.edu.upc.safelab.common.buildDisplayName
import pe.edu.upc.safelab.common.tr

/** A native, simple profile page. View -> pencil -> edit -> save/cancel. */
@Composable
fun UserProfileScreen(
    profile: UserProfile,
    language: String,
    onOpenSubscription: () -> Unit,
    onSave: suspend (ProfileChanges) -> ProfileSaveResult
) {
    val scope = rememberCoroutineScope()
    var saving by remember(profile.userId) { mutableStateOf(false) }
    var editing by remember(profile.userId) { mutableStateOf(false) }
    var firstNames by remember(profile.userId) { mutableStateOf(profile.firstNames) }
    var lastNames by remember(profile.userId) { mutableStateOf(profile.lastNames) }
    var email by remember(profile.userId) { mutableStateOf(profile.email) }
    var phone by remember(profile.userId) { mutableStateOf(profile.phone) }
    var age by remember(profile.userId) { mutableStateOf(profile.age) }
    var city by remember(profile.userId) { mutableStateOf(profile.city) }
    var jobTitle by remember(profile.userId) { mutableStateOf(profile.jobTitle) }
    var message by remember(profile.userId) { mutableStateOf<String?>(null) }
    var error by remember(profile.userId) { mutableStateOf(false) }

    LaunchedEffect(profile) {
        if (!editing && !saving) {
            firstNames = profile.firstNames
            lastNames = profile.lastNames
            email = profile.email
            phone = profile.phone
            age = profile.age
            city = profile.city
            jobTitle = profile.jobTitle
        }
    }

    fun loadCurrentValues() {
        firstNames = profile.firstNames
        lastNames = profile.lastNames
        email = profile.email
        phone = profile.phone
        age = profile.age
        city = profile.city
        jobTitle = profile.jobTitle
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(shape = RoundedCornerShape(26.dp), color = Color.Transparent) {
                Column(
                    modifier = Modifier
                        .background(Brush.linearGradient(listOf(
                            MaterialTheme.colorScheme.surface,
                            Color(0xFF58C8C4).copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        )))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        tr(language, "My profile", "Mi perfil"),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        tr(language, "View and update your personal information.", "Consulta y actualiza tus datos personales."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(62.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                buildAvatarInitials(profile.firstNames, profile.lastNames),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                buildDisplayName(profile.firstNames, profile.lastNames),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "@${profile.username}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            tr(language, "Personal information", "Información personal"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        if (!editing) {
                            OutlinedButton(onClick = {
                                loadCurrentValues()
                                message = null
                                editing = true
                            }) {
                                Icon(painterResource(R.drawable.ic_profile_edit), contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.size(5.dp))
                                Text(tr(language, "Edit", "Editar"))
                            }
                        }
                    }

                    if (editing) {
                        ProfileInput(firstNames, { firstNames = it.take(70) }, tr(language, "First names", "Nombres"))
                        ProfileInput(lastNames, { lastNames = it.take(70) }, tr(language, "Last names", "Apellidos"))
                        ProfileInput(email, { email = it.take(254) }, tr(language, "Email", "Correo"), KeyboardType.Email)
                        ProfileInput(phone, { phone = it.filter(Char::isDigit).take(15) }, tr(language, "Mobile phone", "Celular"), KeyboardType.Phone)
                        ProfileInput(age, { age = it.filter(Char::isDigit).take(3) }, tr(language, "Age (optional)", "Edad (opcional)"), KeyboardType.Number)
                        ProfileInput(city, { city = it.take(60) }, tr(language, "City (optional)", "Ciudad (opcional)"))
                        ProfileInput(jobTitle, { jobTitle = it.take(60) }, tr(language, "Job title (optional)", "Cargo (opcional)"))

                        if (message != null) {
                            Text(
                                message.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    loadCurrentValues()
                                    editing = false
                                    message = null
                                }
                            ) { Text(tr(language, "Cancel", "Cancelar")) }
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    saving = true
                                    scope.launch {
                                    val result = try { onSave(ProfileChanges(
                                        firstNames = firstNames,
                                        lastNames = lastNames,
                                        email = email,
                                        phone = phone,
                                        age = age,
                                        city = city,
                                        jobTitle = jobTitle
                                    )) } catch (_: Exception) { ProfileSaveResult.API_ERROR }
                                    saving = false
                                    if (result == ProfileSaveResult.SUCCESS) {
                                        editing = false
                                        error = false
                                        message = tr(language, "Changes saved.", "Cambios guardados.")
                                    } else {
                                        error = true
                                        message = profileErrorMessage(result, language)
                                    }
                                    }
                                },
                                enabled = !saving
                            ) { Text(tr(language, "Save", "Guardar")) }
                        }
                    } else {
                        ProfileLine(tr(language, "First names", "Nombres"), profile.firstNames)
                        ProfileLine(tr(language, "Last names", "Apellidos"), profile.lastNames)
                        ProfileLine(tr(language, "Username", "Usuario"), profile.username)
                        ProfileLine(tr(language, "Email", "Correo"), profile.email)
                        ProfileLine(tr(language, "Mobile phone", "Celular"), profile.phone)
                        ProfileLine(tr(language, "Age", "Edad"), profile.age.ifBlank { "—" })
                        ProfileLine(tr(language, "City", "Ciudad"), profile.city.ifBlank { "—" })
                        ProfileLine(tr(language, "Job title", "Cargo"), profile.jobTitle.ifBlank { "—" })
                        ProfileLine(tr(language, "Account role", "Rol de cuenta"),
                            MultiTenantStore.roleName(profile.role, language))
                        val organizationId = MultiTenantStore.user(profile.userId)?.optString("organizationId").orEmpty()
                        if (organizationId.isNotBlank() && organizationId != "null") {
                            ProfileLine(tr(language, "Organization", "Empresa"), MultiTenantStore.orgName(organizationId))
                        }
                        if (message != null) {
                            Text(message.orEmpty(), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Billing is visible only to a manager or a platform administrator.
        if (!editing && MultiTenantStore.user(profile.userId)?.optString("role") in listOf("organizationAdmin", "platformAdmin")) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenSubscription),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(46.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(13.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_nav_reports),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                tr(language, "Subscription & billing", "Suscripción y pagos"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                tr(language, "Current plan, payment history and upcoming invoice.",
                                    "Plan actual, historial de pagos y próximo recibo."),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            painter = painterResource(R.drawable.ic_nav_chevron),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProfileInput(value: String, onChange: (String) -> Unit, label: String, type: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = type),
        shape = RoundedCornerShape(15.dp)
    )
}

private fun profileErrorMessage(result: ProfileSaveResult, language: String): String = when (result) {
    ProfileSaveResult.INVALID_FIRST_NAMES -> tr(language, "Use letters only for first names (2–70 characters).", "Escribe solo letras en nombres (2–70 caracteres).")
    ProfileSaveResult.INVALID_LAST_NAMES -> tr(language, "Use letters only for last names (2–70 characters).", "Escribe solo letras en apellidos (2–70 caracteres).")
    ProfileSaveResult.INVALID_EMAIL -> tr(language, "Enter a valid email from a supported provider.", "Ingresa un correo válido de un proveedor admitido.")
    ProfileSaveResult.EMAIL_IN_USE -> tr(language, "This email is already in use.", "Este correo ya está en uso.")
    ProfileSaveResult.INVALID_PHONE -> tr(language, "Enter 9 to 15 digits for the mobile phone.", "Ingresa de 9 a 15 dígitos para el celular.")
    ProfileSaveResult.INVALID_AGE -> tr(language, "Age must be between 1 and 120, or left blank.", "La edad debe estar entre 1 y 120, o dejarse vacía.")
    ProfileSaveResult.INVALID_CITY -> tr(language, "City must contain letters only (2–60 characters).", "La ciudad debe contener solo letras (2–60 caracteres).")
    ProfileSaveResult.INVALID_JOB_TITLE -> tr(language, "Job title must contain letters only (2–60 characters).", "El cargo debe contener solo letras (2–60 caracteres).")
    ProfileSaveResult.EMAIL_CHANGE_UNSUPPORTED -> tr(language, "Email changes are not enabled in this API version.", "El cambio de correo no está habilitado en esta versión de la API.")
    ProfileSaveResult.API_ERROR -> tr(language, "SafeLab API is not available. No data was saved.", "No se pudo conectar a SafeLab API. No se guardaron los datos.")
    ProfileSaveResult.NOT_FOUND -> tr(language, "Profile not found.", "Perfil no encontrado.")
    ProfileSaveResult.SUCCESS -> tr(language, "Changes saved.", "Cambios guardados.")
}
