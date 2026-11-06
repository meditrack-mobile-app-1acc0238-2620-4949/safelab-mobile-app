package pe.edu.upc.safelab.boundedcontext.identityaccess.data.local

import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.model.AuthenticatedUser
import pe.edu.upc.safelab.common.DemoAccountIds

object IdentityAccessMockData {
    const val ADMIN_USERNAME = "691339768298838915"
    const val ADMIN_EMAIL = "admin@hotmail.com"
    const val ADMIN_PASSWORD = "SafeLab2026%"

    val administrator = AuthenticatedUser(
        id = DemoAccountIds.ADMIN_ID,
        username = ADMIN_USERNAME,
        displayName = "SafeLab Administrator",
        email = ADMIN_EMAIL,
        role = "ADMINISTRATOR"
    )
}