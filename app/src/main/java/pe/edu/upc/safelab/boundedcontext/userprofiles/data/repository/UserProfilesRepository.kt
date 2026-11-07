package pe.edu.upc.safelab.boundedcontext.userprofiles.data.repository

import androidx.compose.runtime.mutableStateMapOf
import pe.edu.upc.safelab.shared.infrastructure.data.MultiTenantStore
import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.model.AuthenticatedUser
import pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model.ProfileChanges
import pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model.ProfileSaveResult
import pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model.UserProfile
import pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model.validateProfile

/**
 * Frontend-only store: changes survive navigation and logout while this app process runs.
 * It deliberately stores NO passwords, payment data or access tokens.
 * Later, API calls will replace this in-memory repository.
 */
object UserProfilesRepository {
    private val profiles = mutableStateMapOf<String, UserProfile>()

    fun current(userId: String): UserProfile? = profiles[userId]

    // Called when an existing account signs in. Provides reasonable administrator defaults.
    fun ensureFor(user: AuthenticatedUser): UserProfile {
        val source = MultiTenantStore.user(user.id)
        return profiles[user.id] ?: UserProfile(
            userId = user.id,
            username = user.username,
            firstNames = source?.optString("firstName") ?: user.displayName.substringBefore(' '),
            lastNames = source?.optString("lastName") ?: user.displayName.substringAfter(' ', "User"),
            email = user.email,
            phone = source?.optString("phone") ?: "",
            city = source?.optString("city") ?: "",
            jobTitle = source?.optString("jobTitle", user.role) ?: user.role,
            role = user.role
        ).also { profiles[user.id] = it }
    }

    // Registered data is kept pending. Registration does not grant app access.
    fun addPending(
        userId: String,
        username: String,
        firstNames: String,
        lastNames: String,
        email: String,
        phone: String
    ) {
        profiles[userId] = UserProfile(
            userId = userId,
            username = username,
            firstNames = firstNames.trim(),
            lastNames = lastNames.trim(),
            email = email.trim().lowercase(),
            phone = phone,
            role = "OPERATOR"
        )
    }

    fun save(
        userId: String,
        changes: ProfileChanges,
        emailAvailable: (String) -> Boolean
    ): ProfileSaveResult {
        val current = profiles[userId] ?: return ProfileSaveResult.NOT_FOUND
        val validation = validateProfile(changes)
        if (validation != ProfileSaveResult.SUCCESS) return validation
        val normalizedEmail = changes.email.trim().lowercase()
        if (!emailAvailable(normalizedEmail)) return ProfileSaveResult.EMAIL_IN_USE
        profiles[userId] = current.copy(
            firstNames = changes.firstNames.trim(),
            lastNames = changes.lastNames.trim(),
            email = normalizedEmail,
            phone = changes.phone.trim(),
            age = changes.age.trim(),
            city = changes.city.trim(),
            jobTitle = changes.jobTitle.trim()
        )
        MultiTenantStore.updateUser(userId, changes.firstNames.trim(), changes.lastNames.trim(), changes.phone.trim(), normalizedEmail)
        return ProfileSaveResult.SUCCESS
    }
}
