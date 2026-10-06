package pe.edu.upc.safelab.boundedcontext.identityaccess.data.local

import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.model.AuthenticatedUser

object IdentityAccessMockData {
    const val ADMIN_USERNAME = "admin"
    const val ADMIN_EMAIL = "admin@safelab.com"
    const val ADMIN_PASSWORD = "SafeLab2026"

    val administrator = AuthenticatedUser(
        id = "usr-admin-001",
        username = ADMIN_USERNAME,
        displayName = "SafeLab Administrator",
        email = ADMIN_EMAIL,
        role = "ADMINISTRATOR"
    )
}
