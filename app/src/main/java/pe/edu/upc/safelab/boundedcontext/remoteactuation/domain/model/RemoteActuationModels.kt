package pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model

/** Device availability in the offline demonstration (not a live connection). */
enum class DeviceAvailability { ONLINE, OFFLINE }

/** Only a small set of predefined actions is exposed; arbitrary setpoints are not accepted. */
enum class ActuationAction { COOLING_BOOST, NORMAL_COOLING, RESTART_GATEWAY }

enum class RemoteCommandStatus { SIMULATED, CONFIRMED_SAMPLE, CANCELLED_SAMPLE }

enum class RemoteCommandError { NOT_FOUND, OFFLINE, NOT_ALLOWED, INVALID_ACTION, ALREADY_ACTIVE }

data class ActuatorDevice(
    val id: String,
    val assetId: String?,
    val name: String,
    val location: String,
    val equipmentCode: String,
    val availability: DeviceAvailability,
    val currentMode: String,
    val supportedActions: List<ActuationAction>,
    val lastSampleAt: String
)

data class RemoteCommand(
    val id: String,
    val deviceId: String,
    val deviceName: String,
    val action: ActuationAction,
    val actor: String,
    val requestedAt: String,
    val status: RemoteCommandStatus,
    val noteEn: String,
    val noteEs: String
)

/** A simulated request changes only the in-memory UI state. */
sealed class CommandRequestResult {
    data class Accepted(val command: RemoteCommand) : CommandRequestResult()
    data class Rejected(val error: RemoteCommandError) : CommandRequestResult()
}
