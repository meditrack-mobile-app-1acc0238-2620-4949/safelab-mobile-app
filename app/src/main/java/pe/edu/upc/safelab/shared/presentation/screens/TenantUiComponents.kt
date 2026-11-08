package pe.edu.upc.safelab.shared.presentation.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import pe.edu.upc.safelab.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import pe.edu.upc.safelab.shared.infrastructure.data.MultiTenantStore
import pe.edu.upc.safelab.common.tr

/** Reusable UI building blocks shared by the three types of mobile accounts. */
@Composable
fun PageBanner(title: String, subtitle: String) {
    val scheme = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(25.dp), color = Color.Transparent) {
        Column(Modifier.fillMaxWidth().background(
            Brush.horizontalGradient(listOf(scheme.surface, scheme.primary.copy(alpha = .10f), Color(0xFF14B8A6).copy(alpha = .12f)))
        ).padding(19.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text(subtitle, color = scheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun MetricTile(label: String, value: String, description: String, shade: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp), tonalElevation = 2.dp) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(37.dp).background(shade.copy(alpha = .15f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text("●", color = shade)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun StatusPill(value: String, language: String, context: String = "") {
    val lowered = value.lowercase()
    val good = lowered in listOf("active", "normal", "online", "ready", "paid", "reviewed", "compliant", "confirmed", "resolved", "completed")
    val caution = lowered in listOf("pending", "scheduled", "upcoming", "in progress", "acknowledged", "maintenance", "offline", "warning")
    val critical = lowered in listOf("critical", "violation", "open", "suspended", "blocked", "overdue", "out_of_range", "active_alert")
    val color = when {
        lowered == "active" && context in listOf("alerts", "incidents", "complianceViolations") -> Color(0xFFE44848)
        good -> Color(0xFF0D9B72)
        critical -> Color(0xFFE44848)
        caution || lowered == "active" -> Color(0xFFE69A19)
        else -> Color(0xFF64748B)
    }
    val label = when (lowered) {
        "active" -> tr(language, "Active", "Activo")
        "pending" -> tr(language, "Pending", "Pendiente")
        "paid" -> tr(language, "Paid", "Pagado")
        "upcoming" -> tr(language, "Upcoming", "Próximo")
        "offline" -> tr(language, "Offline", "Desconectado")
        "online" -> tr(language, "Online", "Conectado")
        "normal" -> tr(language, "Normal", "Normal")
        "ready" -> tr(language, "Ready", "Listo")
        "open" -> tr(language, "Open", "Abierto")
        "reviewed" -> tr(language, "Reviewed", "Revisado")
        "violation" -> tr(language, "Violation", "Incumplimiento")
        "compliant" -> tr(language, "Compliant", "Conforme")
        "resolved" -> tr(language, "Resolved", "Resuelto")
        "critical" -> tr(language, "Critical", "Crítico")
        "scheduled" -> tr(language, "Scheduled", "Programado")
        "maintenance" -> tr(language, "Maintenance", "Mantenimiento")
        "blocked" -> tr(language, "Blocked", "Bloqueado")
        "cancelled" -> tr(language, "Cancelled", "Cancelado")
        "in progress" -> tr(language, "In progress", "En progreso")
        "acknowledged" -> tr(language, "Acknowledged", "Reconocido")
        else -> value.replaceFirstChar { it.uppercase() }
    }
    Surface(color = color.copy(alpha = .14f), shape = RoundedCornerShape(30.dp)) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun InfoCard(title: String, subtitle: String = "", onClick: (() -> Unit)? = null, trailing: @Composable (() -> Unit)? = null) {
    Surface(modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(17.dp), tonalElevation = 1.dp) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (subtitle.isNotBlank()) Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            if (trailing != null) trailing() else if (onClick != null) Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

fun rowTitle(collection: String, row: JSONObject): String = when (collection) {
    "users" -> row.optString("fullName")
    "organizations" -> row.optString("name")
    "subscriptions" -> row.optString("plan")
    "invitations" -> row.optString("email")
    "complianceEvaluations", "complianceViolations", "auditLogs", "maintenanceRecords", "remoteCommands" ->
        row.optString("id")
    else -> row.optString("name").ifBlank { row.optString("title").ifBlank { row.optString("code", row.optString("id")) } }
}
fun rowDescription(collection: String, row: JSONObject, language: String = "EN"): String = when (collection) {
    "users" -> "${row.optString("email")} · ${MultiTenantStore.roleName(row.optString("role"), language)}" 
    "organizations" -> "${row.optString("city")} · ${row.optString("segment")}" 
    "subscriptions" -> "PEN ${row.optString("monthlyCost")} · ${row.optString("renewalDate")}" 
    "sensors" -> "${row.optString("code")} · ${row.optString("value")} ${row.optString("unit")}" 
    "assets" -> "${row.optString("category")} · ${row.optString("location")}" 
    "alerts", "incidents" -> row.optString("message", row.optString("description"))
    "auditLogs" -> "${row.optString("actor")} · ${row.optString("action")}" 
    "maintenanceRecords" -> "${row.optString("type")} · ${row.optString("date")}" 
    "remoteCommands" -> "${row.optString("command")} · ${row.optString("device")}" 
    "complianceEvaluations" -> "${row.optString("ruleId")} · ${row.optString("value")}" 
    "reports" -> row.optString("type")
    else -> row.optString("code", row.optString("facilityId"))
}

/** A record detail, never exposing the demonstration password in the UI or CSV. */
@Composable
fun RecordDetailDialog(collection: String, record: JSONObject?, language: String, onDismiss: () -> Unit) {
    if (record == null) return
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(rowTitle(collection, record), fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val keys = record.keys().asSequence().filterNot { it == "password" || it == "token" || it == "resourceIds" || it == "facilityIds" }.toList()
                keys.take(18).forEach { key ->
                    val value = record.opt(key)
                    if (value != JSONObject.NULL && value !is JSONObject && value !is org.json.JSONArray) {
                        Column {
                            Text(localizedFieldLabel(key, language), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (key == "status") StatusPill(value.toString(), language, collection)
                            else Text(value.toString(), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text(tr(language, "Close", "Cerrar")) } })
}

@Composable
fun RecordCollectionScreen(
    language: String,
    collection: String,
    title: String,
    actor: JSONObject,
    organizationId: String? = null,
    compact: Boolean = false,
    onOpenAll: (() -> Unit)? = null
) {
    val version = MultiTenantStore.version
    val all = remember(version, collection, organizationId, actor.optString("id")) {
        MultiTenantStore.visibleItems(collection, actor, organizationId)
    }
    var query by remember(collection, organizationId) { mutableStateOf("") }
    var selected by remember(collection, organizationId) { mutableStateOf<JSONObject?>(null) }
    var selectedStatus by remember(collection, organizationId) { mutableStateOf("all") }
    var editing by remember(collection, organizationId) { mutableStateOf<JSONObject?>(null) }
    var deleting by remember(collection, organizationId) { mutableStateOf<JSONObject?>(null) }
    var actionError by remember(collection, organizationId) { mutableStateOf("") }
    var busy by remember(collection, organizationId) { mutableStateOf(false) }
    val actionScope = rememberCoroutineScope()
    val managePermission = when (collection) {
        "assets" -> "assets.manage"
        "sensors" -> "sensors.configure"
        "maintenanceRecords" -> "maintenance.manage"
        else -> ""
    }
    val canEdit = managePermission.isNotBlank() && MultiTenantStore.can(actor, managePermission)
    val canDelete = canEdit && collection in listOf("assets", "sensors")
    val states = all.map { it.optString("status") }.filter { it.isNotBlank() }.distinct()
    val filtered = all.filter { row ->
        row.toString().contains(query, ignoreCase = true) &&
            (selectedStatus == "all" || row.optString("status") == selectedStatus)
    }
    val shown = if (compact) filtered.take(4) else filtered
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (compact && onOpenAll != null) TextButton(onClick = onOpenAll) {
                Text(tr(language, "View all", "Ver todos"))
            }
        }
        if (!compact) {
            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), singleLine = true,
                label = { Text(tr(language, "Search records", "Buscar registros")) })
            if (states.isNotEmpty()) {
                var statusMenu by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { statusMenu = true }) {
                        Text(tr(language, "Status", "Estado") + ": " +
                            if (selectedStatus == "all") tr(language, "All", "Todos") else selectedStatus)
                    }
                    DropdownMenu(expanded = statusMenu, onDismissRequest = { statusMenu = false }) {
                        (listOf("all") + states).forEach { status ->
                            DropdownMenuItem(text = { Text(if (status == "all") tr(language, "All", "Todos") else status) },
                                onClick = { selectedStatus = status; statusMenu = false })
                        }
                    }
                }
            }
        }
        Text("${filtered.size} ${tr(language, "records", "registros")}", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        shown.forEach { row ->
            InfoCard(title = rowTitle(collection, row), subtitle = rowDescription(collection, row, language), onClick = { selected = row }, trailing = {
                Column(horizontalAlignment = Alignment.End) {
                    val state = row.optString("status").ifBlank { row.optString("connection") }
                    if (state.isNotBlank()) StatusPill(state, language, collection)
                    val actions = if (compact) emptyList() else availableRecordActions(collection, row, actor)
                    if (actions.isNotEmpty()) {
                        var menuOpen by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { menuOpen = true }) { Text(tr(language, "Actions", "Acciones")) }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                actions.forEach { (key, label) ->
                                    DropdownMenuItem(text = { Text(tr(language, label.first, label.second)) }, onClick = {
                                        menuOpen = false
                                        when (key) {
                                            "edit" -> editing = row
                                            "delete" -> deleting = row
                                            else -> actionScope.launch {
                                                busy = true
                                                try {
                                                    if (!MultiTenantStore.performRecordActionRemote(collection, row, actor, key))
                                                        throw IllegalStateException(tr(language, "Operation not permitted", "Operación no permitida"))
                                                    actionError = ""
                                                } catch (ex: Exception) { actionError = ex.message ?: "HTTP error" }
                                                finally { busy = false }
                                            }
                                        }
                                    })
                                }
                            }
                        }
                    } else Text("›", color = MaterialTheme.colorScheme.primary)
                }
            })
        }
        if (actionError.isNotBlank()) Text(actionError, color = MaterialTheme.colorScheme.error)
        if (shown.isEmpty()) Text(tr(language, "No matching records", "Sin registros coincidentes"),
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (!compact) OutlinedButton(onClick = {
            val keys = when (collection) {
                "users" -> listOf("username", "fullName", "email", "role", "organization", "status")
                else -> all.firstOrNull()?.keys()?.asSequence()?.filterNot { it == "password" || it == "token" }
                    ?.take(12)?.toList() ?: listOf("id")
            }
            fun csv(s: Any?) = "\"" + (s?.toString() ?: "").replace("\"", "\"\"") + "\""
            val content = keys.joinToString(",") { csv(it) } + "\n" + filtered.joinToString("\n") { row ->
                keys.joinToString(",") { csv(row.opt(it)) }
            }
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"; putExtra(Intent.EXTRA_TEXT, content)
            }, tr(language, "Export CSV", "Exportar CSV")))
        }) { Text(tr(language, "Export CSV", "Exportar CSV")) }
    }
    RecordDetailDialog(collection, selected, language) { selected = null }
    if (editing != null) RecordCrudEditor(editing!!, collection, actor, language) { editing = null }
    if (deleting != null) {
        val record = deleting!!
        AlertDialog(onDismissRequest = { if (!busy) deleting = null },
            title = { Text(tr(language, "Delete record?", "¿Eliminar registro?")) },
            text = { Text(rowTitle(collection, record) + " — " + tr(language,
                "This action is permanent. Linked resources may prevent deletion.",
                "Esta acción es permanente. Recursos vinculados pueden impedir la eliminación.")) },
            confirmButton = { TextButton(enabled = !busy, onClick = {
                busy = true
                actionScope.launch {
                    try {
                        if (!MultiTenantStore.deleteRecordRemote(collection, record.optString("id"), actor))
                            throw IllegalStateException(tr(language, "Action not permitted", "Operación no permitida"))
                        actionError = ""; deleting = null
                    } catch (ex: Exception) { actionError = ex.message ?: "HTTP error"; deleting = null }
                    finally { busy = false }
                }
            }) { Text(tr(language, "Delete", "Eliminar")) } },
            dismissButton = { TextButton(enabled = !busy, onClick = { deleting = null }) {
                Text(tr(language, "Cancel", "Cancelar"))
            } })
    }
}

