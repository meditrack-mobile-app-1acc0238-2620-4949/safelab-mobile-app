package pe.edu.upc.safelab.shared.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import pe.edu.upc.safelab.shared.infrastructure.data.MultiTenantStore
import pe.edu.upc.safelab.shared.infrastructure.http.SafeLabApi
import pe.edu.upc.safelab.shared.presentation.components.AppTopBar
import pe.edu.upc.safelab.common.tr

/** Separate mobile navigation for SafeLab platform administrators. */
@Composable
fun PlatformAdminEntryScreen(
    actor: JSONObject,
    language: String,
    darkTheme: Boolean,
    onLanguageToggle: () -> Unit,
    onThemeToggle: () -> Unit,
    onProfile: () -> Unit,
    onSignOut: () -> Unit
) {
    var tab by remember { mutableStateOf("overview") }
    var orgId by remember { mutableStateOf<String?>(null) }
    var orgTab by remember { mutableStateOf("overview") }
    var fullCollection by remember { mutableStateOf<String?>(null) }
    val title = when {
        fullCollection != null -> tr(language, "All records", "Todos los registros")
        orgId != null -> MultiTenantStore.orgName(orgId!!)
        else -> MultiTenantStore.text(language, "platform_$tab")
    }
    fun back() {
        when {
            fullCollection != null -> fullCollection = null
            orgId != null -> orgId = null
            tab != "overview" -> tab = "overview"
        }
    }
    BackHandler(enabled = fullCollection != null || orgId != null || tab != "overview") { back() }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title, language, darkTheme,
            if (fullCollection != null || orgId != null) ::back else null,
            onLanguageToggle, onThemeToggle, "SA", onProfile) },
        bottomBar = {
            NavigationBar {
                listOf("overview", "organizations", "activity", "more").forEach { id ->
                    val label = if (id == "more") tr(language, "More", "Más") else MultiTenantStore.text(language, "platform_$id")
                    NavigationBarItem(selected = tab == id, onClick = {
                        tab = id; orgId = null; fullCollection = null
                    }, icon = {
                        Text(when (id) { "overview" -> "▦"; "organizations" -> "▤"; "activity" -> "◷"; else -> "☷" },
                            fontWeight = FontWeight.Black)
                    }, label = { Text(label, maxLines = 1, style = MaterialTheme.typography.labelSmall) })
                }
            }
        }
    ) { inner ->
        LazyColumn(Modifier.fillMaxSize().padding(inner), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)) {
            val version = MultiTenantStore.version
            if (fullCollection != null) {
                val collection = fullCollection!!
                item { PageBanner(title, tr(language, "Search, inspect and export records.", "Busca, consulta y exporta registros.")) }
                if (collection == "receipts") item { InvoiceListScreen(orgId, language) }
                else item { RecordCollectionScreen(language, collection,
                    collectionTitle(language, collection), actor, orgId) }
            } else if (orgId != null) {
                val organization = MultiTenantStore.organization(orgId!!)
                item { PageBanner(organization?.optString("name").orEmpty(),
                    tr(language, "Organization details", "Detalle de la empresa")) }
                item {
                    val total = MultiTenantStore.visibleItems("users", actor, orgId).size
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricTile(tr(language, "Users", "Usuarios"), total.toString(),
                            tr(language, "Registered accounts", "Cuentas registradas"), Color(0xFF4F35E8), Modifier.weight(1f))
                        MetricTile(tr(language, "Sensors", "Sensores"),
                            MultiTenantStore.visibleItems("sensors", actor, orgId).size.toString(),
                            tr(language, "Registered", "Registrados"), Color(0xFF10B981), Modifier.weight(1f))
                    }
                }
                item {
                    val nav = listOf("overview", "users", "devices", "billing", "operations")
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        nav.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                row.forEach { id ->
                                    val label = MultiTenantStore.text(language, "tab_$id")
                                    if (orgTab == id) Button(onClick = { orgTab = id }, modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(5.dp)) { Text(label, style = MaterialTheme.typography.labelSmall) }
                                    else OutlinedButton(onClick = { orgTab = id }, modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(5.dp)) { Text(label, style = MaterialTheme.typography.labelSmall) }
                                }
                            }
                        }
                    }
                }
                when (orgTab) {
                    "overview" -> {
                        item { OrganizationSummary(orgId!!, language) }
                        item { InfoCard(tr(language, "Users", "Usuarios"), "${MultiTenantStore.visibleItems("users", actor, orgId).size}",
                            onClick = { orgTab = "users" }) }
                        item { InfoCard(tr(language, "Subscription", "Suscripción"),
                            MultiTenantStore.subscription(orgId!!)?.optString("plan").orEmpty(), onClick = { orgTab = "billing" }) }
                    }
                    "users" -> item { RecordCollectionScreen(language, "users", tr(language, "Users", "Usuarios"), actor, orgId, compact = true,
                        onOpenAll = { fullCollection = "users" }) }
                    "devices" -> {
                        item { RecordCollectionScreen(language, "assets", tr(language, "Equipment", "Equipos"), actor, orgId, true) {
                            fullCollection = "assets" } }
                        item { RecordCollectionScreen(language, "sensors", tr(language, "Sensors", "Sensores"), actor, orgId, true) {
                            fullCollection = "sensors" } }
                        item { RecordCollectionScreen(language, "actuators", tr(language, "Remote devices", "Dispositivos remotos"), actor, orgId, true) {
                            fullCollection = "actuators" } }
                    }
                    "billing" -> {
                        item { BillingSummary(orgId!!, language) }
                        item { InfoCard(tr(language, "All invoices", "Todos los recibos"),
                            tr(language, "View documents", "Consultar documentos"), onClick = { fullCollection = "receipts" }) }
                    }
                    else -> {
                        item { RecordCollectionScreen(language, "alerts", tr(language, "Alerts", "Alertas"), actor, orgId, true) {
                            fullCollection = "alerts" } }
                        item { RecordCollectionScreen(language, "incidents", tr(language, "Incidents", "Incidentes"), actor, orgId, true) {
                            fullCollection = "incidents" } }
                        item { RecordCollectionScreen(language, "auditLogs", tr(language, "Audit", "Auditoría"), actor, orgId, true) {
                            fullCollection = "auditLogs" } }
                    }
                }
            } else {
                when (tab) {
                    "overview" -> {
                        item { PageBanner(tr(language, "Platform overview", "Resumen de la plataforma"),
                            tr(language, "Companies, subscriptions and operations.", "Empresas, suscripciones y operaciones.")) }
                        item {
                            val organizations = MultiTenantStore.items("organizations")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MetricTile(tr(language, "Companies", "Empresas"), organizations.size.toString(),
                                    tr(language, "Registered", "Registradas"), Color(0xFF4F35E8), Modifier.weight(1f))
                                MetricTile(tr(language, "Active plans", "Planes activos"),
                                    MultiTenantStore.items("subscriptions").count { it.optString("status") == "active" }.toString(),
                                    tr(language, "Subscriptions", "Suscripciones"), Color(0xFF10B981), Modifier.weight(1f))
                            }
                        }
                        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricTile(tr(language, "Users", "Usuarios"), MultiTenantStore.items("users").size.toString(),
                                tr(language, "Accounts", "Cuentas"), Color(0xFF4F35E8), Modifier.weight(1f))
                            MetricTile(tr(language, "Sensors", "Sensores"), MultiTenantStore.items("sensors").size.toString(),
                                tr(language, "Devices", "Dispositivos"), Color(0xFF0891B2), Modifier.weight(1f))
                        } }
                        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricTile(tr(language, "Open alerts", "Alertas abiertas"),
                                MultiTenantStore.items("alerts").count { it.optString("status") in listOf("active", "acknowledged") }.toString(),
                                tr(language, "Requires review", "Requieren revisión"), Color(0xFFEF4444), Modifier.weight(1f))
                            MetricTile(tr(language, "Receipts", "Recibos"), MultiTenantStore.items("subscriptions").size.toString(),
                                tr(language, "Upcoming", "Próximos"), Color(0xFFF59E0B), Modifier.weight(1f))
                        } }
                        item { Text(tr(language, "Organizations", "Empresas"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                        MultiTenantStore.items("organizations").forEach { org ->
                            item { InfoCard(org.optString("name"), "${org.optString("city")} · ${org.optString("segment")}",
                                onClick = { orgId = org.optString("id") }) }
                        }
                        item { RecordCollectionScreen(language, "auditLogs", tr(language, "Recent activity", "Actividad reciente"), actor,
                            compact = true, onOpenAll = { fullCollection = "auditLogs" }) }
                    }
                    "organizations" -> {
                        item { PageBanner(tr(language, "Organizations", "Empresas"),
                            tr(language, "Select a company to inspect its operations.", "Selecciona una empresa para consultar sus operaciones.")) }
                        MultiTenantStore.items("organizations").forEach { org ->
                            item { InfoCard(org.optString("name"), "${org.optString("ruc")} · ${org.optString("city")}",
                                onClick = { orgId = org.optString("id") }, trailing = {
                                    StatusPill(org.optString("status"), language)
                                }) }
                        }
                    }
                    "activity" -> {
                        item { PageBanner(tr(language, "Activity & audit", "Actividad y auditoría"),
                            tr(language, "Events across registered companies.", "Eventos de las empresas registradas.")) }
                        item { RecordCollectionScreen(language, "auditLogs", tr(language, "Recent events", "Eventos recientes"), actor,
                            compact = true, onOpenAll = { fullCollection = "auditLogs" }) }
                    }
                    "users" -> {
                        item { PageBanner(tr(language, "Global users", "Usuarios globales"),
                            tr(language, "Employees and managers across all companies.", "Empleados y gerentes de todas las empresas.")) }
                        item { RecordCollectionScreen(language, "users", tr(language, "Users", "Usuarios"), actor,
                            compact = true, onOpenAll = { fullCollection = "users" }) }
                    }
                    "subscriptions" -> {
                        item { PageBanner(tr(language, "Subscriptions", "Suscripciones"),
                            tr(language, "Plans, seats and receipts.", "Planes, cupos y recibos.")) }
                        MultiTenantStore.items("subscriptions").forEach { subscription ->
                            item { InfoCard(MultiTenantStore.orgName(subscription.optString("organizationId")),
                                "PEN ${subscription.optString("monthlyCost")} · " +
                                    tr(language, "Seats", "Cupos") + ": ${subscription.optInt("includedSeats") + subscription.optInt("additionalSeats")}",
                                onClick = { orgId = subscription.optString("organizationId"); orgTab = "billing" },
                                trailing = { StatusPill(subscription.optString("status"), language) }) }
                        }
                    }
                    "settings" -> item { PlatformSettingsPanel(actor, language) }
                    else -> {
                        item { PageBanner(tr(language, "Platform administration", "Administración de plataforma"),
                            tr(language, "Global tools", "Herramientas globales")) }
                        item { InfoCard(tr(language, "Global users", "Usuarios globales"), onClick = { tab = "users" }) }
                        item { InfoCard(tr(language, "Subscriptions", "Suscripciones"), onClick = { tab = "subscriptions" }) }
                        item { InfoCard(tr(language, "Settings", "Configuración"), onClick = { tab = "settings" }) }
                        item { InfoCard(tr(language, "My profile", "Mi perfil"), onClick = onProfile) }
                        item { SignOutCard(language, onSignOut) }
                    }
                }
            }
        }
    }
}

