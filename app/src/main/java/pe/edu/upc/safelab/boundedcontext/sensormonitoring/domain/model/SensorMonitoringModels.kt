package pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model

import java.util.Locale
import kotlin.math.roundToInt

/**
 * Sensor Monitoring bounded context.
 * Sensors, their readings, threshold rules, calibrations and telemetry batches.
 * Facilities and assets are referenced by id; their master data belongs to Monitoring Organization.
 * Dates are ISO-8601 strings (e.g. 2026-06-19T02:25:00Z).
 */

enum class SensorType(val label: String) {
    TEMPERATURE("Temperature"),
    HUMIDITY("Humidity"),
    DOOR_STATUS("Door status"),
    VIBRATION("Vibration"),
    ENERGY("Energy");

    /** Text shown for a reading value: "Closed"/"Open" for doors, the number with its unit otherwise. */
    fun formatValue(value: Double, unit: String): String = when (this) {
        DOOR_STATUS -> if (value >= 1.0) "Open" else "Closed"
        HUMIDITY -> "${value.roundToInt()} $unit".trim()
        else -> "${"%.1f".format(Locale.US, value)} $unit".trim()
    }
}

/** Status of the device itself. */
enum class SensorStatus(val label: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    DISCONNECTED("Disconnected"),
    MAINTENANCE("Maintenance")
}

enum class ReadingStatus(val label: String) {
    NORMAL("Normal"),
    OUT_OF_RANGE("Out of range"),
    INVALID("Invalid")
}

enum class ThresholdStatus(val label: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive")
}

enum class CalibrationStatus(val label: String) {
    PENDING("Pending"),
    COMPLETED("Completed"),
    OVERDUE("Overdue")
}

enum class BatchStatus(val label: String) {
    RECEIVED("Received"),
    PROCESSED("Processed"),
    REJECTED("Rejected")
}

/** Allowed range of a sensor. A reading inside [minValue, maxValue] is normal. */
data class ThresholdRule(
    val id: String,
    val minValue: Double,
    val maxValue: Double,
    val unit: String,
    val status: ThresholdStatus
) {
    init {
        require(id.isNotBlank()) { "Threshold id is required." }
        require(minValue <= maxValue) { "Minimum value cannot be greater than the maximum value." }
    }

    fun validate(value: Double): Boolean = value in minValue..maxValue

    /**
     * Position of a value over the range, from 0 to 1, leaving half of the range as margin on each side.
     * The allowed range is therefore drawn between 0.25 and 0.75.
     */
    fun position(value: Double): Float {
        val span = maxValue - minValue
        if (span == 0.0) return 0.5f
        val from = minValue - span / 2
        return (((value - from) / (span * 2)).toFloat()).coerceIn(0f, 1f)
    }

    fun activate(): ThresholdRule = copy(status = ThresholdStatus.ACTIVE)

    fun deactivate(): ThresholdRule = copy(status = ThresholdStatus.INACTIVE)
}

/**
 * Value captured by a sensor. For door sensors [value] is 0.0 (Closed) or 1.0 (Open)
 * and [displayValue] shows "Closed" / "Open".
 */
data class SensorReading(
    val id: String,
    val sensorId: String,
    val value: Double,
    val displayValue: String,
    val unit: String,
    val recordedAt: String,
    val status: ReadingStatus
) {
    init {
        require(id.isNotBlank()) { "Reading id is required." }
        require(sensorId.isNotBlank()) { "Sensor id is required." }
        require(displayValue.isNotBlank()) { "Display value is required." }
        require(recordedAt.isNotBlank()) { "Recording date is required." }
    }

    fun markAsNormal(): SensorReading = copy(status = ReadingStatus.NORMAL)

    fun markAsOutOfRange(): SensorReading = copy(status = ReadingStatus.OUT_OF_RANGE)
}

data class SensorCalibration(
    val id: String,
    val sensorId: String,
    val calibratedAt: String?,
    val nextCalibrationAt: String,
    val performedBy: String?,
    val status: CalibrationStatus
) {
    init {
        require(id.isNotBlank()) { "Calibration id is required." }
        require(sensorId.isNotBlank()) { "Sensor id is required." }
        require(nextCalibrationAt.isNotBlank()) { "Next calibration date is required." }
    }

    fun markAsCompleted(): SensorCalibration = copy(status = CalibrationStatus.COMPLETED)

    fun markAsOverdue(): SensorCalibration = copy(status = CalibrationStatus.OVERDUE)
}

data class TelemetryBatch(
    val id: String,
    val sensorId: String,
    val receivedAt: String,
    val readingCount: Int,
    val status: BatchStatus
) {
    init {
        require(id.isNotBlank()) { "Batch id is required." }
        require(sensorId.isNotBlank()) { "Sensor id is required." }
        require(receivedAt.isNotBlank()) { "Reception date is required." }
        require(readingCount >= 0) { "Reading count cannot be negative." }
    }

    fun process(): TelemetryBatch = copy(status = BatchStatus.PROCESSED)

    fun reject(): TelemetryBatch = copy(status = BatchStatus.REJECTED)
}

data class Sensor(
    val id: String,
    val code: String,
    val name: String,
    val type: SensorType,
    val status: SensorStatus,
    val facilityId: String,
    val assetId: String,
    val assetName: String,
    val facilityName: String,
    val unit: String,
    val responsible: String,
    val installedAt: String,
    val threshold: ThresholdRule,
    val latestReading: SensorReading
) {
    init {
        require(id.isNotBlank()) { "Sensor id is required." }
        require(code.isNotBlank()) { "Sensor code is required." }
        require(name.isNotBlank()) { "Sensor name is required." }
        require(facilityId.isNotBlank()) { "Facility is required." }
        require(assetId.isNotBlank()) { "Asset is required." }
        require(assetName.isNotBlank()) { "Asset name is required." }
        require(facilityName.isNotBlank()) { "Facility name is required." }
        require(responsible.isNotBlank()) { "Responsible is required." }
        require(installedAt.isNotBlank()) { "Installation date is required." }
    }

    val isOnline: Boolean
        get() = status != SensorStatus.DISCONNECTED

    val readingStatus: ReadingStatus
        get() = latestReading.status

    fun activate(): Sensor = copy(status = SensorStatus.ACTIVE)

    fun deactivate(): Sensor = copy(status = SensorStatus.INACTIVE)

    fun markAsDisconnected(): Sensor = copy(status = SensorStatus.DISCONNECTED)
}

/** Domain result: counters of the sensor fleet. */
data class SensorSummary(
    val total: Int,
    val normal: Int,
    val outOfRange: Int,
    val invalid: Int,
    val offline: Int
)

/** Domain result: statistics of the readings of one sensor. */
data class ReadingStats(
    val min: Double,
    val max: Double,
    val average: Double,
    val outOfRangeCount: Int,
    val count: Int
)
