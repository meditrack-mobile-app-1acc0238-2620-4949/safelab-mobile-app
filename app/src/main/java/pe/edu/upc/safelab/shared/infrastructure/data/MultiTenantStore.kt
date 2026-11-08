package pe.edu.upc.safelab.shared.infrastructure.data

import android.content.Context
import pe.edu.upc.safelab.shared.infrastructure.http.SafeLabApi
import pe.edu.upc.safelab.shared.infrastructure.http.SafeLabHttpException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.model.AuthenticatedUser
import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.service.UserIdentifierGenerator
import java.util.UUID
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Same fictional records and stable IDs as the Vue frontend's server/db.json.
 * This class is deliberately small: when the API is available, replace its
 * JSON reads with HTTP repository methods, without changing the UI screens.
 * Data and passwords are demonstrative: this is NOT a production auth server.
 */
object MultiTenantStore {
    private lateinit var context: Context
    private lateinit var data: JSONObject
    private lateinit var permissions: JSONObject
    private lateinit var languages: Map<String, JSONObject>
    var version by mutableIntStateOf(0)
        private set
    private val refreshLock = Mutex()

    fun initialize(appContext: Context) {
        if (::data.isInitialized) return
        context = appContext.applicationContext
        fun asset(file: String) = context.assets.open(file).bufferedReader().use { it.readText() }
        val prefs = context.getSharedPreferences("safelab_multitenant", Context.MODE_PRIVATE)
        val packaged = JSONObject(asset("safelab_data.json"))
        // Persist only interactions made in this frontend; initial records come from the web seed.
        val saved = prefs.getString("demo_state_v1", null)
        data = try { if (saved != null) JSONObject(saved) else packaged } catch (_: Exception) { packaged }
        permissions = JSONObject(asset("safelab_permissions.json"))
        languages = mapOf("EN" to JSONObject(asset("strings_en.json")), "ES" to JSONObject(asset("strings_es.json")))
    }

    /** Sustituye el snapshot local por los datos que el backend autorizó. */
    suspend fun refreshFromBackend() {
        if (SafeLabApi.isMock || !SafeLabApi.authenticated) return
        refreshLock.withLock {
            if (!SafeLabApi.authenticated) return@withLock
            val snapshot = SafeLabApi.bootstrap()
            data = snapshot
            version++ // Invalida pantallas Compose de todos los BC.
        }
    }

    /** Crear de forma remota. Nunca confirma el cambio antes del HTTP 201. */
    suspend fun addRecordRemote(collection: String, actor: JSONObject, fields: JSONObject): Boolean {
        if (SafeLabApi.isMock) return addRecord(collection, actor, fields)
        val endpoint = when (collection) {
            "assets" -> "/facilityassets/assets"
            "sensors" -> "/sensormonitoring/sensors"
            "incidents" -> "/alertsincidents/incidents"
            "maintenanceRecords" -> "/equipmentmaintenance/records"
            "reports" -> "/reportinganalytics/reports"
            else -> return false
        }
        val body = JSONObject(fields.toString())
        // Los formularios móviles anteriores no pedían facilityId; la API sí lo exige.
        val assignedFacility = visibleItems("facilities", actor).firstOrNull()?.optString("id").orEmpty()
        if (collection in listOf("assets", "incidents", "sensors") &&
            body.optString("facilityId").isBlank()) {
            if (assignedFacility.isBlank()) throw IllegalStateException("No tienes una sede autorizada para registrar este recurso")
            body.put("facilityId", assignedFacility)
        }
        if (collection == "assets") {
            if (!body.has("category")) body.put("category", "Cold Storage")
            if (!body.has("storageUnit")) body.put("storageUnit", "Local")
            // El ID es la autoridad; el nombre solo sirve para compatibilidad con los mocks.
            if (!body.has("responsibleUserId") && actor.optString("role") != "organizationAdmin")
                body.put("responsibleUserId", actor.optString("id"))
            if (!body.has("responsible")) body.put("responsible",
                if (actor.optString("role") == "organizationAdmin") "" else actor.optString("fullName"))
        }
        if (collection == "sensors") {
            val asset = visibleItems("assets", actor).firstOrNull() ?:
                throw IllegalStateException("Se necesita un equipo autorizado para registrar un sensor")
            body.put("assetId", asset.optString("id"))
            body.put("facilityId", asset.optString("facilityId"))
            body.put("code", "SEN-" + UUID.randomUUID().toString().take(8).uppercase())
        }
        if (collection == "maintenanceRecords") {
            body.put("responsible", actor.optString("fullName"))
            body.put("status", body.optString("status", "scheduled"))
        }
        try {
            SafeLabApi.request("POST", endpoint, body)
            refreshFromBackend()
            return true
        } catch (ex: Exception) { throw ex }
    }

