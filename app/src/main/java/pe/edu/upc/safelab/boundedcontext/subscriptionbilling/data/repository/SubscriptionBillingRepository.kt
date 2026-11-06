package pe.edu.upc.safelab.boundedcontext.subscriptionbilling.data.repository

import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.domain.model.BillingAccount
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.domain.model.BillingDocument
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.domain.model.PaymentStatus
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.domain.model.Subscription
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.domain.model.SubscriptionStatus

/**
 * Read-only frontend data. Nothing in this object executes payments or changes plans.
 * The values below are fictional and will be replaced by authenticated REST responses.
 */
object SubscriptionBillingRepository {
    private const val ADMIN_ID = "usr-admin-001"

    private fun paidReceipt(month: Int, number: String, reference: String): BillingDocument {
        val start = "2026-${month.toString().padStart(2, '0')}-06"
        val endMonth = month + 1
        val end = "2026-${endMonth.toString().padStart(2, '0')}-05"
        return BillingDocument(
            id = "receipt-$number",
            number = "SL-R-$number",
            status = PaymentStatus.PAID,
            issuedDate = start,
            dueDate = start,
            periodStart = start,
            periodEnd = end,
            subtotalCents = 10000,
            taxCents = 1800,
            currencyCode = "PEN",
            paidDate = start,
            method = "online",
            reference = reference
        )
    }

    private val adminBilling = BillingAccount(
        subscription = Subscription(
            id = "SUB-2026-001",
            planName = "SafeLab Professional",
            status = SubscriptionStatus.ACTIVE,
            startDate = "2026-08-06",
            renewalDate = "2026-12-06",
            cycle = "MONTHLY",
            priceCents = 11800,
            currencyCode = "PEN"
        ),
        upcomingInvoice = BillingDocument(
            id = "invoice-2026-12",
            number = "SL-F-202612",
            status = PaymentStatus.UPCOMING,
            issuedDate = "2026-11-06",
            dueDate = "2026-12-06",
            periodStart = "2026-12-06",
            periodEnd = "2027-01-05",
            subtotalCents = 10000,
            taxCents = 1800,
            currencyCode = "PEN"
        ),
        paymentHistory = listOf(
            paidReceipt(11, "202611", "PAY-202611-01"),
            paidReceipt(10, "202610", "PAY-202610-01"),
            paidReceipt(9, "202609", "PAY-202609-01"),
            paidReceipt(8, "202608", "PAY-202608-01")
        )
    )

    /** The local administrator has sample documents; other users have no invented billing history. */
    fun getForUser(userId: String): BillingAccount? =
        if (userId == ADMIN_ID) adminBilling else null
}
