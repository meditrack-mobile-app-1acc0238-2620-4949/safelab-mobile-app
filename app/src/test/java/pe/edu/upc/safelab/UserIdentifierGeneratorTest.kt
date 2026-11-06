package pe.edu.upc.safelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.local.IdentityAccessMockData
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository.AuthenticationResult
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository.LocalIdentityAccessRepository
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository.RegistrationRequest
import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.service.UserIdentifierGenerator
import pe.edu.upc.safelab.common.DemoAccountIds
import java.util.UUID

class UserIdentifierGeneratorTest {
    @Test fun usernameIs18DigitsAndNeverStartsWithZero() {
        repeat(100) {
            val name = UserIdentifierGenerator.generateUsername()
            assertTrue(name.matches(Regex("[1-9][0-9]{17}")))
        }
    }

    @Test fun internalIdsAreValidVersion4Uuids() {
        repeat(30) {
            val id = UUID.fromString(UserIdentifierGenerator.generateUserId())
            assertEquals(4, id.version())
        }
    }

    @Test fun registrationsWithSameNamesReceiveDistinctImmutableUsernames() {
        val repo = LocalIdentityAccessRepository()
        val marker = UUID.randomUUID().toString().replace("-", "").take(16)
        fun request(index: Int) = RegistrationRequest(
            "Maria Elena", "Lopez Garcia", "999999999", "$marker$index@hotmail.com", "Testing2026%"
        )
        val first = repo.register(request(1))
        val second = repo.register(request(2))
        assertTrue(first.successful)
        assertTrue(second.successful)
        assertNotEquals(first.generatedUsername, second.generatedUsername)
        assertNotEquals(first.userId, second.userId)
        assertEquals(4, UUID.fromString(first.userId!!).version())
        assertTrue(first.generatedUsername!!.matches(Regex("[1-9][0-9]{17}")))
        assertTrue(repo.authenticate(first.generatedUsername!!, "Testing2026%") is AuthenticationResult.PendingActivation)
    }

    @Test fun administratorAndBillingShareStableId() {
        assertEquals(DemoAccountIds.ADMIN_ID, IdentityAccessMockData.administrator.id)
        assertEquals(4, UUID.fromString(DemoAccountIds.ADMIN_ID).version())
        assertTrue(IdentityAccessMockData.ADMIN_USERNAME.matches(Regex("[1-9][0-9]{17}")))
        assertEquals("admin@hotmail.com", IdentityAccessMockData.ADMIN_EMAIL)
        assertEquals("SafeLab2026%", IdentityAccessMockData.ADMIN_PASSWORD)
    }
}