    /** Updates only the collections for which Spring Boot exposes a scoped PATCH. */
    suspend fun updateRecordRemote(collection: String, id: String, actor: JSONObject, fields: JSONObject): Boolean {
        val required = when (collection) {
            "assets" -> "assets.manage"
            "sensors" -> "sensors.configure"
            "maintenanceRecords" -> "maintenance.manage"
            else -> return false
        }
        if (!can(actor, required) || visibleItems(collection, actor).none { it.optString("id") == id })
            return false
        if (SafeLabApi.isMock) {
            val existing = find(collection, id) ?: return false
            for (key in fields.keys()) existing.put(key, fields.opt(key))
            changed(); return true
        }
        val endpoint = when (collection) {
            "assets" -> "/facilityassets/assets/$id"
            "sensors" -> "/sensormonitoring/sensors/$id"
            "maintenanceRecords" -> "/equipmentmaintenance/records/$id"
            else -> return false
        }
        val payload = JSONObject(fields.toString())
        // Immutable fields must never be submitted by the editor.
        payload.remove("id"); payload.remove("organizationId"); payload.remove("createdAt")
        if (collection == "assets" && payload.has("responsibleUserId") &&
            actor.optString("role") != "organizationAdmin") return false
        SafeLabApi.request("PATCH", endpoint, payload)
        refreshFromBackend()
        return true
    }

    /** Deletion is supported in the API for assets and sensors only. */
    suspend fun deleteRecordRemote(collection: String, id: String, actor: JSONObject): Boolean {
        val required = when (collection) {
            "assets" -> "assets.manage"
            "sensors" -> "sensors.configure"
            else -> return false
        }
        if (!can(actor, required) || visibleItems(collection, actor).none { it.optString("id") == id })
            return false
        if (SafeLabApi.isMock) return false // Mock UI does not pretend to delete remotely.
        val endpoint = if (collection == "assets") "/facilityassets/assets/$id" else "/sensormonitoring/sensors/$id"
        SafeLabApi.request("DELETE", endpoint)
        refreshFromBackend()
        return true
    }

    suspend fun updateSensorReadingRemote(id: String, actor: JSONObject, value: Double): Boolean {
        if (!can(actor, "sensors.configure") || visibleItems("sensors", actor).none { it.optString("id") == id })
            return false
        if (SafeLabApi.isMock) return false
        SafeLabApi.request("PATCH", "/sensormonitoring/sensors/$id/reading", JSONObject().put("value", value))
        refreshFromBackend()
        return true
    }

    /** Match Web operational actions using the same role checks and HTTP routes. */
    suspend fun performRecordActionRemote(collection: String, record: JSONObject,
                                          actor: JSONObject, action: String): Boolean {
        val id = record.optString("id")
        val org = actor.optString("organizationId")
        if (record.optString("organizationId") != org && actor.optString("role") != "platformAdmin")
            return false
        val permission: String
        val path: String
        val body = JSONObject()
        when (collection to action) {
            "alerts" to "acknowledge" -> {
                permission = "alerts.acknowledge"; path = "/alertsincidents/alerts/$id/acknowledge"
                body.put("notes", "Acknowledged from SafeLab Mobile")
            }
            "alerts" to "resolve" -> {
                permission = "incidents.resolve"; path = "/alertsincidents/alerts/$id/resolve"
                body.put("notes", "Resolved from SafeLab Mobile")
            }
            "alerts" to "escalate" -> {
                permission = "incidents.create"; path = "/alertsincidents/alerts/$id/escalate"
                body.put("notes", "Escalated from SafeLab Mobile")
            }
            "incidents" to "investigate", "incidents" to "resolve", "incidents" to "close" -> {
                permission = "incidents.resolve"; path = "/alertsincidents/incidents/$id/$action"
                body.put("notes", "Changed from SafeLab Mobile: $action")
            }
            "complianceRules" to "toggle" -> {
                permission = "compliance.manage"; path = "/environmentalcompliance/rules/$id"
                body.put("status", if (record.optString("status") == "active") "paused" else "active")
            }
            "notifications" to "mark-read" -> {
                permission = "incidents.view"; path = "/alertsincidents/notifications/$id/mark-read"
            }
            else -> return false
        }
        if (!can(actor, permission) || visibleItems(collection, actor).none { it.optString("id") == id })
            return false
        if (SafeLabApi.isMock) return false
        SafeLabApi.request(if (collection == "complianceRules") "PATCH" else "POST", path,
            if (body.length() == 0) null else body)
        refreshFromBackend()
        return true
    }

