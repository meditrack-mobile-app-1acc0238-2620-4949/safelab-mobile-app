package pe.edu.upc.safelab.boundedcontext.remoteactuation.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.remoteactuation.data.repository.RemoteActuationRepository as Repo
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.ActuationAction
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.ActuatorDevice
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.CommandRequestResult
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.DeviceAvailability
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.RemoteCommand
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.RemoteCommandError
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.RemoteCommandStatus
import pe.edu.upc.safelab.boundedcontext.remoteactuation.presentation.components.RemoteCommandRow
import pe.edu.upc.safelab.boundedcontext.remoteactuation.presentation.components.RemoteDeviceRow
import pe.edu.upc.safelab.boundedcontext.remoteactuation.presentation.components.RemoteInfoRow
import pe.edu.upc.safelab.boundedcontext.remoteactuation.presentation.components.RemotePanel
import pe.edu.upc.safelab.boundedcontext.remoteactuation.presentation.components.RemoteStatusPill
import pe.edu.upc.safelab.boundedcontext.remoteactuation.presentation.components.actionLabel
import pe.edu.upc.safelab.boundedcontext.remoteactuation.presentation.components.actorLabel
import pe.edu.upc.safelab.boundedcontext.remoteactuation.presentation.components.deviceLocationLabel
import pe.edu.upc.safelab.boundedcontext.remoteactuation.presentation.components.commandSampleDate
import pe.edu.upc.safelab.boundedcontext.remoteactuation.presentation.components.commandStatusLabel
import pe.edu.upc.safelab.boundedcontext.remoteactuation.presentation.components.modeLabel
import pe.edu.upc.safelab.common.LocalAppLanguage
import pe.edu.upc.safelab.common.tr

private enum class RemoteSection { OVERVIEW, DEVICES, HISTORY }
private enum class DeviceFilter { ALL, ONLINE, OFFLINE }
private data class ProposedCommand(val deviceId: String, val action: ActuationAction)

/**
 * Safe, offline demonstration: UI confirmation and sample state only.
 * The app never sends an instruction to a real sensor, refrigerator or gateway.
 */
