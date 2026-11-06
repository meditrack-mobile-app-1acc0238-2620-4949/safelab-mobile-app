package pe.edu.upc.safelab.remoteactuation

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.safelab.boundedcontext.remoteactuation.data.repository.RemoteActuationRepository as Repo
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.ActuationAction
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.CommandRequestResult
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.RemoteCommandError
import pe.edu.upc.safelab.boundedcontext.remoteactuation.domain.model.RemoteCommandStatus

/** Pure Kotlin tests: no hardware, network or Android emulator required. */
class RemoteActuationRepositoryTest {
    @After fun reset() = Repo.resetDemo()

    @Test fun sampleCommandsAlignWithAuditTrail() {
        assertEquals("Cold Chamber A-01", Repo.command("CMD-081")?.deviceName)
        assertEquals("Gateway S-02", Repo.command("CMD-080")?.deviceName)
        assertEquals("Freezer B-02", Repo.command("CMD-079")?.deviceName)
        assertEquals("asset-001", Repo.device("ctrl-001")?.assetId)
    }

    @Test fun offlineDeviceIsRejected() {
        assertEquals(CommandRequestResult.Rejected(RemoteCommandError.OFFLINE),
            Repo.simulateCommand("ctrl-002", ActuationAction.COOLING_BOOST, "Tester", "ADMINISTRATOR"))
    }

    @Test fun onlyAdministratorCanSimulate() {
        assertFalse(Repo.isAuthorized("OPERATOR"))
        assertTrue(Repo.isAuthorized("ADMINISTRATOR"))
        assertEquals(CommandRequestResult.Rejected(RemoteCommandError.NOT_ALLOWED),
            Repo.simulateCommand("ctrl-001", ActuationAction.COOLING_BOOST, "Tester", "OPERATOR"))
    }

    @Test fun rejectsInvalidActionAndUnknownDevice() {
        assertEquals(CommandRequestResult.Rejected(RemoteCommandError.INVALID_ACTION),
            Repo.simulateCommand("ctrl-gw-02", ActuationAction.COOLING_BOOST, "Tester", "ADMINISTRATOR"))
        assertEquals(CommandRequestResult.Rejected(RemoteCommandError.NOT_FOUND),
            Repo.simulateCommand("unknown", ActuationAction.RESTART_GATEWAY, "Tester", "ADMINISTRATOR"))
    }

    @Test fun successfulSimulationUpdatesOnlyDemoState() {
        val result = Repo.simulateCommand("ctrl-001", ActuationAction.COOLING_BOOST, "Tester", "ADMINISTRATOR")
        assertTrue(result is CommandRequestResult.Accepted)
        assertEquals("BOOST", Repo.device("ctrl-001")?.currentMode)
        assertEquals(RemoteCommandStatus.SIMULATED, Repo.history().first().status)
        assertEquals(4, Repo.history().size)
    }

    @Test fun repeatedCoolingModeIsRejected() {
        Repo.simulateCommand("ctrl-001", ActuationAction.COOLING_BOOST, "Tester", "ADMINISTRATOR")
        assertEquals(CommandRequestResult.Rejected(RemoteCommandError.ALREADY_ACTIVE),
            Repo.simulateCommand("ctrl-001", ActuationAction.COOLING_BOOST, "Tester", "ADMINISTRATOR"))
        assertEquals(4, Repo.history().size)
    }

    @Test fun gatewayCanRestartMultipleTimesInDemo() {
        repeat(2) {
            assertTrue(Repo.simulateCommand("ctrl-gw-02", ActuationAction.RESTART_GATEWAY,
                "Tester", "ADMINISTRATOR") is CommandRequestResult.Accepted)
        }
        assertEquals(5, Repo.history().size)
    }

    @Test fun resetRestoresSampleData() {
        Repo.simulateCommand("ctrl-001", ActuationAction.COOLING_BOOST, "Tester", "ADMINISTRATOR")
        Repo.resetDemo()
        assertEquals("NORMAL", Repo.device("ctrl-001")?.currentMode)
        assertEquals(3, Repo.history().size)
    }
}