    suspend fun acknowledgeAlertRemote(id: String, actor: JSONObject): Boolean {
        if (SafeLabApi.isMock) {
            mutateRecord("alerts",id,"status","acknowledged",actor);return true
        }
        SafeLabApi.request("POST", "/alertsincidents/alerts/$id/acknowledge",
            JSONObject().put("notes", "Acknowledged from SafeLab Mobile"))
        refreshFromBackend();return true
    }

    suspend fun executeCommandRemote(id: String, actor: JSONObject, command: String = "START"): Boolean {
        val normalized = command.uppercase()
        if (normalized !in setOf("START", "STOP", "RESET") || !can(actor,"actuation.execute") ||
            visibleItems("actuators", actor).none { it.optString("id") == id }) return false
        if (SafeLabApi.isMock) return simulateCommand(actor,id,normalized)
        val apiCommand = normalized.lowercase().replaceFirstChar { it.uppercase() }
        SafeLabApi.request("POST", "/remoteactuation/actuators/$id/commands",
            JSONObject().put("command", apiCommand))
        refreshFromBackend();return true
    }

    suspend fun inviteRemote(manager: JSONObject, email: String, role: String): String? {
        if (SafeLabApi.isMock) return invite(manager, email, role)
        val org = manager.optString("organizationId")
        val body = JSONObject().put("email",email).put("role",role)
            .put("facilityIds",manager.optJSONArray("facilityIds") ?: JSONArray())
            .put("resourceIds",JSONArray())
        val created = SafeLabApi.request("POST", "/identityaccess/organizations/$org/invitations", body)
        refreshFromBackend()
        return created.optString("code")
    }

    suspend fun cancelInvitationRemote(actor: JSONObject, id: String) {
        if (SafeLabApi.isMock) { cancelInvitation(actor,id);return }
        SafeLabApi.request("POST", "/identityaccess/organizations/${actor.optString("organizationId")}/invitations/$id/cancel")
        refreshFromBackend()
    }

    suspend fun saveEmployeePermissionsRemote(actor: JSONObject, employee: JSONObject, draft: Map<String, Boolean>): Boolean {
        if (SafeLabApi.isMock) return saveEmployeePermissions(actor,employee,draft)
        val permissions = JSONArray()
        draft.filterValues { it }.keys.forEach { permissions.put(it) }
        SafeLabApi.request("PUT", "/identityaccess/organizations/${actor.optString("organizationId")}/users/${employee.optString("id")}/permissions",
            JSONObject().put("permissions", permissions))
        refreshFromBackend(); return true
    }

    suspend fun saveEmployeeResourcesRemote(actor: JSONObject, employee: JSONObject, resourceIds: List<String>): Boolean {
        if (SafeLabApi.isMock) return saveEmployeeResources(actor, employee, resourceIds)
        val facilities = employee.optJSONArray("facilityIds") ?: JSONArray()
        val resources = JSONArray(); resourceIds.forEach { resources.put(it) }
        SafeLabApi.request("PATCH", "/identityaccess/organizations/${actor.optString("organizationId")}/users/${employee.optString("id")}/assignments",
            JSONObject().put("facilityIds", facilities).put("resourceIds", resources))
        refreshFromBackend();return true
    }

    private fun changed() {
        if (!SafeLabApi.isMock) { version++; return } // En modo API no guardar writes locales como si fueran remotas.
        context.getSharedPreferences("safelab_multitenant", Context.MODE_PRIVATE)
            .edit().putString("demo_state_v1", data.toString()).apply()
        version++
    }

