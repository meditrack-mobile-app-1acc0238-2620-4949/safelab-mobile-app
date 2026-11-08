package pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository

import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.model.AuthenticatedUser
import pe.edu.upc.safelab.shared.infrastructure.data.MultiTenantStore
import pe.edu.upc.safelab.shared.infrastructure.http.SafeLabApi
import pe.edu.upc.safelab.shared.infrastructure.http.SafeLabHttpException

sealed class AuthenticationResult {
    data class Success(val user: AuthenticatedUser): AuthenticationResult()
    data object PendingActivation: AuthenticationResult()
    data object InvalidCredentials: AuthenticationResult()
    data class NetworkError(val message: String): AuthenticationResult()
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
    suspend fun authenticate(identifier: String, password: String): AuthenticationResult {
        if (SafeLabApi.isMock) {
            val account = MultiTenantStore.account(identifier, password) ?: return AuthenticationResult.InvalidCredentials
            if (account.optString("status") != "active") return AuthenticationResult.PendingActivation
            return AuthenticationResult.Success(MultiTenantStore.asAuthenticatedUser(account))
        }
        return try {
            val user = SafeLabApi.signIn(identifier.trim(), password)
            MultiTenantStore.refreshFromBackend()
            AuthenticationResult.Success(MultiTenantStore.asAuthenticatedUser(user))
        } catch (error: SafeLabHttpException) {
            when (error.status) {
                401 -> AuthenticationResult.InvalidCredentials
                403 -> AuthenticationResult.PendingActivation
                else -> AuthenticationResult.NetworkError(error.message)
            }
        } catch (error: Exception) {
            AuthenticationResult.NetworkError(error.message ?: "SafeLab API no disponible")
        }
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
