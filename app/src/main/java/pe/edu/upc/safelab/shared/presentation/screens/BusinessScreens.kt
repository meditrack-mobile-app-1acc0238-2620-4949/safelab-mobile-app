package pe.edu.upc.safelab.shared.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import pe.edu.upc.safelab.shared.infrastructure.data.MultiTenantStore
import pe.edu.upc.safelab.common.tr

/** Home and operational screens for a manager or a member of one company. */
@Composable
fun BusinessDashboard(actor: JSONObject, language: String, onOpen: (String) -> Unit) {
    val version = MultiTenantStore.version
    val orgId = actor.optString("organizationId")
    val org = MultiTenantStore.organization(orgId)
    val sensors = MultiTenantStore.visibleItems("sensors", actor)
    val assets = MultiTenantStore.visibleItems("assets", actor)
    val alerts = MultiTenantStore.visibleItems("alerts", actor)
    val incidents = MultiTenantStore.visibleItems("incidents", actor)
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { PageBanner(tr(language, "Dashboard", "Panel principal"), org?.optString("name").orEmpty()) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(tr(language, "Sensors", "Sensores"), sensors.size.toString(),
                    tr(language, "Assigned devices", "Dispositivos asignados"), Color(0xFF4F35E8), Modifier.weight(1f))
                MetricTile(tr(language, "Online", "Conectados"),
                    sensors.count { it.optString("connection") == "online" }.toString(),
                    tr(language, "Connected sensors", "Sensores conectados"), Color(0xFF10B981), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(tr(language, "Equipment", "Equipos"), assets.size.toString(),
                    tr(language, "Tracked assets", "Activos registrados"), Color(0xFF0891B2), Modifier.weight(1f))
                MetricTile(tr(language, "Alerts", "Alertas"),
                    alerts.count { it.optString("status") in listOf("active", "acknowledged") }.toString(),
                    tr(language, "Require attention", "Requieren atención"), Color(0xFFEF4444), Modifier.weight(1f))
            }
        }
        item { MetricTile(tr(language, "Incidents", "Incidentes"),
            incidents.count { it.optString("status") in listOf("open", "investigating", "in_progress") }.toString(),
            tr(language, "Open investigations", "Investigaciones abiertas"), Color(0xFFF59E0B)) }
        item { Text(tr(language, "Quick access", "Accesos rápidos"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item { InfoCard(tr(language, "Sensors", "Sensores"), tr(language, "Readings and connected devices", "Lecturas y dispositivos conectados"),
            onClick = { onOpen("sensors") }) }
        item { InfoCard(tr(language, "Alerts & incidents", "Alertas e incidentes"),
            tr(language, "Operational follow-up", "Seguimiento operativo"), onClick = { onOpen("alerts") }) }
        item { RecordCollectionScreen(language, "alerts", tr(language, "Recent alerts", "Alertas recientes"),
            actor, compact = true, onOpenAll = { onOpen("alerts") }) }
    }
}

/** Displays data from the same seed used by the web, never the outdated per-BC mock lists. */
@Composable
fun BusinessRecords(actor: JSONObject, language: String, section: String) {
    val data = when (section) {
        "sensors" -> listOf("sensors" to tr(language, "Sensors", "Sensores"))
        "alerts" -> listOf("alerts" to tr(language, "Alerts", "Alertas"), "incidents" to tr(language, "Incidents", "Incidentes"))
        "equipment" -> listOf("assets" to tr(language, "Equipment", "Equipos"),
            "maintenanceRecords" to tr(language, "Maintenance history", "Historial de mantenimiento"))
        "facilities" -> listOf("facilities" to tr(language, "Facilities", "Sedes"), "assets" to tr(language, "Assets", "Activos"))
        "compliance" -> listOf("complianceRules" to tr(language, "Environmental rules", "Reglas ambientales"),
            "complianceEvaluations" to tr(language, "Evaluations", "Evaluaciones"),
            "complianceViolations" to tr(language, "Violations", "Incumplimientos"))
        "remote" -> listOf("actuators" to tr(language, "Remote devices", "Dispositivos remotos"),
            "remoteCommands" to tr(language, "Command history", "Historial de comandos"))
        "reports" -> listOf("reports" to tr(language, "Reports", "Reportes"))
        "audit" -> listOf("auditLogs" to tr(language, "Audit trail", "Auditoría"))
        else -> emptyList()
    }
    var current by remember(section) { mutableStateOf(data.firstOrNull()?.first.orEmpty()) }
    var refreshError by remember(section) { mutableStateOf("") }
    LaunchedEffect(section, current) {
        try { MultiTenantStore.refreshFromBackend(); refreshError = "" }
        catch (ex: Exception) { refreshError = ex.message ?: "Sync error" }
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { PageBanner(data.firstOrNull { it.first == current }?.second.orEmpty(),
            tr(language, "Records from your authorized facilities and equipment.",
                "Registros de las instalaciones y equipos autorizados.")) }
        // Recarga automática al entrar o cambiar de submódulo; sin botón manual.
        if (refreshError.isNotBlank()) item {
            Text(refreshError, color = MaterialTheme.colorScheme.error)
        }
        if (data.size > 1) item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                data.forEach { (key, title) ->
                    if (key == current) Button(onClick = { current = key }, modifier = Modifier.fillMaxWidth()) {
                        Text(title)
                    } else OutlinedButton(onClick = { current = key }, modifier = Modifier.fillMaxWidth()) {
                        Text(title)
                    }
                }
            }
        }
        if (current.isNotBlank()) item {
            RecordCollectionScreen(language, current,
                data.firstOrNull { it.first == current }?.second.orEmpty(), actor, compact = false)
        }
        if (section == "remote" && MultiTenantStore.can(actor, "actuation.execute")) {
            item { RemoteActionPanel(actor, language) }
        }
        if (section == "alerts") item { AlertActionPanel(actor, language) }
        if (section in listOf("sensors", "equipment", "alerts", "reports", "facilities")) {
            item { BusinessEntryForms(actor, language, section) }
        }
    }
}