    fun resetDemonstration() {
        if (!SafeLabApi.isMock) return
        context.getSharedPreferences("safelab_multitenant", Context.MODE_PRIVATE)
            .edit().remove("demo_state_v1").apply()
        context.assets.open("safelab_data.json").bufferedReader().use { data = JSONObject(it.readText()) }
        version++
    }

    fun text(language: String, key: String): String {
        val dictionary = languages[language] ?: languages.getValue("EN")
        val part = dictionary.optJSONObject("multi")
        return part?.optString(key)?.takeIf { it.isNotBlank() } ?: key
    }

    fun roleName(role: String, lang: String) = text(lang, "role_$role").let { if (it.startsWith("role_")) role else it }

    fun items(collection: String): List<JSONObject> {
        val array = data.optJSONArray(collection) ?: return emptyList()
        return (0 until array.length()).mapNotNull { array.optJSONObject(it) }
    }

    fun find(collection: String, id: String): JSONObject? = items(collection).firstOrNull { it.optString("id") == id }
    fun user(id: String): JSONObject? = find("users", id)
    fun organization(id: String) = find("organizations", id)
    fun orgName(id: String) = organization(id)?.optString("name") ?: "SafeLab Platform"

    fun account(identifier: String, password: String): JSONObject? {
        val key = identifier.trim().lowercase()
        return items("users").firstOrNull { record ->
            (record.optString("email").lowercase() == key || record.optString("username") == key) &&
                record.optString("password") == password
        }
    }

    fun asAuthenticatedUser(item: JSONObject) = AuthenticatedUser(
        id = item.optString("id"), username = item.optString("username"),
        displayName = item.optString("fullName"), email = item.optString("email"),
        role = item.optString("role"), organizationId = item.optString("organizationId").takeIf { it != "null" }
    )

    private fun allowedRoles(role: String): Set<String> {
        val defaults = permissions.optJSONObject("defaults")?.optJSONArray(role)
        return arrayStrings(defaults).toSet()
    }

    fun grantable(role: String) = arrayStrings(permissions.optJSONObject("grantable")?.optJSONArray(role)).toSet()

    fun defaults(role: String): Set<String> = allowedRoles(role)

    fun allPermissionGroups(): List<Pair<String, List<String>>> =
        (permissions.optJSONArray("groups") ?: JSONArray()).let { groups ->
            (0 until groups.length()).map { index ->
                val g = groups.getJSONObject(index)
                g.optString("id") to arrayStrings(g.optJSONArray("permissions"))
            }
        }

    fun availableRoles(segment: String) = arrayStrings(
        permissions.optJSONObject("segmentRoles")?.optJSONArray(segment)
    )

    fun can(user: JSONObject, permission: String): Boolean {
        if (!SafeLabApi.isMock && user.has("effectivePermissions"))
            return arrayStrings(user.optJSONArray("effectivePermissions")).contains(permission)
        val role = user.optString("role")
        if (role == "platformAdmin") return true
        if (role == "organizationAdmin") {
            return permission == "billing.read" ||
                arrayStrings(permissions.optJSONArray("manager")).contains(permission)
        }
        if (permission !in grantable(role)) return false
        val stored = data.optJSONObject("permissionOverrides")?.optJSONObject(user.optString("id"))
        return stored?.let { if (it.has(permission)) it.optBoolean(permission) else null }
            ?: (permission in defaults(role))
    }

    fun employeePermissions(user: JSONObject): Map<String, Boolean> = allPermissionGroups()
        .flatMap { it.second }.associateWith { can(user, it) }

    fun saveEmployeePermissions(actor: JSONObject, employee: JSONObject, draft: Map<String, Boolean>): Boolean {
        if (!SafeLabApi.isMock) return false // Debe usar /identityaccess/.../permissions
        if (actor.optString("role") != "organizationAdmin") return false
        if (actor.optString("organizationId") != employee.optString("organizationId")) return false
        if (employee.optString("role") !in listOf("bioclinicalCoordinator", "labTechnician", "pharmaOperationsCoordinator", "pharmaTechnician", "complianceOfficer", "maintenanceOperator")) return false
        val values = JSONObject()
        for ((key, value) in draft) if (key in grantable(employee.optString("role"))) values.put(key, value)
        val overrides = data.optJSONObject("permissionOverrides") ?: JSONObject().also { data.put("permissionOverrides", it) }
        overrides.put(employee.optString("id"), values)
        appendAudit(actor, "Updated employee permissions: ${employee.optString("fullName")}")
        changed()
        return true
    }

