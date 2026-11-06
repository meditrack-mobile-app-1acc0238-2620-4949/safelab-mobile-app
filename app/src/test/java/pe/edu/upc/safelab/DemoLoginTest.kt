package pe.edu.upc.safelab

import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository.AuthenticationResult
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository.LocalIdentityAccessRepository

/** Basic IAM regression tests for the in-memory authentication contract. */
class DemoLoginTest {
    private val login = LocalIdentityAccessRepository()

    @Test fun administratorLogsInWithValidCredentials() {
        val result = login.authenticate("admin", "SafeLab2026")
        assertTrue(result is AuthenticationResult.Success)
    }

    @Test fun invalidPasswordDoesNotAuthenticate() {
        val result = login.authenticate("admin", "invalid")
        assertTrue(result is AuthenticationResult.InvalidCredentials)
    }
}
