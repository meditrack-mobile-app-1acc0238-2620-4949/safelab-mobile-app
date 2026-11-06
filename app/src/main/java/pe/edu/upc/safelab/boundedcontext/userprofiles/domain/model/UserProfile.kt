package pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model

/** Personal information belongs to the profile, not the authentication session. */
data class UserProfile(
    val userId: String,
    val username: String,
    val firstNames: String,
    val lastNames: String,
    val email: String,
    val phone: String,
    val role: String,
    val age: String = "",
    val city: String = "",
    val jobTitle: String = ""
)

/** Fields that the user may change. Username and role remain read-only. */
data class ProfileChanges(
    val firstNames: String,
    val lastNames: String,
    val email: String,
    val phone: String,
    val age: String,
    val city: String,
    val jobTitle: String
)

enum class ProfileSaveResult {
    SUCCESS,
    INVALID_FIRST_NAMES,
    INVALID_LAST_NAMES,
    INVALID_EMAIL,
    EMAIL_IN_USE,
    INVALID_PHONE,
    INVALID_AGE,
    INVALID_CITY,
    INVALID_JOB_TITLE,
    NOT_FOUND
}