/** The server, not the UI, always makes the final authorization decision. */
private fun availableRecordActions(collection: String, row: JSONObject, actor: JSONObject): List<Pair<String, Pair<String, String>>> {
    val actions = mutableListOf<Pair<String, Pair<String, String>>>()
    fun add(key: String, en: String, es: String) { actions.add(key to (en to es)) }
    fun can(permission: String) = MultiTenantStore.can(actor, permission)
    val status = row.optString("status")
    when (collection) {
        "assets" -> if (can("assets.manage")) {
            add("edit", "Edit", "Editar"); add("delete", "Delete", "Eliminar")
        }
        "sensors" -> if (can("sensors.configure")) {
            add("edit", "Configure / reading", "Configurar / lectura"); add("delete", "Delete", "Eliminar")
        }
        "maintenanceRecords" -> if (can("maintenance.manage")) add("edit", "Edit maintenance", "Editar mantenimiento")
        "alerts" -> {
            if (status == "active" && can("alerts.acknowledge")) add("acknowledge", "Acknowledge", "Reconocer")
            if (status !in listOf("resolved", "closed") && can("incidents.resolve")) add("resolve", "Resolve alert", "Resolver alerta")
            if (status in listOf("active", "acknowledged") && can("incidents.create")) add("escalate", "Escalate", "Escalar")
        }
        "incidents" -> if (can("incidents.resolve")) {
            if (status == "open") add("investigate", "Investigate", "Investigar")
            if (status in listOf("open", "investigating", "in_progress")) add("resolve", "Resolve incident", "Resolver incidente")
            if (status == "resolved") add("close", "Close incident", "Cerrar incidente")
        }
        "complianceRules" -> if (can("compliance.manage")) add("toggle",
            if (status == "active") "Pause rule" else "Activate rule",
            if (status == "active") "Pausar regla" else "Activar regla")
        "notifications" -> if (row.optBoolean("unread") && can("incidents.view"))
            add("mark-read", "Mark read", "Marcar como leída")
    }
    return actions
}

