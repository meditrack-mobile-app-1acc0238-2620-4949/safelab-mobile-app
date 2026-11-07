package pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository

import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.model.AuthenticatedUser
import pe.edu.upc.safelab.shared.infrastructure.data.MultiTenantStore

sealed class AuthenticationResult {
    data class Success(val user: AuthenticatedUser): AuthenticationResult()
    data object PendingActivation: AuthenticationResult()
    data object InvalidCredentials: AuthenticationResult()
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

class LocalIdentityAccessRepository {
    fun authenticate(identifier: String, password: String): AuthenticationResult {
        val account = MultiTenantStore.account(identifier, password) ?: return AuthenticationResult.InvalidCredentials
        if (account.optString("status") != "active") return AuthenticationResult.PendingActivation
        if (account.optString("role") != "platformAdmin" &&
            MultiTenantStore.organization(account.optString("organizationId"))?.optString("status") != "active"
        ) return AuthenticationResult.PendingActivation
        return AuthenticationResult.Success(MultiTenantStore.asAuthenticatedUser(account))
    }

    fun register(request: RegistrationRequest): RegistrationResult = RegistrationResult(
        false, "Select company registration or an invitation."
    )

    fun isEmailAvailable(userId: String, email: String): Boolean = MultiTenantStore.items("users").none {
        it.optString("id") != userId && it.optString("email").equals(email.trim(), true)
    }

    fun updateEmail(userId: String, email: String) {
        val target = MultiTenantStore.user(userId) ?: return
        MultiTenantStore.updateUser(
            userId, target.optString("firstName"), target.optString("lastName"),
            target.optString("phone"), email
        )
    }
}
