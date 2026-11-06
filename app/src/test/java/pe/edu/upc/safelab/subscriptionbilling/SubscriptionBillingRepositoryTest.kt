package pe.edu.upc.safelab.subscriptionbilling

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.data.repository.SubscriptionBillingRepository
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.domain.model.PaymentStatus
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.domain.model.SubscriptionStatus
import java.time.LocalDate

class SubscriptionBillingRepositoryTest {
    @Test fun administratorHasActiveSubscription() {
        val billing = SubscriptionBillingRepository.getForUser("usr-admin-001")
        assertNotNull(billing)
        assertEquals(SubscriptionStatus.ACTIVE, billing!!.subscription.status)
    }

    @Test fun receiptsAreReadOnlyPaidRecordsWithCorrectTotals() {
        val history = SubscriptionBillingRepository.getForUser("usr-admin-001")!!.paymentHistory
        assertEquals(4, history.size)
        assertTrue(history.all { it.status == PaymentStatus.PAID })
        assertTrue(history.all { it.totalCents == it.subtotalCents + it.taxCents })
        assertTrue(history.all { it.reference != null && it.paidDate != null })
    }

    @Test fun upcomingInvoiceHasDueDateAndNoPaymentConfirmation() {
        val invoice = SubscriptionBillingRepository.getForUser("usr-admin-001")!!.upcomingInvoice!!
        assertEquals(PaymentStatus.UPCOMING, invoice.status)
        assertTrue(LocalDate.parse(invoice.dueDate).isAfter(LocalDate.parse(invoice.issuedDate)))
        assertNull(invoice.paidDate)
        assertNull(invoice.reference)
    }

    @Test fun lookupUsesDocumentId() {
        val billing = SubscriptionBillingRepository.getForUser("usr-admin-001")!!
        assertEquals(billing.upcomingInvoice, billing.findDocument("invoice-2026-12"))
        assertEquals(billing.paymentHistory.first(), billing.findDocument("receipt-202611"))
        assertNull(billing.findDocument("nonexistent"))
    }

    @Test fun doesNotInventBillingRecordsForUnknownAccounts() {
        assertNull(SubscriptionBillingRepository.getForUser("usr-unknown"))
    }
}
