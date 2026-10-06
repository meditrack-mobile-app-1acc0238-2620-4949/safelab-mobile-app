package pe.edu.upc.safelab.boundedcontext.sensormonitoring.data.repository

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.time.Duration
import java.time.Instant
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.data.local.SensorMonitoringMockData
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.CalibrationStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.ReadingStats
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.ReadingStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.Sensor
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorCalibration
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorReading
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorSummary
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorType
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.TelemetryBatch
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.ThresholdRule
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.ThresholdStatus

/**
 * In-memory repository for TB1. Readings and calibrations registered in the app are kept
 * while it is open. It will be replaced by a remote repository (SafeLab Platform API).
 */
object SensorMonitoringRepository {

    private const val CALIBRATION_INTERVAL_DAYS = 90L

    private val sensorList: SnapshotStateList<Sensor> =
        mutableStateListOf(*SensorMonitoringMockData.sensors.toTypedArray())

    private val readings: SnapshotStateList<SensorReading> =
        mutableStateListOf(*SensorMonitoringMockData.readings.toTypedArray())

    private val calibrations: SnapshotStateList<SensorCalibration> =
        mutableStateListOf(*SensorMonitoringMockData.calibrations.toTypedArray())

    private val batches: List<TelemetryBatch> = SensorMonitoringMockData.batches

    /** Minutes elapsed since the mock clock started; every command moves it one minute forward. */
    private var clockTicks = 0L

    // ---------- Queries ----------

    /** Current moment of the mock clock, used for "time since" labels and for new records. */
    fun currentTime(): Instant =
        Instant.parse(SensorMonitoringMockData.NOW).plus(Duration.ofMinutes(clockTicks))

    /** Live monitoring: search by name, code or responsible; most recent reading first. */
    fun sensors(
        query: String = "",
        type: SensorType? = null,
        readingStatus: ReadingStatus? = null
    ): List<Sensor> {
        val text = query.trim()
        return sensorList
            .filter { type == null || it.type == type }
            .filter { readingStatus == null || it.readingStatus == readingStatus }
            .filter {
                text.isEmpty() ||
                    it.name.contains(text, ignoreCase = true) ||
                    it.code.contains(text, ignoreCase = true) ||
                    it.responsible.contains(text, ignoreCase = true)
            }
            .sortedByDescending { it.latestReading.recordedAt }
    }

    fun findSensor(sensorId: String?): Sensor? = sensorList.firstOrNull { it.id == sensorId }

    /** Historical readings of a sensor, newest first. */
    fun readingsOf(sensorId: String?, status: ReadingStatus? = null): List<SensorReading> =
        readings
            .filter { it.sensorId == sensorId }
            .filter { status == null || it.status == status }
            .sortedByDescending { it.recordedAt }

    fun thresholdOf(sensorId: String?): ThresholdRule? = findSensor(sensorId)?.threshold

    /** Calibrations of a sensor, the one due last first. */
    fun calibrationsOf(sensorId: String?): List<SensorCalibration> =
        calibrations
            .filter { it.sensorId == sensorId }
            .sortedByDescending { it.nextCalibrationAt }

    /** Date of the next calibration: the soonest open one or, if none is open, the one already scheduled. */
    fun nextCalibrationOf(sensorId: String?): String? {
        val all = calibrationsOf(sensorId)
        return all.filter { it.status != CalibrationStatus.COMPLETED }.minOfOrNull { it.nextCalibrationAt }
            ?: all.maxOfOrNull { it.nextCalibrationAt }
    }

    /** Telemetry batches of a sensor, newest first. */
    fun batchesOf(sensorId: String?): List<TelemetryBatch> =
        batches
            .filter { it.sensorId == sensorId }
            .sortedByDescending { it.receivedAt }

    /** Sensors that are not sending data: disconnected first, then maintenance and inactive. */
    fun offlineSensors(): List<Sensor> =
        sensorList
            .filter { it.status != SensorStatus.ACTIVE }
            .sortedWith(
                compareBy<Sensor> { it.status != SensorStatus.DISCONNECTED }
                    .thenBy { it.latestReading.recordedAt }
            )