private fun collectionTitle(language: String, collection: String) = when (collection) {
    "users" -> tr(language, "Global users", "Usuarios globales")
    "sensors" -> tr(language, "Sensors", "Sensores")
    "assets" -> tr(language, "Equipment", "Equipos")
    "subscriptions" -> tr(language, "Subscriptions", "Suscripciones")
    "auditLogs" -> tr(language, "Audit records", "Registros de auditoría")
    "alerts" -> tr(language, "Alerts", "Alertas")
    else -> collection
}

@Composable
private fun OrganizationSummary(orgId: String, language: String) {
    val org = MultiTenantStore.organization(orgId) ?: return
    InfoCard(tr(language, "Company details", "Datos empresariales"),
        "${org.optString("name")} · RUC ${org.optString("ruc")} · ${org.optString("city")}",
        trailing = { StatusPill(org.optString("status"), language) })
}

@Composable
fun BillingSummary(orgId: String, language: String) {
    val subscription = MultiTenantStore.subscription(orgId) ?: return
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PageBanner(tr(language, "Subscription & billing", "Suscripción y pagos"),
            MultiTenantStore.orgName(orgId))
        InfoCard(subscription.optString("plan"), "PEN ${subscription.optString("monthlyCost")} / " +
            tr(language, "month", "mes"), trailing = { StatusPill(subscription.optString("status"), language) })
        val used = MultiTenantStore.usedSeats(orgId)
        val cap = MultiTenantStore.seatCapacity(orgId)
        MetricTile(tr(language, "Employee seats", "Cupos de empleados"), "$used / $cap",
            tr(language, "Manager not counted", "Gerente no incluido"), Color(0xFF4F35E8))
        Text(tr(language, "Upcoming and paid receipts", "Próximos recibos y pagos"), fontWeight = FontWeight.Bold)
        var selectedInvoice by remember { mutableStateOf<JSONObject?>(null) }
        val invoices = subscription.optJSONArray("invoices") ?: org.json.JSONArray()
        (0 until invoices.length()).forEach { i ->
            val invoice = invoices.optJSONObject(i) ?: return@forEach
            InfoCard(invoice.optString("number"), "PEN ${invoice.optString("amount")} · ${invoice.optString("dueDate")}",
                onClick = { selectedInvoice = invoice }, trailing = { StatusPill(invoice.optString("status"), language) })
        }
        RecordDetailDialog("invoices", selectedInvoice, language) { selectedInvoice = null }
    }
}