@Composable
private fun AlertActionPanel(actor: JSONObject, language: String) {
    val scope = rememberCoroutineScope()
    var apiError by remember { mutableStateOf("") }
    val alerts = MultiTenantStore.visibleItems("alerts", actor).filter { it.optString("status") == "active" }
    if (!MultiTenantStore.can(actor, "alerts.acknowledge") || alerts.isEmpty()) return
    Text(tr(language, "Acknowledge alerts", "Reconocer alertas"), fontWeight = FontWeight.Bold)
    alerts.forEach { record ->
        InfoCard(rowTitle("alerts", record), record.optString("severity"), onClick = {
            scope.launch {
                try { MultiTenantStore.acknowledgeAlertRemote(record.optString("id"), actor); apiError = "" }
                catch (ex: Exception) { apiError = ex.message ?: "API error" }
            }
        }, trailing = {
            Text(tr(language, "Acknowledge", "Reconocer"), color = MaterialTheme.colorScheme.primary)
        })
    }
    if (apiError.isNotBlank()) Text(apiError, color = MaterialTheme.colorScheme.error)
}

@Composable
private fun RemoteActionPanel(actor: JSONObject, language: String) {
    val scope = rememberCoroutineScope()
    var apiError by remember { mutableStateOf("") }
    var confirmId by remember { mutableStateOf<String?>(null) }
    var pendingCommand by remember { mutableStateOf("START") }
    val available = MultiTenantStore.visibleItems("actuators", actor)
        .filter { it.optString("status") == "online" && it.optString("safetyState") == "enabled" }
    if (available.isEmpty()) return
    Text(tr(language, "Authorized remote commands", "Comandos remotos autorizados"), fontWeight = FontWeight.Bold)
    available.forEach { record ->
        InfoCard(record.optString("name"), tr(language, "Confirm before simulating a command", "Confirmar antes de simular un comando"),
            onClick = { confirmId = record.optString("id"); pendingCommand = "START" })
    }
    if (confirmId != null) AlertDialog(onDismissRequest = { confirmId = null },
        title = { Text(tr(language, "Simulate remote command?", "¿Simular comando remoto?")) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(tr(language, "This is a simulated API command, not a hardware action.",
                "Este es un comando simulado de API, no una acción sobre equipos físicos."))
            listOf("START", "STOP", "RESET").forEach { command ->
                FilterChip(selected = pendingCommand == command, onClick = { pendingCommand = command },
                    label = { Text(command) })
            }
        } },
        confirmButton = { TextButton(onClick = {
            val id = confirmId!!
            scope.launch {
                try {
                    if (!MultiTenantStore.executeCommandRemote(id, actor, pendingCommand))
                        throw IllegalStateException(tr(language, "Not allowed", "No autorizado"))
                    apiError = ""
                }
                catch (ex: Exception) { apiError = ex.message ?: "API error" }
            }
            confirmId = null
        }) { Text(tr(language, "Confirm", "Confirmar")) } },
        dismissButton = { TextButton(onClick = { confirmId = null }) { Text(tr(language, "Cancel", "Cancelar")) } })
    if (apiError.isNotBlank()) Text(apiError, color = MaterialTheme.colorScheme.error)
}

