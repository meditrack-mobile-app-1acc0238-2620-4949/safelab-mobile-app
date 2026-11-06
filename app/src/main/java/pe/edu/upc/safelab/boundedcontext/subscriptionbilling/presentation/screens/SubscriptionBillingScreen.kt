package pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.domain.model.BillingAccount
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.domain.model.SubscriptionStatus
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.components.BillingAmountRow
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.components.BillingDetailRow
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.components.BillingDocumentRow
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.components.BillingSection
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.components.BillingStatusBadge
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.components.dateLabel
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.components.money
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.components.subscriptionStatusLabel
import pe.edu.upc.safelab.common.tr

@Composable
fun SubscriptionBillingScreen(
    billing: BillingAccount?,
    language: String,
    onOpenDocument: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(tr(language, "Subscription & billing", "Suscripción y pagos"),
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text(tr(language, "Review your plan, payments and next invoice.", "Consulta tu plan, pagos y próximo recibo."),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (billing == null) {
            item {
                BillingSection(tr(language, "Subscription information", "Información de suscripción")) {
                    Text(tr(language, "No subscription information is available for this account.",
                        "Esta cuenta todavía no tiene información de suscripción disponible."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            val subscription = billing.subscription
            item {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                    Column(
                        modifier = Modifier.background(Brush.linearGradient(listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        ))).padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(tr(language, "Current subscription", "Suscripción actual"),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(subscription.planName,
                            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        BillingStatusBadge(
                            subscriptionStatusLabel(subscription.status, language),
                            subscription.status == SubscriptionStatus.ACTIVE
                        )
                        Text(
                            money(subscription.priceCents, subscription.currencyCode, language) +
                                tr(language, " / month", " / mes"),
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        BillingDetailRow(tr(language, "Subscription start", "Inicio de suscripción"),
                            dateLabel(subscription.startDate, language))
                        BillingDetailRow(tr(language, "Next renewal", "Próxima renovación"),
                            dateLabel(subscription.renewalDate, language))
                    }
                }
            }

            item {
                BillingSection(tr(language, "Next invoice", "Próximo recibo")) {
                    val nextInvoice = billing.upcomingInvoice
                    if (nextInvoice != null) {
                        Text(tr(language, "Scheduled for", "Programado para") + " " +
                            dateLabel(nextInvoice.dueDate, language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        BillingAmountRow(tr(language, "Amount due", "Importe por pagar"),
                            money(nextInvoice.totalCents, nextInvoice.currencyCode, language), total = true)
                        BillingDocumentRow(nextInvoice, language) { onOpenDocument(nextInvoice.id) }
                    } else {
                        Text(tr(language, "No upcoming invoices.", "No hay recibos próximos."),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Text(tr(language, "Payment history", "Historial de pagos"),
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            if (billing.paymentHistory.isEmpty()) {
                item {
                    BillingSection(tr(language, "Payments", "Pagos")) {
                        Text(tr(language, "No payments have been recorded.", "Aún no se han registrado pagos."),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(billing.paymentHistory, key = { it.id }) { receipt ->
                    BillingDocumentRow(receipt, language) { onOpenDocument(receipt.id) }
                }
            }
        }
    }
}
