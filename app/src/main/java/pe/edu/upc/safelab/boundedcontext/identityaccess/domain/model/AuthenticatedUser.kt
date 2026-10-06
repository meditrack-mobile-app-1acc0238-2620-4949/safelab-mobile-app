package pe.edu.upc.safelab.boundedcontext.identityaccess.domain.model

data class AuthenticatedUser(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val role: String
)
