package pe.edu.upc.safelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.safelab.common.DemoAccountIds
import java.io.File

/** The packaged JSON is the only source of local mock credentials. */
class DemoLoginTest {
    @Test fun packagedAdminExistsOnlyInTheSharedSeed() {
        val seed = File("src/main/assets/safelab_data.json").readText()
        assertTrue(seed.contains("admin@safelab.test"))
        assertTrue(seed.contains("platformAdmin"))
        assertTrue(seed.contains(DemoAccountIds.ADMIN_ID))
    }

    @Test fun platformIdentityRemainsStableAcrossModules() {
        assertEquals("SafeLab Administrator", DemoAccountIds.ADMIN_DISPLAY_NAME)
    }
}