@Composable
fun BusinessMore(actor: JSONObject, language: String, onOpen: (String) -> Unit, onSignOut: () -> Unit) {
    val manager = actor.optString("role") == "organizationAdmin"
    val options = listOf(
        Triple("facilities", tr(language, "Facilities & assets", "Sedes y activos"), "assets.view"),
        Triple("compliance", tr(language, "Environmental compliance", "Cumplimiento ambiental"), "compliance.view"),
        Triple("remote", tr(language, "Remote control", "Control remoto"), "actuation.view"),
        Triple("reports", tr(language, "Reports & analytics", "Reportes y análisis"), "reports.view"),
        Triple("audit", tr(language, "Audit & traceability", "Auditoría y trazabilidad"), "audit.view")
    )
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item { PageBanner(tr(language, "More", "Más"), MultiTenantStore.orgName(actor.optString("organizationId"))) }
        options.filter { MultiTenantStore.can(actor, it.third) }.forEach { (id, label, _) ->
            item { InfoCard(label, onClick = { onOpen(id) }) }
        }
        if (manager) {
            item { InfoCard(tr(language, "Users & permissions", "Usuarios y permisos"),
                tr(language, "Manage employees, invitations and access", "Administra empleados, invitaciones y accesos"),
                onClick = { onOpen("users") }) }
            item { InfoCard(tr(language, "Subscription & billing", "Suscripción y pagos"),
                tr(language, "Plan, seats and invoices", "Plan, cupos y recibos"),
                onClick = { onOpen("billing") }) }
        }
        item { InfoCard(tr(language, "My profile", "Mi perfil"), onClick = { onOpen("profile") }) }
        item { SignOutCard(language, onSignOut) }
    }
}

