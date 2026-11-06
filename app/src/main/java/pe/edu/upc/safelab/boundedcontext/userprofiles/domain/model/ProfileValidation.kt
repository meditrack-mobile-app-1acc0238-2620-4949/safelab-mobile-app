package pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model

/** Simple frontend rules; the API must validate all inputs again later. */
fun validateProfile(changes: ProfileChanges): ProfileSaveResult {
    // Unicode letters allow names with Spanish accents; digits and symbols are rejected.
    val names = Regex("^[\\p{L}]+(?: [\\p{L}]+)*$")
    val optionalText = Regex("^[\\p{L}]+(?: [\\p{L}]+)*$")
    val email = Regex("^[A-Za-z0-9+_.-]+@(?:gmail|hotmail|outlook|yahoo|icloud|protonmail|live)\\.com$", RegexOption.IGNORE_CASE)
    val ageValue = changes.age.toIntOrNull()
    return when {
        changes.firstNames.trim().length !in 2..70 || !names.matches(changes.firstNames.trim()) -> ProfileSaveResult.INVALID_FIRST_NAMES
        changes.lastNames.trim().length !in 2..70 || !names.matches(changes.lastNames.trim()) -> ProfileSaveResult.INVALID_LAST_NAMES
        !email.matches(changes.email.trim()) -> ProfileSaveResult.INVALID_EMAIL
        changes.phone.length !in 9..15 || !changes.phone.all(Char::isDigit) -> ProfileSaveResult.INVALID_PHONE
        changes.age.isNotBlank() && (ageValue == null || ageValue !in 1..120) -> ProfileSaveResult.INVALID_AGE
        changes.city.isNotBlank() && (changes.city.trim().length !in 2..60 || !optionalText.matches(changes.city.trim())) -> ProfileSaveResult.INVALID_CITY
        changes.jobTitle.isNotBlank() && (changes.jobTitle.trim().length !in 2..60 || !optionalText.matches(changes.jobTitle.trim())) -> ProfileSaveResult.INVALID_JOB_TITLE
        else -> ProfileSaveResult.SUCCESS
    }
}
