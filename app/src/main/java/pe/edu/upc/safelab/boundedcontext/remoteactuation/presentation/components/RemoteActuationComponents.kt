package pe.edu.upc.safelab.boundedcontext.remoteactuation.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.ActuationAction
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.ActuatorDevice
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.DeviceAvailability
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.RemoteCommand
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.RemoteCommandStatus
import pe.edu.upc.safelab.common.tr
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess
import pe.edu.upc.safelab.ui.theme.SafeLabWarning
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun RemotePanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .13f))) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp), content = content)
    }
}

@Composable
fun RemoteStatusPill(label: String, positive: Boolean) {
    val tone = if (positive) SafeLabSuccess else SafeLabWarning
    Surface(shape = CircleShape, color = tone.copy(alpha = .13f)) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = tone, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun RemoteInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, modifier = Modifier.weight(.9f), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, modifier = Modifier.weight(1.15f), style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium)
    }
}

fun actionLabel(action: ActuationAction, language: String): String = when (action) {
    ActuationAction.COOLING_BOOST -> tr(language, "Enable cooling boost", "Activar refuerzo de enfriamiento")
    ActuationAction.NORMAL_COOLING -> tr(language, "Restore normal cooling", "Restablecer enfriamiento normal")
    ActuationAction.RESTART_GATEWAY -> tr(language, "Restart gateway", "Reiniciar pasarela")
}

fun modeLabel(mode: String, language: String): String = when (mode) {
    "BOOST" -> tr(language, "Boost (sample)", "Refuerzo (ejemplo)")
    "NORMAL" -> tr(language, "Normal (sample)", "Normal (ejemplo)")
    "READY" -> tr(language, "Ready (sample)", "Disponible (ejemplo)")
    else -> mode
}

fun deviceLocationLabel(location: String, language: String): String = if (language != "ES") location else when (location) {
    "Storage 1 · Central Clinical Laboratory" -> "Almacén 1 · Laboratorio clínico central"
    "Storage · North Province Clinical Lab" -> "Almacén · Laboratorio clínico del norte"
    "Vaccines · San Gabriel Hospital Pharmacy" -> "Vacunas · Farmacia del Hospital San Gabriel"
    "Cold Chain · PharmAndina" -> "Cadena de frío · PharmAndina"
    "Central Clinical Laboratory" -> "Laboratorio clínico central"
    else -> location
}

fun actorLabel(actor: String, language: String): String = if (language != "ES") actor else when (actor) {
    "Lab Technician" -> "Técnico de laboratorio"
    "SafeLab Administrator" -> "Administrador de SafeLab"
    else -> actor
}

fun commandStatusLabel(status: RemoteCommandStatus, language: String): String = when (status) {
    RemoteCommandStatus.SIMULATED -> tr(language, "Simulated", "Simulado")
    RemoteCommandStatus.CONFIRMED_SAMPLE -> tr(language, "Confirmed (sample)", "Confirmado (ejemplo)")
    RemoteCommandStatus.CANCELLED_SAMPLE -> tr(language, "Cancelled (sample)", "Cancelado (ejemplo)")
}

fun commandSampleDate(value: String, language: String): String = runCatching {
    val locale = if (language == "ES") Locale.forLanguageTag("es-PE") else Locale.US
    val pattern = if (language == "ES") "dd/MM/yyyy · HH:mm" else "MMM d, yyyy · HH:mm"
    LocalDateTime.parse(value).format(DateTimeFormatter.ofPattern(pattern, locale))
}.getOrElse { value }

@Composable
fun RemoteDeviceRow(device: ActuatorDevice, language: String, onOpen: () -> Unit) {
    val online = device.availability == DeviceAvailability.ONLINE
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .13f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .10f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center) {
                Text(if (device.assetId == null) "⌁" else "⚙", color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(device.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(device.equipmentCode, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(modeLabel(device.currentMode, language), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                RemoteStatusPill(if (online) tr(language, "Online", "En línea") else tr(language, "Offline", "Sin conexión"), online)
                Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun RemoteCommandRow(command: RemoteCommand, language: String, onOpen: () -> Unit) {
    val positive = command.status != RemoteCommandStatus.CANCELLED_SAMPLE
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .13f))) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(actionLabel(command.action, language), fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Text(command.deviceName, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                RemoteStatusPill(commandStatusLabel(command.status, language), positive)
            }
            Text("${command.id} · ${commandSampleDate(command.requestedAt, language)}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(tr(language, "View command details  ›", "Ver detalle del comando  ›"),
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold)
        }
    }
}
