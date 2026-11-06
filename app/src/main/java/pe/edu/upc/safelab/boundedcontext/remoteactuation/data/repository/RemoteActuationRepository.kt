package pe.edu.upc.safelab.boundedcontext.remoteactuation.data.repository

import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.ActuationAction
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.ActuatorDevice
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.CommandRequestResult
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.DeviceAvailability
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.RemoteCommand
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.RemoteCommandError
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.RemoteCommandStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * AV2 offline repository. No APIs, sockets, Bluetooth or real equipment commands are used.
 * A future backend will own access control, safety interlocks and the auditable execution record.
 */
object RemoteActuationRepository {
    private val seedDevices = listOf(
        ActuatorDevice("ctrl-001", "asset-001", "Reagent Freezer A", "Storage 1 · Central Clinical Laboratory", "EQ-CEN-001", DeviceAvailability.ONLINE,
            "NORMAL", listOf(ActuationAction.COOLING_BOOST, ActuationAction.NORMAL_COOLING), "2026-11-05T21:15:00"),
        ActuatorDevice("ctrl-002", "asset-005", "North Lab Refrigerator", "Storage · North Province Clinical Lab", "EQ-NOR-001", DeviceAvailability.OFFLINE,
            "NORMAL", listOf(ActuationAction.COOLING_BOOST, ActuationAction.NORMAL_COOLING), "2026-11-04T12:40:00"),
        ActuatorDevice("ctrl-003", "asset-007", "Vaccine Fridge", "Vaccines · San Gabriel Hospital Pharmacy", "EQ-HSP-001", DeviceAvailability.ONLINE,
            "NORMAL", listOf(ActuationAction.COOLING_BOOST, ActuationAction.NORMAL_COOLING), "2026-11-05T21:10:00"),
        ActuatorDevice("ctrl-004", "asset-010", "PharmAndina Cold Room", "Cold Chain · PharmAndina Cold Chain", "EQ-PHA-001", DeviceAvailability.ONLINE,
            "BOOST", listOf(ActuationAction.COOLING_BOOST, ActuationAction.NORMAL_COOLING), "2026-11-05T21:08:00"),
        ActuatorDevice("ctrl-a01", null, "Cold Chamber A-01", "Central Clinical Laboratory", "ACT-CEN-A01", DeviceAvailability.ONLINE,
            "NORMAL", listOf(ActuationAction.COOLING_BOOST, ActuationAction.NORMAL_COOLING), "2026-11-05T20:50:00"),
        ActuatorDevice("ctrl-freezer-b02", null, "Freezer B-02", "Central Clinical Laboratory", "ACT-CEN-B02", DeviceAvailability.OFFLINE,
            "NORMAL", listOf(ActuationAction.COOLING_BOOST, ActuationAction.NORMAL_COOLING), "2026-11-04T11:10:00"),
        ActuatorDevice("ctrl-gw-02", null, "Gateway S-02", "Central Clinical Laboratory", "GW-S-02", DeviceAvailability.ONLINE,
            "READY", listOf(ActuationAction.RESTART_GATEWAY), "2026-11-05T20:55:00")
    )

    private val initialHistory = listOf(
        RemoteCommand("CMD-081", "ctrl-a01", "Cold Chamber A-01", ActuationAction.COOLING_BOOST, "Lab Technician", "2026-06-19T08:54:00",
            RemoteCommandStatus.CONFIRMED_SAMPLE, "Historical sample aligned with the audit trail; not issued by this app.",
            "Ejemplo histórico vinculado a auditoría; no fue enviado desde esta app."),
        RemoteCommand("CMD-080", "ctrl-gw-02", "Gateway S-02", ActuationAction.RESTART_GATEWAY, "SafeLab Administrator", "2026-06-19T08:11:00",
            RemoteCommandStatus.CONFIRMED_SAMPLE, "Historical mock record; no hardware connection is active.",
            "Registro histórico simulado; no hay conexión activa con hardware."),
        RemoteCommand("CMD-079", "ctrl-freezer-b02", "Freezer B-02", ActuationAction.NORMAL_COOLING, "Dr. Maria Lopez", "2026-06-18T17:34:00",
            RemoteCommandStatus.CANCELLED_SAMPLE, "Return-to-normal cooling request was cancelled before execution; demonstration history only.",
            "La orden de volver al enfriamiento normal se canceló antes de ejecutarse; historial de demostración.")
    )

    private val localCommands = mutableListOf<RemoteCommand>()
    private val simulatedModes = mutableMapOf<String, String>()
    private var counter = 1

    fun devices(): List<ActuatorDevice> = seedDevices.map { original ->
        original.copy(currentMode = simulatedModes[original.id] ?: original.currentMode)
    }

    fun device(id: String): ActuatorDevice? = devices().find { it.id == id }

    fun history(): List<RemoteCommand> = localCommands.asReversed() + initialHistory

    fun command(id: String): RemoteCommand? = history().find { it.id == id }

    fun availableActions(id: String): List<ActuationAction> = device(id)?.supportedActions.orEmpty()

    fun isAuthorized(role: String): Boolean = role.equals("ADMINISTRATOR", ignoreCase = true)

    /**
     * Applies a confirmed UI action to local sample state only.
     * Even a successful result means SIMULATED, never a command delivered to hardware.
     */
    fun simulateCommand(deviceId: String, action: ActuationAction, actor: String, role: String): CommandRequestResult {
        if (!isAuthorized(role)) return CommandRequestResult.Rejected(RemoteCommandError.NOT_ALLOWED)
        val target = device(deviceId) ?: return CommandRequestResult.Rejected(RemoteCommandError.NOT_FOUND)
        if (target.availability == DeviceAvailability.OFFLINE) return CommandRequestResult.Rejected(RemoteCommandError.OFFLINE)
        if (action !in target.supportedActions) return CommandRequestResult.Rejected(RemoteCommandError.INVALID_ACTION)
        val nextMode = when (action) {
            ActuationAction.COOLING_BOOST -> "BOOST"
            ActuationAction.NORMAL_COOLING -> "NORMAL"
            ActuationAction.RESTART_GATEWAY -> "READY"
        }
        if (action != ActuationAction.RESTART_GATEWAY && target.currentMode == nextMode) {
            return CommandRequestResult.Rejected(RemoteCommandError.ALREADY_ACTIVE)
        }
        val date = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val record = RemoteCommand("SIM-${counter.toString().padStart(3, '0')}", target.id, target.name,
            action, actor.ifBlank { "SafeLab user" }, date, RemoteCommandStatus.SIMULATED,
            "Confirmed in the demo. No command was transmitted to a device.",
            "Confirmado en la demostración. No se transmitió ningún comando a un equipo.")
        counter += 1
        simulatedModes[target.id] = nextMode
        localCommands += record
        return CommandRequestResult.Accepted(record)
    }

    /** Used by unit tests to isolate independent examples. */
    fun resetDemo() {
        localCommands.clear()
        simulatedModes.clear()
        counter = 1
    }
}
