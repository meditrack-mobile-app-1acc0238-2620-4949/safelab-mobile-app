package pe.edu.upc.safelab

import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.local.IdentityAccessMockData
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository.AuthenticationResult
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository.LocalIdentityAccessRepository

/** Basic IAM regression tests for the in-memory authentication contract. */
class DemoLoginTest {
    private val login = LocalIdentityAccessRepository()

    @Test fun administratorLogsInWithValidCredentials() {
        val result = login.authenticate(IdentityAccessMockData.ADMIN_EMAIL, IdentityAccessMockData.ADMIN_PASSWORD)
        assertTrue(result is AuthenticationResult.Success)
    }

    @Test fun invalidPasswordDoesNotAuthenticate() {
        val result = login.authenticate(IdentityAccessMockData.ADMIN_USERNAME, "invalid")
        assertTrue(result is AuthenticationResult.InvalidCredentials)
    }
}
