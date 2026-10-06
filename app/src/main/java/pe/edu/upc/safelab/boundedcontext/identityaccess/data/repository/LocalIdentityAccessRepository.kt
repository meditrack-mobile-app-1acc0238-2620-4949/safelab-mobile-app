package pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository

import pe.edu.upc.safelab.boundedcontext.identityaccess.data.local.IdentityAccessMockData
import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.model.AuthenticatedUser

class LocalIdentityAccessRepository {
    fun authenticate(identifier: String, password: String): AuthenticatedUser? {
        val normalizedIdentifier = identifier.trim().lowercase()
        val validIdentifier = normalizedIdentifier == IdentityAccessMockData.ADMIN_USERNAME ||
            normalizedIdentifier == IdentityAccessMockData.ADMIN_EMAIL
        val validPassword = password == IdentityAccessMockData.ADMIN_PASSWORD

        return if (validIdentifier && validPassword) {
            IdentityAccessMockData.administrator
        } else {
            null
        }
    }
}
