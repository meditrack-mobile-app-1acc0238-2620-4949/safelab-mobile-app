package pe.edu.upc.safelab.boundedcontext.sensormonitoring.data.local

import java.time.Duration
import java.time.Instant
import kotlin.math.roundToInt
import kotlin.math.sin
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.BatchStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.CalibrationStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.ReadingStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.Sensor
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorCalibration
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorReading
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorStatus
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.SensorType
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.TelemetryBatch
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.ThresholdRule
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.domain.model.ThresholdStatus

/**
 * Static TB1 data aligned with the SafeLab Platform API seed (facilities, assets and sensors).
 * Sensor, facility and asset ids are shared with the other bounded contexts.
 * Readings, calibrations and batches are generated without randomness so the app always looks the same.
 */
object SensorMonitoringMockData {

    /** Reference clock of the mock data: the moment at which the app "is". */
    const val NOW = "2026-06-19T04:30:00Z"

    private const val READINGS_PER_SENSOR = 24
    private const val READING_INTERVAL_MINUTES = 30L
    private const val RAMP_READINGS = 4
    private const val BATCHES_PER_SENSOR = 4
    private const val READINGS_PER_BATCH = 4

    private val facilityNames = mapOf(
        "fac-central" to "Central Clinical Laboratory",
        "fac-north" to "North Province Clinical Lab",
        "fac-hospital" to "San Gabriel Hospital Pharmacy",
        "fac-pharma" to "PharmAndina Cold Chain"
    )

    /**
     * Definition of a sensor and of how its history is generated.
     * [baseline] is the normal value of a sensor that ends out of range, [invalidTail] the number of
     * latest readings that are invalid and [openings] the reading positions in which a door is open.
     */
    private data class SensorSeed(
        val id: String,
        val code: String,
        val name: String,
        val type: SensorType,
        val status: SensorStatus,
        val facilityId: String,
        val assetId: String,
        val assetName: String,
        val current: Double,
        val unit: String,
        val min: Double,
        val max: Double,
        val lastReadingAt: String,
        val responsible: String,
        val installedAt: String,
        val amplitude: Double = 0.0,
        val baseline: Double? = null,
        val invalidTail: Int = 0,
        val openings: Set<Int> = emptySet()
    )