@Composable
private fun PlatformSettingsPanel(actor: JSONObject, language: String) {
    var name by remember { mutableStateOf(MultiTenantStore.setting("platformName", "SafeLab")) }
    var support by remember { mutableStateOf(MultiTenantStore.setting("supportEmail", "support@safelab.test")) }
    var timezone by remember { mutableStateOf(MultiTenantStore.setting("timezone", "America/Lima")) }
    var saved by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PageBanner(tr(language, "Platform settings", "Configuración de plataforma"),
            tr(language, "General preferences and policies.", "Preferencias y políticas generales."))
        OutlinedTextField(name, { name = it; saved = false }, Modifier.fillMaxWidth(),
            label = { Text(tr(language, "Platform name", "Nombre de plataforma")) })
        OutlinedTextField(support, { support = it; saved = false }, Modifier.fillMaxWidth(),
            label = { Text(tr(language, "Support email", "Correo de soporte")) })
        OutlinedTextField(timezone, { timezone = it; saved = false }, Modifier.fillMaxWidth(),
            label = { Text(tr(language, "Timezone", "Zona horaria")) })
        InfoCard(tr(language, "Included employees", "Empleados incluidos"), "10")
        InfoCard(tr(language, "Invitation lifetime", "Duración de invitaciones"), "72 h")
        InfoCard(tr(language, "Manager seat", "Cupo del gerente"),
            tr(language, "Not counted", "No consume cupo"))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                name = MultiTenantStore.setting("platformName", "SafeLab")
                support = MultiTenantStore.setting("supportEmail", "support@safelab.test")
                timezone = MultiTenantStore.setting("timezone", "America/Lima")
                saved = false
            }, modifier = Modifier.weight(1f)) { Text(tr(language, "Cancel", "Cancelar")) }
            Button(onClick = {
                if (name.isNotBlank() && support.contains('@') && timezone.contains('/')) {
                    if (SafeLabApi.isMock) {
                        MultiTenantStore.saveSettings(actor, name, support, timezone); saved = true
                    }
                }
            }, modifier = Modifier.weight(1f)) { Text(tr(language, "Save", "Guardar")) }
        }
        if (!SafeLabApi.isMock) Text(tr(language, "Editing platform settings requires a backend endpoint and is disabled in API mode.",
            "La configuración de plataforma requiere un endpoint del backend y está deshabilitada en modo API."),
            color = MaterialTheme.colorScheme.error)
        if (saved) Text(tr(language, "Changes saved", "Cambios guardados"), color = MaterialTheme.colorScheme.primary)
    }
}
