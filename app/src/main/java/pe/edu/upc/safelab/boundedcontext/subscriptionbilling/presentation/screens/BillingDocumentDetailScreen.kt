package pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.domain.model.BillingDocument
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.domain.model.PaymentStatus
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.components.BillingAmountRow
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.components.BillingDetailRow
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.components.BillingSection
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.components.BillingStatusBadge
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.components.dateLabel
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.components.money
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.components.paymentStatusLabel
import pe.edu.upc.safelab.common.tr

@Composable
fun BillingDocumentDetailScreen(document: BillingDocument?, language: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (document == null) {
            item {
                Text(tr(language, "Document not found.", "Documento no encontrado."),
                    color = MaterialTheme.colorScheme.error)
            }
        } else {
            val isPaid = document.status == PaymentStatus.PAID
            item {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(tr(language, if (isPaid) "Payment details" else "Invoice details",
                        if (isPaid) "Detalle de pago" else "Detalle del recibo"),
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text(document.number, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                BillingSection(tr(language, "Payment summary", "Resumen del pago")) {
                    BillingStatusBadge(paymentStatusLabel(document.status, language), isPaid)
                    BillingAmountRow(tr(language, "Total amount", "Importe total"),
                        money(document.totalCents, document.currencyCode, language), total = true)
                }
            }
            item {
                BillingSection(tr(language, "Invoice information", "Información del recibo")) {
                    BillingDetailRow(tr(language, "Document number", "Número de documento"), document.number)
                    BillingDetailRow(tr(language, "Issue date", "Fecha de emisión"), dateLabel(document.issuedDate, language))
                    BillingDetailRow(tr(language, "Due date", "Fecha de vencimiento"), dateLabel(document.dueDate, language))
                    BillingDetailRow(tr(language, "Billing period", "Período facturado"),
                        dateLabel(document.periodStart, language) + " – " + dateLabel(document.periodEnd, language))
                }
            }
            item {
                BillingSection(tr(language, "Amount breakdown", "Desglose del importe")) {
                    BillingAmountRow(tr(language, "Subtotal", "Subtotal"),
                        money(document.subtotalCents, document.currencyCode, language))
                    BillingAmountRow(tr(language, "Tax (18%)", "IGV (18 %)"),
                        money(document.taxCents, document.currencyCode, language))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    BillingAmountRow(tr(language, "Total", "Total"),
                        money(document.totalCents, document.currencyCode, language), total = true)
                }
            }
            if (isPaid) {
                item {
                    BillingSection(tr(language, "Payment confirmation", "Confirmación de pago")) {
                        document.paidDate?.let {
                            BillingDetailRow(tr(language, "Payment date", "Fecha de pago"), dateLabel(it, language))
                        }
                        document.method?.let {
                            BillingDetailRow(tr(language, "Payment method", "Método de pago"),
                                if (it == "online") tr(language, "Online payment", "Pago en línea") else it)
                        }
                        document.reference?.let {
                            BillingDetailRow(tr(language, "Reference", "Referencia"), it)
                        }
                    }
                }
            }
        }
    }
}