@Composable
fun RemoteActuationEntryScreen(actor: String, role: String) {
    val language = LocalAppLanguage.current
    var sectionName by rememberSaveable { mutableStateOf(RemoteSection.OVERVIEW.name) }
    var filterName by rememberSaveable { mutableStateOf(DeviceFilter.ALL.name) }
    var selectedDeviceId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCommandId by rememberSaveable { mutableStateOf<String?>(null) }
    var proposed by remember { mutableStateOf<ProposedCommand?>(null) }
    var lastResult by remember { mutableStateOf<CommandRequestResult?>(null) }
    var revision by remember { mutableIntStateOf(0) }

    fun back() {
        when {
            selectedCommandId != null -> selectedCommandId = null
            selectedDeviceId != null -> { selectedDeviceId = null; lastResult = null }
            sectionName != RemoteSection.OVERVIEW.name -> sectionName = RemoteSection.OVERVIEW.name
        }
    }
    BackHandler(enabled = selectedCommandId != null || selectedDeviceId != null ||
        sectionName != RemoteSection.OVERVIEW.name) { back() }

    // A local revision refreshes the mocked cards after a simulated command.
    val currentDevice = selectedDeviceId?.let { Repo.device(it) }
    val currentCommand = selectedCommandId?.let { Repo.command(it) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (selectedDeviceId == null && selectedCommandId == null) {
            LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RemoteSection.entries) { tab ->
                    val selected = sectionName == tab.name
                    Surface(modifier = Modifier.clickable { sectionName = tab.name },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant) {
                        Text(when (tab) {
                            RemoteSection.OVERVIEW -> tr(language, "Overview", "Resumen")
                            RemoteSection.DEVICES -> tr(language, "Devices", "Dispositivos")
                            RemoteSection.HISTORY -> tr(language, "History", "Historial")
                        }, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
        key(revision) {
        when {
            selectedCommandId != null -> RemoteCommandDetail(currentCommand, language, ::back)
            selectedDeviceId != null -> RemoteDeviceDetail(currentDevice, language, Repo.isAuthorized(role),
                lastResult?.let { resultFeedback(it, language) },
                onBack = ::back,
                onPropose = { action -> proposed = ProposedCommand(selectedDeviceId!!, action) },
                onHistory = { id -> selectedCommandId = id })
            sectionName == RemoteSection.OVERVIEW.name -> RemoteOverview(language,
                onDevices = { sectionName = RemoteSection.DEVICES.name },
                onHistory = { sectionName = RemoteSection.HISTORY.name },
                onOpenDevice = { selectedDeviceId = it },
                onOpenCommand = { selectedCommandId = it })
            sectionName == RemoteSection.DEVICES.name -> RemoteDevices(language, filterName,
                onFilter = { filterName = it }, onOpen = { selectedDeviceId = it })
            else -> RemoteHistory(language, onOpen = { selectedCommandId = it })
        }
        }
    }

    val request = proposed
    if (request != null) {
        val target = Repo.device(request.deviceId)
        AlertDialog(
            onDismissRequest = { proposed = null },
            title = { Text(tr(language, "Confirm simulation", "Confirmar simulación"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(target?.name.orEmpty(), fontWeight = FontWeight.SemiBold)
                    Text(actionLabel(request.action, language))
                    Text(tr(language,
                        "This action only changes sample data on this screen. No command is sent to physical equipment.",
                        "Esta acción solo modifica datos de ejemplo en esta pantalla. No se envía ningún comando a equipos físicos."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val result = Repo.simulateCommand(request.deviceId, request.action, actor, role)
                    lastResult = result
                    revision += 1
                    proposed = null
                }) { Text(tr(language, "Simulate", "Simular")) }
            },
            dismissButton = {
                TextButton(onClick = { proposed = null }) { Text(tr(language, "Cancel", "Cancelar")) }
            }
        )
    }
}

@Composable
private fun RemoteOverview(
    language: String, onDevices: () -> Unit, onHistory: () -> Unit,
    onOpenDevice: (String) -> Unit, onOpenCommand: (String) -> Unit
) {
    val devices = Repo.devices()
    val online = devices.count { it.availability == DeviceAvailability.ONLINE }
    val history = Repo.history()
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = .17f), MaterialTheme.colorScheme.surface)))
                    .padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(tr(language, "REMOTE CONTROL & ACTUATION", "CONTROL Y ACTUACIÓN REMOTOS"),
                        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary)
                    Text(tr(language, "Device control center", "Centro de control de dispositivos"),
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text(tr(language, "Explore connected actuators and review command requests from one place.",
                        "Explora actuadores conectados y consulta solicitudes de comando desde un solo lugar."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    RemoteStatusPill(tr(language, "Offline demo · no hardware control", "Demo sin conexión · sin control físico"), true)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RemoteStat(tr(language, "Available", "Disponibles"), "$online / ${devices.size}", Modifier.weight(1f))
                RemoteStat(tr(language, "Commands", "Comandos"), "${history.size}", Modifier.weight(1f))
            }
        }
        item {
            RemotePanel {
                Text(tr(language, "Safe demonstration", "Demostración segura"),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(tr(language,
                    "Device availability and operating modes are fictional. Confirmed actions create local simulation records only.",
                    "La disponibilidad y los modos operativos son ficticios. Las acciones confirmadas solo crean registros locales de simulación."),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = onDevices, shape = RoundedCornerShape(12.dp)) {
                    Text(tr(language, "Explore devices", "Explorar dispositivos"))
                }
            }
        }
        item { RemoteHeading(tr(language, "Monitored controllers", "Controladores monitoreados"),
            tr(language, "Select a device to view actions.", "Selecciona un dispositivo para consultar acciones.")) }
        items(devices.take(3), key = { it.id }) { RemoteDeviceRow(it, language) { onOpenDevice(it.id) } }
        item {
            OutlinedButton(onClick = onDevices, modifier = Modifier.fillMaxWidth()) {
                Text(tr(language, "See all devices", "Ver todos los dispositivos"))
            }
        }
        item { RemoteHeading(tr(language, "Recent commands", "Comandos recientes"),
            tr(language, "Historical and simulated records", "Registros históricos y simulados")) }
        items(history.take(2), key = { it.id }) { RemoteCommandRow(it, language) { onOpenCommand(it.id) } }
        item {
            OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth()) {
                Text(tr(language, "See full history", "Ver historial completo"))
            }
        }
    }
}

@Composable
private fun RemoteStat(label: String, value: String, modifier: Modifier) {
    RemotePanel(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun RemoteHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RemoteDevices(language: String, filter: String, onFilter: (String) -> Unit, onOpen: (String) -> Unit) {
    val devices = Repo.devices().filter { device -> when (filter) {
        DeviceFilter.ONLINE.name -> device.availability == DeviceAvailability.ONLINE
        DeviceFilter.OFFLINE.name -> device.availability == DeviceAvailability.OFFLINE
        else -> true
    } }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { RemoteHeading(tr(language, "Actuator devices", "Dispositivos actuadores"),
            tr(language, "Sample connectivity and available actions", "Conectividad y acciones disponibles de ejemplo")) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DeviceFilter.entries.forEach { option ->
                    val selected = option.name == filter
                    Surface(modifier = Modifier.clickable { onFilter(option.name) }, shape = RoundedCornerShape(10.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant) {
                        Text(when (option) {
                            DeviceFilter.ALL -> tr(language, "All", "Todos")
                            DeviceFilter.ONLINE -> tr(language, "Online", "En línea")
                            DeviceFilter.OFFLINE -> tr(language, "Offline", "Sin conexión")
                        }, modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        items(devices, key = { it.id }) { device -> RemoteDeviceRow(device, language) { onOpen(device.id) } }
    }
}

@Composable
private fun RemoteDeviceDetail(
    device: ActuatorDevice?, language: String, authorized: Boolean, feedback: String?,
    onBack: () -> Unit, onPropose: (ActuationAction) -> Unit, onHistory: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { OutlinedButton(onClick = onBack) { Text(tr(language, "← Back to devices", "← Volver a dispositivos")) } }
        if (device == null) {
            item { RemotePanel { Text(tr(language, "Device not found", "Dispositivo no encontrado")) } }
        } else {
            val online = device.availability == DeviceAvailability.ONLINE
            item {
                RemotePanel {
                    Text(device.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    RemoteStatusPill(if (online) tr(language, "Online (sample)", "En línea (ejemplo)")
                        else tr(language, "Offline (sample)", "Sin conexión (ejemplo)"), online)
                    RemoteInfoRow(tr(language, "Equipment code", "Código del equipo"), device.equipmentCode)
                    RemoteInfoRow(tr(language, "Asset reference", "Referencia de activo"), device.assetId ?: "—")
                    RemoteInfoRow(tr(language, "Location", "Ubicación"), deviceLocationLabel(device.location, language))
                    RemoteInfoRow(tr(language, "Operating mode", "Modo de operación"), modeLabel(device.currentMode, language))
                    RemoteInfoRow(tr(language, "Last sample contact", "Último contacto de ejemplo"),
                        commandSampleDate(device.lastSampleAt, language))
                }
            }
            item {
                RemotePanel {
                    Text(tr(language, "Available actions", "Acciones disponibles"),
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(tr(language, "Choose a predefined action. A confirmation is required before its local simulation.",
                        "Elige una acción predefinida. Se pedirá confirmación antes de simularla localmente."),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!online) Text(tr(language, "This sample device is offline. Actions are disabled.",
                        "Este dispositivo de ejemplo está desconectado. Las acciones están deshabilitadas."),
                        color = MaterialTheme.colorScheme.error)
                    if (!authorized) Text(tr(language, "Only administrators can simulate actions in this demo.",
                        "Solo los administradores pueden simular acciones en esta demo."),
                        color = MaterialTheme.colorScheme.error)
                    device.supportedActions.forEach { action ->
                        val sameMode = (action == ActuationAction.COOLING_BOOST && device.currentMode == "BOOST") ||
                            (action == ActuationAction.NORMAL_COOLING && device.currentMode == "NORMAL")
                        OutlinedButton(onClick = { onPropose(action) }, modifier = Modifier.fillMaxWidth(),
                            enabled = online && authorized && !sameMode,
                            shape = RoundedCornerShape(12.dp)) {
                            Text(actionLabel(action, language))
                        }
                    }
                    if (feedback != null) {
                        Text(feedback, color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            val recent = Repo.history().filter { it.deviceId == device.id }.take(3)
            item { RemoteHeading(tr(language, "Device command history", "Historial de comandos del dispositivo"),
                tr(language, "Select a record for details.", "Selecciona un registro para ver detalles.")) }
            if (recent.isEmpty()) item { RemotePanel { Text(tr(language, "No commands recorded", "No hay comandos registrados")) } }
            items(recent, key = { it.id }) { command -> RemoteCommandRow(command, language) { onHistory(command.id) } }
        }
    }
}

@Composable
private fun RemoteHistory(language: String, onOpen: (String) -> Unit) {
    val history = Repo.history()
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { RemoteHeading(tr(language, "Command history", "Historial de comandos"),
            tr(language, "Sample audit references and actions simulated in this app session.",
                "Referencias de auditoría de ejemplo y acciones simuladas durante esta sesión.")) }
        items(history, key = { it.id }) { command -> RemoteCommandRow(command, language) { onOpen(command.id) } }
    }
}

@Composable
private fun RemoteCommandDetail(command: RemoteCommand?, language: String, onBack: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { OutlinedButton(onClick = onBack) { Text(tr(language, "← Back", "← Volver")) } }
        if (command == null) {
            item { RemotePanel { Text(tr(language, "Command not found", "Comando no encontrado")) } }
        } else {
            item {
                RemotePanel {
                    Text(tr(language, "Command details", "Detalle del comando"),
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    RemoteStatusPill(commandStatusLabel(command.status, language),
                        command.status != RemoteCommandStatus.CANCELLED_SAMPLE)
                    RemoteInfoRow(tr(language, "Reference", "Referencia"), command.id)
                    RemoteInfoRow(tr(language, "Device", "Dispositivo"), command.deviceName)
                    RemoteInfoRow(tr(language, "Action", "Acción"), actionLabel(command.action, language))
                    RemoteInfoRow(tr(language, "Requested by", "Solicitado por"), actorLabel(command.actor, language))
                    RemoteInfoRow(tr(language, "Date and time", "Fecha y hora"), commandSampleDate(command.requestedAt, language))
                    RemoteInfoRow(tr(language, "Status", "Estado"), commandStatusLabel(command.status, language))
                }
            }
            item {
                RemotePanel {
                    Text(tr(language, "Execution information", "Información de ejecución"),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(tr(language, command.noteEn, command.noteEs),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(tr(language, "No physical device response is available in this version.",
                        "En esta versión no existe una respuesta de un equipo físico."),
                        color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun errorMessage(error: RemoteCommandError, language: String): String = when (error) {
    RemoteCommandError.NOT_FOUND -> tr(language, "Device not found.", "Dispositivo no encontrado.")
    RemoteCommandError.OFFLINE -> tr(language, "Device offline: simulation rejected.", "Dispositivo desconectado: simulación rechazada.")
    RemoteCommandError.NOT_ALLOWED -> tr(language, "This role cannot simulate commands.", "Este rol no puede simular comandos.")
    RemoteCommandError.INVALID_ACTION -> tr(language, "Action unavailable for this device.", "Acción no disponible para este dispositivo.")
    RemoteCommandError.ALREADY_ACTIVE -> tr(language, "The sample device already uses that mode.", "El dispositivo de ejemplo ya utiliza ese modo.")
}

private fun resultFeedback(result: CommandRequestResult, language: String): String = when (result) {
    is CommandRequestResult.Accepted -> tr(language,
        "Simulation saved as ${result.command.id}. No command transmitted.",
        "Simulación guardada como ${result.command.id}. No se transmitió ningún comando.")
    is CommandRequestResult.Rejected -> errorMessage(result.error, language)
}