    private val seeds = listOf(
        SensorSeed(
            id = "sen-001", code = "SEN-CLN-001", name = "Reagent Freezer Temperature",
            type = SensorType.TEMPERATURE, status = SensorStatus.ACTIVE,
            facilityId = "fac-central", assetId = "asset-001", assetName = "Reagent Freezer A",
            current = 3.8, unit = "°C", min = 2.0, max = 8.0,
            lastReadingAt = "2026-06-19T02:25:00Z", responsible = "Carlos Mendoza",
            installedAt = "2025-03-14", amplitude = 0.6
        ),
        SensorSeed(
            id = "sen-002", code = "SEN-CLN-002", name = "Hematology Room Humidity",
            type = SensorType.HUMIDITY, status = SensorStatus.ACTIVE,
            facilityId = "fac-central", assetId = "asset-003", assetName = "Hematology Analyzer Reagents",
            current = 46.0, unit = "%", min = 35.0, max = 60.0,
            lastReadingAt = "2026-06-19T02:24:00Z", responsible = "Andrea Torres",
            installedAt = "2025-03-14", amplitude = 3.0
        ),
        SensorSeed(
            id = "sen-003", code = "SEN-CLN-003", name = "PCR Freezer Temperature",
            type = SensorType.TEMPERATURE, status = SensorStatus.ACTIVE,
            facilityId = "fac-central", assetId = "asset-002", assetName = "PCR Reagent Rack",
            current = 9.1, unit = "°C", min = 2.0, max = 8.0,
            lastReadingAt = "2026-06-19T03:10:00Z", responsible = "Carlos Mendoza",
            installedAt = "2025-04-02", amplitude = 0.4, baseline = 6.5
        ),
        SensorSeed(
            id = "sen-004", code = "SEN-CLN-004", name = "Sample Storage Door",
            type = SensorType.DOOR_STATUS, status = SensorStatus.ACTIVE,
            facilityId = "fac-central", assetId = "asset-004", assetName = "Sample Storage Door",
            current = 0.0, unit = "", min = 0.0, max = 1.0,
            lastReadingAt = "2026-06-19T01:24:00Z", responsible = "Andrea Torres",
            installedAt = "2025-04-02", openings = setOf(6, 15)
        ),
        SensorSeed(
            id = "sen-005", code = "SEN-NPL-001", name = "North Lab Reagent Refrigerator",
            type = SensorType.TEMPERATURE, status = SensorStatus.ACTIVE,
            facilityId = "fac-north", assetId = "asset-005", assetName = "North Lab Refrigerator",
            current = 4.1, unit = "°C", min = 2.0, max = 8.0,
            lastReadingAt = "2026-06-19T02:10:00Z", responsible = "Patricia Rivas",
            installedAt = "2025-05-20", amplitude = 0.5
        ),
        SensorSeed(
            id = "sen-006", code = "SEN-NPL-002", name = "North Incubator Vibration",
            type = SensorType.VIBRATION, status = SensorStatus.ACTIVE,
            facilityId = "fac-north", assetId = "asset-006", assetName = "North Incubator",
            current = 4.8, unit = "mm/s", min = 0.0, max = 3.0,
            lastReadingAt = "2026-06-19T02:12:00Z", responsible = "Patricia Rivas",
            installedAt = "2025-05-20", amplitude = 0.3, baseline = 2.0
        ),
        SensorSeed(
            id = "sen-007", code = "SEN-PHM-001", name = "Vaccine Fridge Temperature",
            type = SensorType.TEMPERATURE, status = SensorStatus.ACTIVE,
            facilityId = "fac-hospital", assetId = "asset-007", assetName = "Vaccine Fridge",
            current = 5.1, unit = "°C", min = 2.0, max = 8.0,
            lastReadingAt = "2026-06-19T02:05:00Z", responsible = "Rosa Fernandez",
            installedAt = "2025-06-09", amplitude = 0.6
        ),
        SensorSeed(
            id = "sen-008", code = "SEN-PHM-002", name = "Insulin Storage Humidity",
            type = SensorType.HUMIDITY, status = SensorStatus.ACTIVE,
            facilityId = "fac-hospital", assetId = "asset-008", assetName = "Insulin Storage",
            current = 64.0, unit = "%", min = 35.0, max = 60.0,
            lastReadingAt = "2026-06-19T04:00:00Z", responsible = "Rosa Fernandez",
            installedAt = "2025-06-09", amplitude = 2.0, baseline = 55.0
        ),
        SensorSeed(
            id = "sen-009", code = "SEN-PHM-003", name = "Controlled Drugs Cabinet Door",
            type = SensorType.DOOR_STATUS, status = SensorStatus.DISCONNECTED,
            facilityId = "fac-hospital", assetId = "asset-009", assetName = "Controlled Drugs Cabinet",
            current = 1.0, unit = "", min = 0.0, max = 1.0,
            lastReadingAt = "2026-06-19T00:29:00Z", responsible = "Gabriel Paredes",
            installedAt = "2025-07-15", invalidTail = 3, openings = setOf(5, 14, 21, 22)
        ),
        SensorSeed(
            id = "sen-010", code = "SEN-PHA-001", name = "PharmAndina Cold Room",
            type = SensorType.TEMPERATURE, status = SensorStatus.ACTIVE,
            facilityId = "fac-pharma", assetId = "asset-010", assetName = "PharmAndina Cold Room",
            current = 4.5, unit = "°C", min = 2.0, max = 8.0,
            lastReadingAt = "2026-06-19T02:20:00Z", responsible = "Valeria Rojas",
            installedAt = "2025-08-01", amplitude = 0.5
        ),
        SensorSeed(
            id = "sen-011", code = "SEN-NPL-003", name = "North Lab Refrigerator Door",
            type = SensorType.DOOR_STATUS, status = SensorStatus.DISCONNECTED,
            facilityId = "fac-north", assetId = "asset-005", assetName = "North Lab Refrigerator",
            current = 0.0, unit = "", min = 0.0, max = 1.0,
            lastReadingAt = "2026-06-18T22:40:00Z", responsible = "Patricia Rivas",
            installedAt = "2025-09-10", openings = setOf(9)
        ),
        SensorSeed(
            id = "sen-012", code = "SEN-PHA-002", name = "Cold Room Energy Meter",
            type = SensorType.ENERGY, status = SensorStatus.MAINTENANCE,
            facilityId = "fac-pharma", assetId = "asset-010", assetName = "PharmAndina Cold Room",
            current = 2.3, unit = "kW", min = 0.0, max = 5.0,
            lastReadingAt = "2026-06-18T20:15:00Z", responsible = "Valeria Rojas",
            installedAt = "2025-09-10", amplitude = 0.3
        )
    )

    /** Status of the latest batches of a sensor, newest first. The rest are processed. */
    private val batchStatuses = mapOf(
        "sen-002" to listOf(BatchStatus.RECEIVED),
        "sen-003" to listOf(BatchStatus.RECEIVED),
        "sen-008" to listOf(BatchStatus.RECEIVED),
        "sen-009" to listOf(BatchStatus.REJECTED, BatchStatus.REJECTED)
    )

    val readings: List<SensorReading> = seeds.flatMap { buildReadings(it) }

    val sensors: List<Sensor> = seeds.map { seed ->
        Sensor(
            id = seed.id,
            code = seed.code,
            name = seed.name,
            type = seed.type,
            status = seed.status,
            facilityId = seed.facilityId,
            assetId = seed.assetId,
            assetName = seed.assetName,
            facilityName = facilityNames.getValue(seed.facilityId),
            unit = seed.unit,
            responsible = seed.responsible,
            installedAt = seed.installedAt,
            threshold = ThresholdRule(
                id = "thr-${suffix(seed.id)}",
                minValue = seed.min,
                maxValue = seed.max,
                unit = seed.unit,
                status = ThresholdStatus.ACTIVE
            ),
            latestReading = readings.last { it.sensorId == seed.id }
        )
    }

