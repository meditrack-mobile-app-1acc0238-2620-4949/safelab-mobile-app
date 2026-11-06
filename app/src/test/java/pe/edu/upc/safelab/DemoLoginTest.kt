package pe.edu.upc.safelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository.LocalIdentityAccessRepository

class DemoLoginTest {
    private val login = LocalIdentityAccessRepository()

    @Test fun `administrator logs in with demo credentials`() {
        assertEquals("ADMINISTRATOR", login.authenticate("admin", "SafeLab2026")?.role)
    }

    @Test fun `invalid password does not authenticate`() {
        assertNull(login.authenticate("admin", "invalid"))
    }
}
