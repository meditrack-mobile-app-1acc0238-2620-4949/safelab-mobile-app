package pe.edu.upc.safelab.boundedcontext.identityaccess.domain.service

import java.security.SecureRandom
import java.util.UUID

/**
 * Creates unrelated identifiers for a demonstration account.
 * - UUID v4: internal primary identifier used to link records across BCs.
 * - 18 digits: immutable, human-visible username. It is NOT a security token.
 *
 * When the backend exists, it must generate these server-side, enforce UNIQUE
 * constraints and retry any database collisions in a transaction.
 */
object UserIdentifierGenerator {
    private val random = SecureRandom()

    fun generateUserId(): String = UUID.randomUUID().toString()

    fun generateUsername(): String = buildString(18) {
        append(random.nextInt(9) + 1) // No leading zero.
        repeat(17) { append(random.nextInt(10)) }
    }
}