    val calibrations: List<SensorCalibration> = listOf(
        done("cal-001-1", "sen-001", "2026-03-10", "2026-09-10", "Carlos Mendoza"),
        done("cal-002-1", "sen-002", "2026-04-02", "2026-10-02", "Andrea Torres"),
        done("cal-003-1", "sen-003", "2025-12-15", "2026-06-15", "Carlos Mendoza"),
        open("cal-003-2", "sen-003", "2026-06-15", CalibrationStatus.OVERDUE),
        done("cal-004-1", "sen-004", "2026-01-12", "2026-07-01", "Andrea Torres"),
        open("cal-004-2", "sen-004", "2026-07-01", CalibrationStatus.PENDING),
        done("cal-005-1", "sen-005", "2026-02-20", "2026-08-20", "Patricia Rivas"),
        done("cal-006-1", "sen-006", "2025-12-10", "2026-06-10", "Patricia Rivas"),
        open("cal-006-2", "sen-006", "2026-06-10", CalibrationStatus.OVERDUE),
        done("cal-007-1", "sen-007", "2026-05-05", "2026-11-05", "Rosa Fernandez"),
        done("cal-008-1", "sen-008", "2025-12-25", "2026-06-25", "Rosa Fernandez"),
        open("cal-008-2", "sen-008", "2026-06-25", CalibrationStatus.PENDING),
        open("cal-009-1", "sen-009", "2026-06-30", CalibrationStatus.PENDING),
        done("cal-010-1", "sen-010", "2026-05-28", "2026-11-28", "Valeria Rojas"),
        done("cal-011-1", "sen-011", "2026-01-15", "2026-07-15", "Patricia Rivas"),
        open("cal-012-1", "sen-012", "2026-07-10", CalibrationStatus.PENDING)
    )

    val batches: List<TelemetryBatch> = seeds.flatMap { buildBatches(it) }

    // ---------- Generation ----------

    private fun suffix(sensorId: String): String = sensorId.takeLast(3)

    private fun done(id: String, sensorId: String, calibratedAt: String, next: String, by: String) =
        SensorCalibration(id, sensorId, calibratedAt, next, by, CalibrationStatus.COMPLETED)

    private fun open(id: String, sensorId: String, next: String, status: CalibrationStatus) =
        SensorCalibration(id, sensorId, null, next, null, status)

    private fun buildReadings(seed: SensorSeed): List<SensorReading> {
        val threshold = ThresholdRule("thr-${suffix(seed.id)}", seed.min, seed.max, seed.unit, ThresholdStatus.ACTIVE)
        val last = READINGS_PER_SENSOR - 1
        val lastAt = Instant.parse(seed.lastReadingAt)

        return (0 until READINGS_PER_SENSOR).map { index ->
            val value = valueAt(seed, index)
            val status = when {
                index > last - seed.invalidTail -> ReadingStatus.INVALID
                threshold.validate(value) -> ReadingStatus.NORMAL
                else -> ReadingStatus.OUT_OF_RANGE
            }
            SensorReading(
                id = "rd-${suffix(seed.id)}-%02d".format(index + 1),
                sensorId = seed.id,
                value = value,
                displayValue = seed.type.formatValue(value, seed.unit),
                unit = seed.unit,
                recordedAt = lastAt.minus(Duration.ofMinutes(READING_INTERVAL_MINUTES * (last - index))).toString(),
                status = status
            )
        }
    }

    /** Oldest reading is index 0 and the latest is the last one, always equal to the current value. */
    private fun valueAt(seed: SensorSeed, index: Int): Double {
        val last = READINGS_PER_SENSOR - 1
        if (index == last) return seed.current
        if (seed.type == SensorType.DOOR_STATUS) return if (index in seed.openings) 1.0 else 0.0

        val baseline = seed.baseline
        val raw = if (baseline != null && index > last - RAMP_READINGS) {
            val progress = (index - (last - RAMP_READINGS)).toDouble() / RAMP_READINGS
            baseline + (seed.current - baseline) * progress
        } else {
            (baseline ?: seed.current) + sin(index * 0.9 + suffix(seed.id).toInt()) * seed.amplitude
        }
        return if (seed.type == SensorType.HUMIDITY) raw.roundToInt().toDouble() else (raw * 10).roundToInt() / 10.0
    }

    private fun buildBatches(seed: SensorSeed): List<TelemetryBatch> {
        val overrides = batchStatuses[seed.id].orEmpty()
        val lastAt = Instant.parse(seed.lastReadingAt)
        return (0 until BATCHES_PER_SENSOR).map { n ->
            TelemetryBatch(
                id = "bt-${suffix(seed.id)}-${n + 1}",
                sensorId = seed.id,
                receivedAt = lastAt.minus(Duration.ofMinutes(READING_INTERVAL_MINUTES * READINGS_PER_BATCH * n)).toString(),
                readingCount = READINGS_PER_BATCH,
                status = overrides.getOrElse(n) { BatchStatus.PROCESSED }
            )
        }
    }
}
