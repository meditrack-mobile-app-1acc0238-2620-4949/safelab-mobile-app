package pe.edu.upc.safelab.boundedcontext.subscriptionbilling.domain.model

/** Information supplied by the subscription service in a future API integration. */
enum class SubscriptionStatus { ACTIVE, PENDING_ACTIVATION, EXPIRED, SUSPENDED }
enum class PaymentStatus { PAID, UPCOMING, OVERDUE }

data class Subscription(
    val id: String,
    val planName: String,
    val status: SubscriptionStatus,
    val startDate: String,
    val renewalDate: String,
    val cycle: String,
    val priceCents: Long,
    val currencyCode: String
)

/** A receipt/invoice is read-only in the mobile app. Amounts are stored in cents. */
data class BillingDocument(
    val id: String,
    val number: String,
    val status: PaymentStatus,
    val issuedDate: String,
    val dueDate: String,
    val periodStart: String,
    val periodEnd: String,
    val subtotalCents: Long,
    val taxCents: Long,
    val currencyCode: String,
    val paidDate: String? = null,
    val method: String? = null,
    val reference: String? = null
) {
    val totalCents: Long get() = subtotalCents + taxCents
}

data class BillingAccount(
    val subscription: Subscription,
    val upcomingInvoice: BillingDocument?,
    val paymentHistory: List<BillingDocument>
) {
    fun findDocument(id: String): BillingDocument? =
        (paymentHistory + listOfNotNull(upcomingInvoice)).firstOrNull { it.id == id }
}
