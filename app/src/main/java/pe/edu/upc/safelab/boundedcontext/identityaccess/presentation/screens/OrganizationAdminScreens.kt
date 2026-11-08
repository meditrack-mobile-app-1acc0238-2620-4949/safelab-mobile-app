package pe.edu.upc.safelab.boundedcontext.identityaccess.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import pe.edu.upc.safelab.shared.infrastructure.data.MultiTenantStore
import pe.edu.upc.safelab.shared.presentation.screens.*
import pe.edu.upc.safelab.common.tr

@Composable
fun OrganizationUsersScreen(manager: JSONObject, language: String) {
    val scope = rememberCoroutineScope()
    val version = MultiTenantStore.version
    val orgId = manager.optString("organizationId")
    val staff = remember(version, orgId) { MultiTenantStore.employees(orgId) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var section by remember { mutableStateOf("employees") }
    val selectedUser = staff.firstOrNull { it.optString("id") == selectedId }

    // A separate mobile view keeps the 21 switches away from the employee list.
    // No new BC or duplicate permission logic is needed.
    if (selectedUser != null) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            item {
                EmployeePermissionEditor(manager, selectedUser, language, onClose = { selectedId = null })
            }
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { PageBanner(tr(language, "Users & Permissions", "Usuarios y permisos"),
            tr(language, "Manage your organization's staff, invitations, permissions and assignments.",
                "Administra empleados, invitaciones, permisos y asignaciones de tu empresa.")) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(tr(language, "Active employees", "Empleados activos"), staff.count { it.optString("status") == "active" }.toString(),
                    tr(language, "Current staff", "Personal actual"), Color(0xFF10B981), Modifier.weight(1f))
                MetricTile(tr(language, "Available seats", "Cupos disponibles"),
                    (MultiTenantStore.seatCapacity(orgId) - MultiTenantStore.usedSeats(orgId)).coerceAtLeast(0).toString(),
                    tr(language, "Base plan + extras", "Plan base + extras"), Color(0xFF4F35E8), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf("employees" to tr(language, "Employees", "Empleados"),
                    "invitations" to tr(language, "Invitations", "Invitaciones"),
                    "permissions" to tr(language, "Permissions", "Permisos")).forEach { (id, label) ->
                    if (section == id) Button(onClick = { section = id }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(5.dp)) {
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    } else OutlinedButton(onClick = { section = id }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(5.dp)) {
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        when (section) {
            "employees" -> {
                item { Text(tr(language, "Employees in your organization", "Empleados de tu empresa"),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(staff, key = { it.optString("id") }) { user ->
                    InfoCard(user.optString("fullName"),
                        "${MultiTenantStore.roleName(user.optString("role"), language)} · ${user.optString("email")}",
                        onClick = { selectedId = user.optString("id"); section = "permissions" },
                        trailing = { StatusPill(user.optString("status"), language) })
                }
            }
            "invitations" -> {
                item { InvitationCreator(manager, language) }
                items(MultiTenantStore.items("invitations").filter { it.optString("organizationId") == orgId }) { invite ->
                    InfoCard(invite.optString("email"),
                        "${MultiTenantStore.roleName(invite.optString("role"), language)} · ${invite.optString("code")}", trailing = {
                            Column(horizontalAlignment = Alignment.End) {
                                StatusPill(invite.optString("status"), language)
                                if (invite.optString("status") == "pending") TextButton(onClick = {
                                    scope.launch {
                                        try { MultiTenantStore.cancelInvitationRemote(manager, invite.optString("id")) }
                                        catch (_: Exception) { /* error visible al recargar */ }
                                    }
                                }) { Text(tr(language, "Cancel", "Cancelar")) }
                            }
                        })
                }
            }
            else -> {
                item { Text(tr(language, "Select an employee", "Selecciona un empleado"),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(staff) { employee ->
                    InfoCard(employee.optString("fullName"), MultiTenantStore.roleName(employee.optString("role"), language),
                        onClick = { selectedId = employee.optString("id") })
                }
            }
        }
    }
}

@Composable
private fun InvitationCreator(manager: JSONObject, language: String) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    val segment = MultiTenantStore.organization(manager.optString("organizationId"))?.optString("segment").orEmpty()
    val roles = MultiTenantStore.availableRoles(segment)
    var selectedRole by remember { mutableStateOf(roles.firstOrNull().orEmpty()) }
    var expanded by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(tr(language, "Invite an employee", "Invitar empleado"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(email, { email = it.trim() }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, label = { Text(tr(language, "Employee email", "Correo del empleado")) })
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(MultiTenantStore.roleName(selectedRole, language) + " ▾")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                roles.forEach { role -> DropdownMenuItem(text = { Text(MultiTenantStore.roleName(role, language)) },
                    onClick = { selectedRole = role; expanded = false }) }
            }
        }
        Button(onClick = {
            scope.launch {
                try {
                    notice = MultiTenantStore.inviteRemote(manager, email, selectedRole)?.let { code ->
                        tr(language, "Invitation created. Code: ", "Invitación creada. Código: ") + code
                    } ?: tr(language, "Invalid email, role or no available seats.", "Correo o rol inválido, o sin cupos disponibles.")
                } catch(ex: Exception) { notice = ex.message ?: "Error de SafeLab API" }
            }
        }) { Text(tr(language, "Create invitation", "Crear invitación")) }
        if (notice.isNotBlank()) Text(notice, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary)
        Text(tr(language, "Invitations are individual, valid for 72 hours and reserve one seat.",
            "Las invitaciones son individuales, duran 72 horas y reservan un cupo."),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmployeePermissionEditor(manager: JSONObject, employee: JSONObject, language: String, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    val version = MultiTenantStore.version
    val userId = employee.optString("id")
    val role = employee.optString("role")
    val saved = remember(version, userId) { MultiTenantStore.employeePermissions(employee) }
    val draft = remember(version, userId) { mutableStateMapOf<String, Boolean>().apply { putAll(saved) } }
    val grantable = MultiTenantStore.grantable(role)
    val dirty = saved.any { (permission, active) -> draft[permission] != active }
    var notice by remember(userId) { mutableStateOf("") }
    var confirmDiscard by remember { mutableStateOf(false) }
    var confirmExit by remember { mutableStateOf(false) }
    val requestExit = { if (dirty) confirmExit = true else onClose() }
    BackHandler { requestExit() }
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        TextButton(onClick = requestExit) {
            Text("← " + tr(language, "Back to employees", "Volver a empleados"))
        }
        PageBanner(tr(language, "Employee permissions", "Permisos del empleado"),
            tr(language, "Review access before saving any changes.",
                "Revisa los accesos antes de guardar los cambios."))
        Text(employee.optString("fullName"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(MultiTenantStore.roleName(role, language), color = MaterialTheme.colorScheme.onSurfaceVariant)
        MultiTenantStore.allPermissionGroups().forEach { (group, permissions) ->
            HorizontalDivider()
            Text(MultiTenantStore.text(language, "group_$group"), fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            permissions.forEach { permission ->
                val key = permission.replace('.', '_')
                val editable = permission in grantable
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(MultiTenantStore.text(language, "permission_$key"), fontWeight = FontWeight.SemiBold)
                        Text(MultiTenantStore.text(language, "description_$key"),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = draft[permission] == true, enabled = editable,
                        onCheckedChange = { draft[permission] = it })
                }
            }
        }
        if (dirty) Text(tr(language, "Unsaved changes", "Cambios sin guardar"),
            color = MaterialTheme.colorScheme.tertiary)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            OutlinedButton(onClick = {
                draft.clear(); draft.putAll(MultiTenantStore.defaults(role).let { defaults ->
                    MultiTenantStore.allPermissionGroups().flatMap { it.second }.associateWith { it in defaults }
                })
            }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(8.dp)) {
                Text(tr(language, "Defaults", "Restablecer"), style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(onClick = { if (dirty) confirmDiscard = true }, modifier = Modifier.weight(1f),
                enabled = dirty, contentPadding = PaddingValues(8.dp)) {
                Text(tr(language, "Cancel", "Cancelar"))
            }
            Button(onClick = {
                scope.launch {
                    try {
                        if (MultiTenantStore.saveEmployeePermissionsRemote(manager,employee,draft.toMap()))
                            notice = tr(language,"Changes saved","Cambios guardados")
                    } catch(ex: Exception) { notice = ex.message ?: "Error de SafeLab API" }
                }
            }, enabled = dirty, modifier = Modifier.weight(1f), contentPadding = PaddingValues(8.dp)) {
                Text(tr(language, "Save", "Guardar"))
            }
        }
        if (notice.isNotBlank()) Text(notice, color = MaterialTheme.colorScheme.primary)
        ResourceAssignmentEditor(manager, employee, language)
    }
    if (confirmExit) AlertDialog(
        onDismissRequest = { confirmExit = false },
        title = { Text(tr(language, "Leave without saving?", "¿Salir sin guardar?")) },
        text = { Text(tr(language, "Your permission changes will be discarded.",
            "Se descartarán los cambios de permisos.")) },
        confirmButton = { TextButton(onClick = { confirmExit = false; onClose() }) {
            Text(tr(language, "Discard and leave", "Descartar y salir"))
        } },
        dismissButton = { TextButton(onClick = { confirmExit = false }) {
            Text(tr(language, "Keep editing", "Seguir editando"))
        } }
    )
    if (confirmDiscard) AlertDialog(onDismissRequest = { confirmDiscard = false },
        title = { Text(tr(language, "Discard changes?", "¿Descartar cambios?")) },
        confirmButton = { TextButton(onClick = {
            draft.clear(); draft.putAll(saved); confirmDiscard = false
        }) { Text(tr(language, "Discard", "Descartar")) } },
        dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text(tr(language, "Keep editing", "Seguir editando")) } })
}

@Composable
private fun ResourceAssignmentEditor(manager: JSONObject, employee: JSONObject, language: String) {
    val scope = rememberCoroutineScope()
    val orgId = manager.optString("organizationId")
    val resourceOptions = (MultiTenantStore.items("assets") + MultiTenantStore.items("sensors"))
        .filter { it.optString("organizationId") == orgId }
    val current = MultiTenantStore.arrayStrings(employee.optJSONArray("resourceIds"))
    val selected = remember(employee.optString("id")) { mutableStateMapOf<String, Boolean>().apply {
        resourceOptions.forEach { this[it.optString("id")] = it.optString("id") in current }
    } }
    var notice by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
        Text(tr(language, "Assigned equipment and sensors", "Equipos y sensores asignados") + " ▾")
    }
    if (expanded) {
        resourceOptions.forEach { record ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = selected[record.optString("id")] == true,
                    onCheckedChange = { selected[record.optString("id")] = it })
                Text(rowTitle("assets", record), modifier = Modifier.weight(1f))
            }
        }
        Button(onClick = {
            scope.launch {
                try {
                    notice = if (MultiTenantStore.saveEmployeeResourcesRemote(manager, employee,
                        selected.filterValues { it }.keys.toList()))
                        tr(language, "Assignments saved", "Asignaciones guardadas")
                    else tr(language, "Unable to save assignments", "No se pudieron guardar las asignaciones")
                } catch(ex: Exception) { notice = ex.message ?: "Error de SafeLab API" }
            }
        }) { Text(tr(language, "Save assignments", "Guardar asignaciones")) }
        if (notice.isNotBlank()) Text(notice)
    }
}