/** Small input forms with the same allowed operations as the web, using local data only. */
@Composable
private fun BusinessEntryForms(actor: JSONObject, language: String, section: String) {
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    val options = when (section) {
        "sensors" -> listOf("sensors" to "sensors.configure")
        "equipment" -> listOf("maintenanceRecords" to "maintenance.manage")
        "alerts" -> listOf("incidents" to "incidents.create")
        "facilities" -> listOf("assets" to "assets.manage")
        "reports" -> listOf("reports" to "reports.export")
        else -> emptyList()
    }
    val applicable = options.filter { MultiTenantStore.can(actor, it.second) }
    if (applicable.isEmpty()) return
    val collection = applicable.first().first
    var editing by remember(section) { mutableStateOf(false) }
    var name by remember(section) { mutableStateOf("") }
    var note by remember(section) { mutableStateOf("") }
    var assignedEmployeeId by remember(section) { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    val facility = MultiTenantStore.arrayStrings(actor.optJSONArray("facilityIds")).firstOrNull().orEmpty()
    val title = when (collection) {
        "sensors" -> tr(language, "Add sensor", "Agregar sensor")
        "maintenanceRecords" -> tr(language, "Record maintenance", "Registrar mantenimiento")
        "incidents" -> tr(language, "Create incident", "Registrar incidente")
        "assets" -> tr(language, "Add equipment", "Agregar equipo")
        else -> tr(language, "Generate report", "Generar reporte")
    }
    OutlinedButton(onClick = { editing = !editing }) { Text(title) }
    if (editing) {
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true,
            label = { Text(tr(language, "Name or title", "Nombre o título")) })
        OutlinedTextField(note, { note = it }, Modifier.fillMaxWidth(),
            label = { Text(tr(language, "Description or notes", "Descripción u observaciones")) })
        if (collection == "assets" && actor.optString("role") == "organizationAdmin") {
            // Solo IDs de usuarios activos de la misma empresa autorizados para la sede.
            val employeeChoices = MultiTenantStore.employees(actor.optString("organizationId"))
                .filter { it.optString("status") == "active" &&
                    (MultiTenantStore.arrayStrings(it.optJSONArray("facilityIds")).let { ids ->
                        ids.isEmpty() || facility in ids }) }
            Text(tr(language, "Responsible employee", "Empleado responsable"),
                style = MaterialTheme.typography.titleSmall)
            FilterChip(selected = assignedEmployeeId == null, onClick = { assignedEmployeeId = null },
                label = { Text(tr(language, "Unassigned", "Sin asignar")) })
            employeeChoices.forEach { employee ->
                val id = employee.optString("id")
                FilterChip(selected = assignedEmployeeId == id, onClick = { assignedEmployeeId = id },
                    label = { Text(employee.optString("fullName")) })
            }
        }
        Button(onClick = {
            if (name.isBlank() || facility.isBlank()) {
                message = tr(language, "Enter a name and select an assigned facility.",
                    "Ingresa el nombre y selecciona una sede asignada.")
            } else {
                val fields = org.json.JSONObject().apply {
                    put("facilityId", facility)
                    put("status", when (collection) {
                        "incidents" -> "open"; "reports" -> "ready";
                        "maintenanceRecords" -> "Scheduled"; "assets" -> "compliant"; else -> "normal"
                    })
                    when (collection) {
                        "sensors" -> { put("name", name.trim()); put("code", "SEN-NEW")
                            put("type", "temperature"); put("unit", "°C"); put("value", 4.0)
                            put("min", 2); put("max", 8); put("connection", "online") }
                        "maintenanceRecords" -> { put("type", name.trim()); put("notes", note.trim())
                            put("date", java.time.LocalDate.now().toString())
                            put("equipmentId", MultiTenantStore.visibleItems("assets", actor).firstOrNull()?.optString("id") ?: "") }
                        "incidents" -> { put("title", name.trim()); put("description", note.trim())
                            put("code", "INC-NEW"); put("severity", "warning") }
                        "assets" -> { put("name", name.trim()); put("category", "Cold Storage"); put("location", note.trim())
                            val employeeId = if (actor.optString("role") == "organizationAdmin") assignedEmployeeId else actor.optString("id")
                            if (!employeeId.isNullOrBlank()) put("responsibleUserId", employeeId)
                        }
                        "reports" -> { put("title", name.trim()); put("type", "Operational"); put("format", "CSV") }
                    }
                }
                saving = true
                scope.launch {
                    try {
                        if (MultiTenantStore.addRecordRemote(collection, actor, fields)) {
                            name = ""; note = ""; assignedEmployeeId = null; editing = false
                            message = tr(language, "Record saved", "Registro guardado")
                        } else message = tr(language, "Unable to save this record", "No se pudo guardar el registro")
                    } catch (ex: Exception) { message = ex.message ?: "Error de SafeLab API" }
                    finally { saving = false }
                }
            }
        }, enabled = !saving) { Text(tr(language, "Save", "Guardar")) }
        TextButton(onClick = { editing = false }) { Text(tr(language, "Cancel", "Cancelar")) }
    }
    if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.primary)
}
