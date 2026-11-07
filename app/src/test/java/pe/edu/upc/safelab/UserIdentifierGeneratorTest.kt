package pe.edu.upc.safelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
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

    @Test fun generatedUsernamesAreUniqueAcrossManyAccounts() {
        val usernames = (1..2000).map { UserIdentifierGenerator.generateUsername() }
        assertEquals(usernames.size, usernames.toSet().size)
    }

    @Test fun administratorAndBillingShareStableId() {
        assertEquals(4, UUID.fromString(DemoAccountIds.ADMIN_ID).version())
        assertEquals("SafeLab Administrator", DemoAccountIds.ADMIN_DISPLAY_NAME)
    }
}