/** Same invoice numbers, dates and amounts as Vue. */
@Composable
fun InvoiceListScreen(orgId: String?, language: String) {
    val sources = MultiTenantStore.items("subscriptions").filter {
        orgId.isNullOrBlank() || it.optString("organizationId") == orgId
    }
    val invoices = sources.flatMap { subscription ->
        val array = subscription.optJSONArray("invoices") ?: org.json.JSONArray()
        (0 until array.length()).mapNotNull { array.optJSONObject(it) }
    }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<JSONObject?>(null) }
    val filtered = invoices.filter { it.toString().contains(query, ignoreCase = true) }
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(tr(language, "Invoices and receipts", "Facturas y recibos"),
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text(tr(language, "Search invoices", "Buscar recibos")) })
        Text("${filtered.size} ${tr(language, "receipts", "recibos")}")
        filtered.forEach { invoice ->
            InfoCard(invoice.optString("number"),
                "PEN ${invoice.optString("amount")} · ${invoice.optString("dueDate")}",
                onClick = { selected = invoice }, trailing = { StatusPill(invoice.optString("status"), language) })
        }
        OutlinedButton(onClick = {
            val csv = "number,amount,status,dueDate\n" + filtered.joinToString("\n") {
                "${it.optString("number")},${it.optString("amount")},${it.optString("status")},${it.optString("dueDate")}" }
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"; putExtra(Intent.EXTRA_TEXT, csv)
            }, tr(language, "Export CSV", "Exportar CSV")))
        }) { Text(tr(language, "Export CSV", "Exportar CSV")) }
    }
    RecordDetailDialog("invoices", selected, language) { selected = null }
}