    fun setting(key: String, default: String): String =
        data.optJSONObject("platformSettings")?.optString(key)?.takeIf { it.isNotBlank() } ?: default

    fun saveSettings(actor: JSONObject, name: String, email: String, timezone: String) {
        if (!SafeLabApi.isMock) return
        if (actor.optString("role") != "platformAdmin") return
        val settings = data.optJSONObject("platformSettings") ?: JSONObject().also { data.put("platformSettings", it) }
        settings.put("platformName", name); settings.put("supportEmail", email); settings.put("timezone", timezone)
        changed()
    }

    fun saveEmployeeResources(actor: JSONObject, employee: JSONObject, resourceIds: List<String>): Boolean {
        if (!SafeLabApi.isMock) return false // Debe usar /identityaccess/.../assignments
        if (actor.optString("role") != "organizationAdmin" ||
            actor.optString("organizationId") != employee.optString("organizationId")) return false
        val orgId = actor.optString("organizationId")
        val valid = (items("assets") + items("sensors"))
            .filter { it.optString("organizationId") == orgId }.map { it.optString("id") }.toSet()
        if (!resourceIds.all { it in valid }) return false
        employee.put("resourceIds", JSONArray(resourceIds))
        appendAudit(actor, "Updated assigned resources: ${employee.optString("fullName")}")
        changed()
        return true
    }

    fun employees(organizationId: String): List<JSONObject> = items("users").filter {
        it.optString("organizationId") == organizationId && it.optString("role") != "organizationAdmin"
    }

    fun usedSeats(organizationId: String): Int = employees(organizationId).count { it.optString("status") == "active" } +
        items("invitations").count { it.optString("organizationId") == organizationId && it.optString("status") == "pending" }

    fun seatCapacity(organizationId: String): Int = subscription(organizationId)?.let {
        it.optInt("includedSeats", 10) + it.optInt("additionalSeats", 0)
    } ?: 0

    fun subscription(organizationId: String): JSONObject? = items("subscriptions").firstOrNull {
        it.optString("organizationId") == organizationId
    }

    fun invite(manager: JSONObject, email: String, role: String): String? {
        if (!SafeLabApi.isMock) return null // Usar API /invitations
        val orgId = manager.optString("organizationId")
        if (manager.optString("role") != "organizationAdmin" ||
            role !in availableRoles(organization(orgId)?.optString("segment").orEmpty()) ||
            seatCapacity(orgId) <= usedSeats(orgId) ||
            !email.contains('@') ||
            items("users").any { it.optString("email").equals(email, true) } ||
            items("invitations").any { it.optString("email").equals(email, true) && it.optString("status") == "pending" }
        ) return null
        val code = UUID.randomUUID().toString().replace("-", "").uppercase()
        val expires = System.currentTimeMillis() + 72L * 60 * 60 * 1000
        data.getJSONArray("invitations").put(JSONObject().apply {
            put("id", UUID.randomUUID().toString()); put("code", code)
            put("email", email.trim().lowercase()); put("role", role)
            put("organizationId", orgId); put("createdBy", manager.optString("id"))
            put("status", "pending"); put("expiresAtMillis", expires)
        })
        appendAudit(manager, "Created invitation for ${email.trim().lowercase()}")
        changed()
        return code
    }

    fun invitation(code: String) = items("invitations").firstOrNull {
        it.optString("code").equals(code.trim(), true) && it.optString("status") == "pending" &&
            it.optLong("expiresAtMillis") > System.currentTimeMillis()
    }

    fun cancelInvitation(actor: JSONObject, id: String) {
        val found = find("invitations", id) ?: return
        if (actor.optString("role") != "organizationAdmin" || actor.optString("organizationId") != found.optString("organizationId")) return
        found.put("status", "cancelled")
        changed()
    }

