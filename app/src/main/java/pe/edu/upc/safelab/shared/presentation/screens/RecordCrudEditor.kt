package pe.edu.upc.safelab.shared.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.json.JSONObject
import pe.edu.upc.safelab.common.tr
import pe.edu.upc.safelab.shared.infrastructure.data.MultiTenantStore

/**
 * BC-scoped editor: the same permission gates and Spring Boot endpoints as Web.
 * Only supported fields are sent. No UI-only success, and no edits for read-only users.
 */
@Composable
fun RecordCrudEditor(
    record: JSONObject,
    collection: String,
    actor: JSONObject,
    language: String,
    onDismiss: () -> Unit
) {
    val required = when (collection) {
        "assets" -> "assets.manage"
        "sensors" -> "sensors.configure"
        "maintenanceRecords" -> "maintenance.manage"
        else -> return
    }
    if (!MultiTenantStore.can(actor, required)) return
    val scope = rememberCoroutineScope()
    val id = record.optString("id")
    var saving by remember(id) { mutableStateOf(false) }
    var error by remember(id) { mutableStateOf("") }
    var name by remember(id) { mutableStateOf(record.optString("name")) }
    var category by remember(id) { mutableStateOf(record.optString("category")) }
    var storageUnit by remember(id) { mutableStateOf(record.optString("storageUnit")) }
    var location by remember(id) { mutableStateOf(record.optString("location")) }
    var inspection by remember(id) { mutableStateOf(if (record.isNull("lastInspection")) "" else record.optString("lastInspection")) }
    var status by remember(id) { mutableStateOf(record.optString("status")) }
    var facilityId by remember(id) { mutableStateOf(record.optString("facilityId")) }
    var responsibleId by remember(id) { mutableStateOf(if (record.isNull("responsibleUserId")) "" else record.optString("responsibleUserId")) }
    var code by remember(id) { mutableStateOf(record.optString("code")) }
    var unit by remember(id) { mutableStateOf(record.optString("unit")) }
    var minimum by remember(id) { mutableStateOf(record.optString("min")) }
    var maximum by remember(id) { mutableStateOf(record.optString("max")) }
    var reading by remember(id) { mutableStateOf(record.optString("value")) }
    var notes by remember(id) { mutableStateOf(record.optString("notes")) }
    var responsibleText by remember(id) { mutableStateOf(record.optString("responsible")) }
    val manager = actor.optString("role") == "organizationAdmin"
    val facilities = MultiTenantStore.visibleItems("facilities", actor)
    val employees = MultiTenantStore.employees(actor.optString("organizationId"))
        .filter { employee ->
            employee.optString("status") == "active" &&
            (MultiTenantStore.arrayStrings(employee.optJSONArray("facilityIds")).let { it.isEmpty() || facilityId in it })
        }

    @Composable fun textField(value: String, change: (String) -> Unit, label: String) {
        OutlinedTextField(value = value, onValueChange = change, modifier = Modifier.fillMaxWidth(),
            label = { Text(label) }, singleLine = true, enabled = !saving)
    }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(tr(language, "Edit record", "Editar registro")) },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (collection) {
                    "assets" -> {
                        textField(name, { name = it }, tr(language, "Asset name", "Nombre del equipo"))
                        textField(category, { category = it }, tr(language, "Category", "Categoría"))
                        textField(storageUnit, { storageUnit = it }, tr(language, "Storage unit", "Unidad de almacenamiento"))
                        textField(location, { location = it }, tr(language, "Location", "Ubicación"))
                        textField(inspection, { inspection = it }, tr(language, "Last inspection (YYYY-MM-DD)", "Última inspección (AAAA-MM-DD)"))
                        ChoiceSelector(tr(language, "Facility", "Sede"), facilityId,
                            facilities.map { it.optString("id") to it.optString("name") }, !saving) { facilityId = it }
                        ChoiceSelector(tr(language, "Status", "Estado"), status,
                            listOf("compliant", "warning", "critical", "maintenance").map { it to it }, !saving) { status = it }
                        if (manager) {
                            val choices = listOf("" to tr(language, "Unassigned", "Sin asignar")) +
                                employees.map { it.optString("id") to it.optString("fullName") }
                            ChoiceSelector(tr(language, "Responsible employee", "Empleado responsable"), responsibleId,
                                choices, !saving) { responsibleId = it }
                        }
                    }
                    "sensors" -> {
                        textField(name, { name = it }, tr(language, "Sensor name", "Nombre del sensor"))
                        textField(code, { code = it }, tr(language, "Code", "Código"))
                        textField(unit, { unit = it }, tr(language, "Unit", "Unidad"))
                        textField(minimum, { minimum = it }, tr(language, "Minimum", "Mínimo"))
                        textField(maximum, { maximum = it }, tr(language, "Maximum", "Máximo"))
                        textField(reading, { reading = it }, tr(language, "Current reading", "Lectura actual"))
                    }
                    "maintenanceRecords" -> {
                        textField(status, { status = it }, tr(language, "Status", "Estado"))
                        textField(notes, { notes = it }, tr(language, "Notes", "Observaciones"))
                        textField(responsibleText, { responsibleText = it }, tr(language, "Responsible", "Responsable"))
                    }
                }
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(enabled = !saving, onClick = {
                val payload = JSONObject()
                when (collection) {
                    "assets" -> {
                        if (name.trim().length < 3 || category.isBlank() || facilityId.isBlank()) {
                            error = tr(language, "Name (3+ characters), category and facility are required.",
                                "Se requieren nombre (3+ caracteres), categoría y sede.")
                            return@TextButton
                        }
                        if (inspection.isNotBlank() && !Regex("\\d{4}-\\d{2}-\\d{2}").matches(inspection)) {
                            error = tr(language, "Date format must be YYYY-MM-DD", "La fecha debe ser AAAA-MM-DD")
                            return@TextButton
                        }
                        if (manager && responsibleId.isNotBlank() && employees.none { it.optString("id") == responsibleId }) {
                            error = tr(language, "Select an employee from the facility.", "Selecciona un empleado autorizado para la sede.")
                            return@TextButton
                        }
                        payload.put("name", name.trim()).put("category", category.trim())
                            .put("storageUnit", storageUnit.trim()).put("location", location.trim())
                            .put("facilityId", facilityId).put("status", status)
                        if (inspection.isNotBlank()) payload.put("lastInspection", inspection)
                        if (manager) payload.put("responsibleUserId", if (responsibleId.isBlank()) JSONObject.NULL else responsibleId)
                    }
                    "sensors" -> {
                        val min = minimum.toDoubleOrNull()
                        val max = maximum.toDoubleOrNull()
                        if (name.isBlank() || min == null || max == null || min >= max) {
                            error = tr(language, "Invalid name or threshold range", "Nombre o umbrales incorrectos")
                            return@TextButton
                        }
                        payload.put("name", name.trim()).put("code", code.trim()).put("unit", unit.trim())
                            .put("min", min).put("max", max)
                        if (reading.isNotBlank() && reading.toDoubleOrNull() == null) {
                            error = tr(language, "Reading must be numeric", "La lectura debe ser numérica")
                            return@TextButton
                        }
                    }
                    "maintenanceRecords" -> {
                        payload.put("status", status.trim()).put("notes", notes.trim())
                            .put("responsible", responsibleText.trim())
                    }
                }
                scope.launch {
                    saving = true; error = ""
                    try {
                        if (!MultiTenantStore.updateRecordRemote(collection, id, actor, payload))
                            throw IllegalStateException(tr(language, "Permission denied", "Permiso denegado"))
                        if (collection == "sensors" && reading.isNotBlank() &&
                            reading.toDoubleOrNull() != record.optDouble("value")) {
                            MultiTenantStore.updateSensorReadingRemote(id, actor, reading.toDouble())
                        }
                        onDismiss()
                    } catch (ex: Exception) { error = ex.message ?: "HTTP error" }
                    finally { saving = false }
                }
            }) { Text(if (saving) tr(language, "Saving...", "Guardando...") else tr(language, "Save", "Guardar")) }
        },
        dismissButton = { TextButton(enabled = !saving, onClick = onDismiss) { Text(tr(language, "Cancel", "Cancelar")) } }
    )
}

@Composable
private fun ChoiceSelector(label: String, selectedId: String, choices: List<Pair<String, String>>,
                           enabled: Boolean, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        OutlinedButton(onClick = { expanded = true }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Text(choices.firstOrNull { it.first == selectedId }?.second ?: selectedId.ifBlank { "—" })
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            choices.forEach { (id, name) ->
                DropdownMenuItem(text = { Text(name) }, onClick = { onSelected(id); expanded = false })
            }
        }
    }
}