private fun localizedFieldLabel(key: String, language: String): String {
    val labels = mapOf(
        "id" to ("ID" to "ID"), "name" to ("Name" to "Nombre"),
        "fullName" to ("Full name" to "Nombre completo"),
        "email" to ("Email" to "Correo electrónico"),
        "role" to ("Role" to "Rol"),
        "status" to ("Status" to "Estado"),
        "organization" to ("Organization" to "Empresa"),
        "organizationId" to ("Organization ID" to "ID de empresa"),
        "facilityId" to ("Facility ID" to "ID de sede"),
        "assetId" to ("Equipment ID" to "ID de equipo"),
        "sensorId" to ("Sensor ID" to "ID de sensor"),
        "severity" to ("Severity" to "Gravedad"),
        "date" to ("Date" to "Fecha"),
        "dueDate" to ("Due date" to "Vencimiento"),
        "createdAt" to ("Created at" to "Fecha de registro"),
        "amount" to ("Amount" to "Importe"),
        "code" to ("Code" to "Código"),
        "action" to ("Action" to "Acción"),
        "actor" to ("Actor" to "Responsable")
    )
    return labels[key]?.let { tr(language, it.first, it.second) }
        ?: key.replace(Regex("([a-z])([A-Z])"), "$1 $2")
}

/** Destructive navigation action: always red, in light and dark themes. */
@Composable
fun SignOutCard(language: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        color = Color(0xFFDC2626)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_nav_logout),
                contentDescription = null,
                modifier = Modifier.size(19.dp),
                tint = Color.White
            )
            Spacer(Modifier.size(9.dp))
            Text(
                tr(language, "Sign out", "Cerrar sesión"),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