    fun registerEmployee(code: String, firstNames: String, lastNames: String, phone: String, password: String): String? {
        val inv = invitation(code) ?: return null
        if (items("users").any { it.optString("email").equals(inv.optString("email"), true) }) return null
        val id = UUID.randomUUID().toString()
        val username = uniqueUsername()
        val orgId = inv.optString("organizationId")
        val org = organization(orgId) ?: return null
        data.getJSONArray("users").put(JSONObject().apply {
            put("id", id); put("username", username); put("email", inv.optString("email"))
            put("password", password); put("firstName", firstNames.trim()); put("lastName", lastNames.trim())
            put("fullName", "${firstNames.trim()} ${lastNames.trim()}"); put("phone", phone)
            put("role", inv.optString("role")); put("status", "pending")
            put("organizationId", orgId); put("organization", org.optString("name"))
            put("segment", org.optString("segment")); put("facilityId", "")
            put("facilityIds", org.optJSONArray("facilityIds") ?: JSONArray())
            put("resourceIds", JSONArray()); put("city", org.optString("city"))
        })
        inv.put("status", "accepted")
        changed()
        return username
    }

    fun registerCompany(legalName: String, ruc: String, segment: String, corporateEmail: String,
                        firstNames: String, lastNames: String, phone: String, email: String, password: String): String? {
        if (ruc.length != 11 || !ruc.all(Char::isDigit) || legalName.trim().length < 4 ||
            !corporateEmail.contains('@') || items("organizations").any { it.optString("ruc") == ruc } ||
            items("users").any { it.optString("email").equals(email, true) }) return null
        val username = uniqueUsername()
        val organizationId = "org-" + UUID.randomUUID().toString().take(8)
        val userId = UUID.randomUUID().toString()
        data.getJSONArray("organizations").put(JSONObject().apply {
            put("id", organizationId); put("name", legalName.trim()); put("ruc", ruc)
            put("segment", segment); put("corporateEmail", corporateEmail.trim().lowercase())
            put("city", ""); put("status", "pending"); put("managerId", userId)
            put("facilityIds", JSONArray())
        })
        data.getJSONArray("users").put(JSONObject().apply {
            put("id", userId); put("username", username); put("firstName", firstNames.trim())
            put("lastName", lastNames.trim()); put("fullName", "${firstNames.trim()} ${lastNames.trim()}")
            put("email", email.trim().lowercase()); put("password", password); put("phone", phone)
            put("role", "organizationAdmin"); put("status", "pending")
            put("organizationId", organizationId); put("organization", legalName.trim())
            put("segment", segment); put("facilityId", ""); put("facilityIds", JSONArray())
        })
        data.getJSONArray("accountRequests").put(JSONObject().apply {
            put("organizationId", organizationId); put("userId", userId)
            put("status", "pendingVerificationAndPayment"); put("corporateEmail", corporateEmail)
        })
        changed()
        return username
    }

    private fun uniqueUsername(): String {
        var value: String
        do { value = UserIdentifierGenerator.generateUsername() }
        while (items("users").any { it.optString("username") == value })
        return value
    }

    /** Applies organization + facility + assigned resources, even if a URL is guessed. */
    fun visibleItems(collection: String, actor: JSONObject, overrideOrganization: String? = null): List<JSONObject> {
        val role = actor.optString("role")
        val orgId = if (role == "platformAdmin") overrideOrganization else actor.optString("organizationId")
        val records = items(collection).filter { orgId.isNullOrBlank() || it.optString("organizationId") == orgId }
        // In API mode bootstrap has already applied the server's RBAC and resource scope.
        // Re-filtering with stale/offline mock rules causes different counts in Web vs Android.
        if (!SafeLabApi.isMock) return records
        if (role == "platformAdmin" || role == "organizationAdmin") return records
        val allowedFacilities = arrayStrings(actor.optJSONArray("facilityIds")).toSet()
        val resourceIds = arrayStrings(actor.optJSONArray("resourceIds")).toSet()
        val resourceScoped = role in listOf("labTechnician", "pharmaTechnician", "maintenanceOperator")
        return records.filter { item ->
            val id = item.optString("id")
            val sensorId = when (collection) {
                "complianceViolations" -> find("complianceEvaluations", item.optString("evaluationId"))?.optString("sensorId").orEmpty()
                else -> item.optString("sensorId", item.optString("relatedSensorId"))
            }
            val assetId = when (collection) {
                "maintenanceRecords" -> item.optString("equipmentId")
                "remoteCommands" -> find("actuators", item.optString("actuatorId"))?.optString("assetId").orEmpty()
                else -> item.optString("assetId", item.optString("relatedAssetId"))
            }
            val sensor = find("sensors", sensorId)
            val asset = find("assets", assetId)
            val actuator = find("actuators", item.optString("actuatorId"))
            val facilityId = item.optString("facilityId").ifBlank {
                sensor?.optString("facilityId") ?: asset?.optString("facilityId")
                ?: actuator?.optString("facilityId") ?: ""
            }
            val isResource = collection in listOf("assets", "sensors", "actuators", "alerts", "incidents",
                "complianceRules", "complianceEvaluations", "complianceViolations", "maintenanceRecords", "remoteCommands")
            when {
                collection == "facilities" -> id in allowedFacilities
                !isResource -> true  // Reports and audit are tenant-scoped and require their own permission.
                resourceIds.isEmpty() && resourceScoped -> false
                resourceIds.isEmpty() -> facilityId in allowedFacilities
                else -> id in resourceIds || sensorId in resourceIds || assetId in resourceIds ||
                    sensor?.optString("assetId").orEmpty() in resourceIds || actuator?.optString("assetId").orEmpty() in resourceIds
            }
        }
    }

