package pe.edu.upc.safelab.shared.infrastructure.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.model.AuthenticatedUser
import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.service.UserIdentifierGenerator
import java.util.UUID

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

    private fun changed() {
        context.getSharedPreferences("safelab_multitenant", Context.MODE_PRIVATE)
            .edit().putString("demo_state_v1", data.toString()).apply()
        version++
    }

    fun resetDemonstration() {
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
        if (actor.optString("role") != "platformAdmin") return
        val settings = data.optJSONObject("platformSettings") ?: JSONObject().also { data.put("platformSettings", it) }
        settings.put("platformName", name); settings.put("supportEmail", email); settings.put("timezone", timezone)
        changed()
    }

    fun saveEmployeeResources(actor: JSONObject, employee: JSONObject, resourceIds: List<String>): Boolean {
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
        val record = find(collection, id) ?: return
        if (!visibleItems(collection, actor).any { it.optString("id") == id }) return
        record.put(key, newValue)
        appendAudit(actor, "Updated $collection / $id")
        changed()
    }

    fun arrayStrings(array: JSONArray?): List<String> = if (array == null) emptyList()
        else (0 until array.length()).map { array.optString(it) }
}
