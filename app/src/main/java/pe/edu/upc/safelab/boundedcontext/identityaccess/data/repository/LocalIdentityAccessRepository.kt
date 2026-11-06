package pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository

import pe.edu.upc.safelab.boundedcontext.identityaccess.data.local.IdentityAccessMockData
import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.model.AuthenticatedUser
import pe.edu.upc.safelab.common.buildDisplayName
import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.service.UserIdentifierGenerator

sealed class AuthenticationResult {
    data class Success(val user: AuthenticatedUser) : AuthenticationResult()
    data object PendingActivation : AuthenticationResult()
    data object InvalidCredentials : AuthenticationResult()
}

data class RegistrationRequest(
    val firstNames: String,
    val lastNames: String,
    val phone: String,
    val email: String,
    val password: String
)

data class RegistrationResult(
    val successful: Boolean,
    val message: String,
    val generatedUsername: String? = null,
    val userId: String? = null
)

private data class LocalAccount(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String,
    val active: Boolean
)

class LocalIdentityAccessRepository {
    companion object {
        private val accounts = mutableListOf(
            LocalAccount(
                id = IdentityAccessMockData.administrator.id,
                username = IdentityAccessMockData.ADMIN_USERNAME,
                displayName = IdentityAccessMockData.administrator.displayName,
                email = IdentityAccessMockData.ADMIN_EMAIL,
                phone = "999999999",
                password = IdentityAccessMockData.ADMIN_PASSWORD,
                role = IdentityAccessMockData.administrator.role,
                active = true
            )
        )
    }

    fun authenticate(identifier: String, password: String): AuthenticationResult {
        val normalizedIdentifier = identifier.trim().lowercase()
        val account = accounts.firstOrNull {
            it.username.lowercase() == normalizedIdentifier || it.email.lowercase() == normalizedIdentifier
        } ?: return AuthenticationResult.InvalidCredentials

        if (account.password != password) return AuthenticationResult.InvalidCredentials
        if (!account.active) return AuthenticationResult.PendingActivation

        return AuthenticationResult.Success(
            AuthenticatedUser(
                id = account.id,
                username = account.username,
                displayName = account.displayName,
                email = account.email,
                role = account.role
            )
        )
    }

    fun register(request: RegistrationRequest): RegistrationResult {
        val email = request.email.trim().lowercase()
        if (accounts.any { it.email.lowercase() == email }) {
            return RegistrationResult(false, "A user with this email already exists.")
        }

        // Names do not determine the username: two people may have identical names.
        // Protect against the (extremely unlikely) in-memory collision as well.
        var username: String
        do {
            username = UserIdentifierGenerator.generateUsername()
        } while (accounts.any { it.username == username })

        var accountId: String
        do {
            accountId = UserIdentifierGenerator.generateUserId()
        } while (accounts.any { it.id == accountId })
        accounts += LocalAccount(
            id = accountId,
            username = username,
            displayName = buildDisplayName(request.firstNames, request.lastNames),
            email = email,
            phone = request.phone.trim(),
            password = request.password,
            role = "OPERATOR",
            active = false
        )

        return RegistrationResult(
            successful = true,
            message = "Your account was created and is pending verification.",
            generatedUsername = username,
            userId = accountId
        )
    }

    fun isEmailAvailable(userId: String, email: String): Boolean =
        accounts.none { it.id != userId && it.email.equals(email.trim(), ignoreCase = true) }

    fun updateEmail(userId: String, email: String) {
        val index = accounts.indexOfFirst { it.id == userId }
        if (index >= 0) accounts[index] = accounts[index].copy(email = email.trim().lowercase())
    }

}