    fun summary(): SensorSummary = SensorSummary(
        total = sensorList.size,
        normal = sensorList.count { it.readingStatus == ReadingStatus.NORMAL },
        outOfRange = sensorList.count { it.readingStatus == ReadingStatus.OUT_OF_RANGE },
        invalid = sensorList.count { it.readingStatus == ReadingStatus.INVALID },
        offline = sensorList.count { it.status != SensorStatus.ACTIVE }
    )

    /** Statistics of the valid readings of a sensor; null when it has none. */
    fun readingStats(sensorId: String?): ReadingStats? {
        val valid = readingsOf(sensorId).filter { it.status != ReadingStatus.INVALID }
        if (valid.isEmpty()) return null
        return ReadingStats(
            min = valid.minOf { it.value },
            max = valid.maxOf { it.value },
            average = valid.map { it.value }.average(),
            outOfRangeCount = valid.count { it.status == ReadingStatus.OUT_OF_RANGE },
            count = valid.size
        )
    }

    // ---------- Commands ----------

    /** Registers a reading, evaluates it against the threshold and updates the latest reading of the sensor. */
    fun registerReading(sensorId: String?, value: Double): Result<SensorReading> {
        val index = sensorList.indexOfFirst { it.id == sensorId }
        if (index < 0) return Result.failure(IllegalArgumentException("Select the sensor."))
        if (!value.isFinite()) return Result.failure(IllegalArgumentException("Enter a valid value."))

        val sensor = sensorList[index]
        if (sensor.status != SensorStatus.ACTIVE) {
            return Result.failure(IllegalStateException("Only active sensors can register readings."))
        }

        val threshold = sensor.threshold
        val base = SensorReading(
            id = "rd-${sensor.id.takeLast(3)}-%02d".format(readings.count { it.sensorId == sensor.id } + 1),
            sensorId = sensor.id,
            value = value,
            displayValue = sensor.type.formatValue(value, sensor.unit),
            unit = sensor.unit,
            recordedAt = nextTimestamp(),
            status = ReadingStatus.NORMAL
        )
        val reading = if (threshold.status == ThresholdStatus.ACTIVE && !threshold.validate(value)) {
            base.markAsOutOfRange()
        } else {
            base.markAsNormal()
        }

        readings.add(reading)
        sensorList[index] = sensor.copy(latestReading = reading)
        return Result.success(reading)
    }

    /** Completes the overdue (or else pending) calibration of a sensor and schedules the next one. */
    fun calibrate(sensorId: String?, performedBy: String?): Result<SensorCalibration> {
        if (findSensor(sensorId) == null) return Result.failure(IllegalArgumentException("Select the sensor."))
        if (performedBy.isNullOrBlank()) return Result.failure(IllegalArgumentException("Select who performed the calibration."))

        val index = calibrations.indexOfFirst { it.sensorId == sensorId && it.status == CalibrationStatus.OVERDUE }
            .takeIf { it >= 0 }
            ?: calibrations.indexOfFirst { it.sensorId == sensorId && it.status == CalibrationStatus.PENDING }
        if (index < 0) return Result.failure(IllegalStateException("This sensor has no pending calibration."))

        val now = nextTimestamp()
        val completed = calibrations[index]
            .copy(
                calibratedAt = now.substringBefore('T'),
                nextCalibrationAt = currentTime().plus(Duration.ofDays(CALIBRATION_INTERVAL_DAYS)).toString().substringBefore('T'),
                performedBy = performedBy.trim()
            )
            .markAsCompleted()
        calibrations[index] = completed
        return Result.success(completed)
    }

    fun setSensorStatus(sensorId: String?, status: SensorStatus): Boolean {
        val index = sensorList.indexOfFirst { it.id == sensorId }
        if (index < 0) return false
        val sensor = sensorList[index]
        sensorList[index] = when (status) {
            SensorStatus.ACTIVE -> sensor.activate()
            SensorStatus.INACTIVE -> sensor.deactivate()
            SensorStatus.DISCONNECTED -> sensor.markAsDisconnected()
            SensorStatus.MAINTENANCE -> sensor.copy(status = SensorStatus.MAINTENANCE)
        }
        return true
    }

    /** Moves the mock clock one minute forward and returns the new moment as an ISO string. */
    private fun nextTimestamp(): String {
        clockTicks += 1
        return currentTime().toString()
    }
}