    /** All mock writes are company-scoped and require the matching permission. */
    fun addRecord(collection: String, actor: JSONObject, fields: JSONObject): Boolean {
        if (!SafeLabApi.isMock) return false // Usar addRecordRemote() desde corutina.
        val permission = when (collection) {
            "assets" -> "assets.manage"
            "sensors" -> "sensors.configure"
            "maintenanceRecords" -> "maintenance.manage"
            "incidents" -> "incidents.create"
            "reports" -> "reports.export"
            else -> return false
        }
        if (!can(actor, permission)) return false
        val orgId = actor.optString("organizationId")
        if (orgId.isBlank() || orgId == "null") return false
        val facilityId = fields.optString("facilityId")
        if (facilityId.isNotBlank() && organization(orgId)?.optJSONArray("facilityIds")
                ?.let { arrayStrings(it).contains(facilityId) } != true) return false
        fields.put("id", "${collection.take(3)}-" + UUID.randomUUID().toString().take(8))
        fields.put("organizationId", orgId)
        data.getJSONArray(collection).put(fields)
        appendAudit(actor, "Created $collection / ${fields.optString("id")}")
        changed()
        return true
    }

    fun simulateCommand(actor: JSONObject, actuatorId: String, command: String): Boolean {
        if (!SafeLabApi.isMock) return false
        if (!can(actor, "actuation.execute")) return false
        val target = visibleItems("actuators", actor).firstOrNull { it.optString("id") == actuatorId } ?: return false
        if (target.optString("status") != "online" || target.optString("safetyState") != "enabled") return false
        target.put("lastCommand", command)
        data.getJSONArray("remoteCommands").put(JSONObject().apply {
            put("id", "CMD-" + UUID.randomUUID().toString().take(8).uppercase())
            put("actuatorId", actuatorId); put("command", command)
            put("device", target.optString("name")); put("status", "confirmed-sample")
            put("requestedBy", actor.optString("fullName"))
            put("createdAt", java.time.Instant.now().toString())
            put("organizationId", actor.optString("organizationId"))
        })
        appendAudit(actor, "Remote $command / $actuatorId")
        changed()
        return true
    }

    fun updateUser(id: String, first: String, last: String, phone: String, email: String) {
        if (!SafeLabApi.isMock) return
        val target = user(id) ?: return
        target.put("firstName", first); target.put("lastName", last)
        target.put("fullName", "$first $last"); target.put("phone", phone)
        target.put("email", email.trim().lowercase())
        changed()
    }

    private fun appendAudit(actor: JSONObject, action: String) {
        data.getJSONArray("auditLogs").put(JSONObject().apply {
            put("id", "aud-${UUID.randomUUID().toString().take(8)}")
            put("actor", actor.optString("fullName")); put("action", action)
            put("module", "Identity & Access"); put("severity", "info")
            put("status", "pending"); put("createdAt", java.time.Instant.now().toString())
            put("organizationId", actor.optString("organizationId"))
        })
    }

    fun mutateRecord(collection: String, id: String, key: String, newValue: Any, actor: JSONObject) {
        if (!SafeLabApi.isMock) return
        val record = find(collection, id) ?: return
        if (!visibleItems(collection, actor).any { it.optString("id") == id }) return
        record.put(key, newValue)
        appendAudit(actor, "Updated $collection / $id")
        changed()
    }

    fun arrayStrings(array: JSONArray?): List<String> = if (array == null) emptyList()
        else (0 until array.length()).map { array.optString(it) }
}
