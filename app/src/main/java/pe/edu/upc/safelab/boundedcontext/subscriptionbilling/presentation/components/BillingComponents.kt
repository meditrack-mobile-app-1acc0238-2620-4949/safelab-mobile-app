package pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.R
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.domain.model.BillingDocument
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.domain.model.PaymentStatus
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.domain.model.SubscriptionStatus
import pe.edu.upc.safelab.common.tr
import pe.edu.upc.safelab.ui.theme.SafeLabSuccess
import pe.edu.upc.safelab.ui.theme.SafeLabWarning
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.FormatStyle
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

/** Formatting stays in the UI, never in the backend domain models. */
fun money(cents: Long, currencyCode: String, language: String): String {
    val locale = if (language == "ES") Locale.forLanguageTag("es-PE") else Locale.US
    return NumberFormat.getCurrencyInstance(locale).apply {
        currency = Currency.getInstance(currencyCode)
    }.format(cents / 100.0)
}

fun dateLabel(date: String, language: String): String = runCatching {
    val locale = if (language == "ES") Locale.forLanguageTag("es-PE") else Locale.US
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(locale).format(LocalDate.parse(date))
}.getOrDefault(date)

fun paymentStatusLabel(status: PaymentStatus, language: String) = when (status) {
    PaymentStatus.PAID -> tr(language, "Paid", "Pagado")
    PaymentStatus.UPCOMING -> tr(language, "Upcoming", "Próximo")
    PaymentStatus.OVERDUE -> tr(language, "Overdue", "Vencido")
}

fun subscriptionStatusLabel(status: SubscriptionStatus, language: String) = when (status) {
    SubscriptionStatus.ACTIVE -> tr(language, "Active", "Activa")
    SubscriptionStatus.PENDING_ACTIVATION -> tr(language, "Pending activation", "Pendiente de activación")
    SubscriptionStatus.EXPIRED -> tr(language, "Expired", "Vencida")
    SubscriptionStatus.SUSPENDED -> tr(language, "Suspended", "Suspendida")
}

@Composable
fun BillingSection(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
fun BillingDetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BillingAmountRow(label: String, value: String, total: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(
            label, modifier = Modifier.weight(1f),
            style = if (total) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (total) FontWeight.Bold else FontWeight.Normal,
            color = if (total) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, fontWeight = FontWeight.Bold, style = if (total) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun BillingStatusBadge(text: String, isPositive: Boolean) {
    val tone = if (isPositive) SafeLabSuccess else SafeLabWarning
    Surface(shape = CircleShape, color = tone.copy(alpha = 0.14f)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = tone,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BillingDocumentRow(document: BillingDocument, language: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(15.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_nav_reports),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(23.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(document.number, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(
                    dateLabel(document.paidDate ?: document.dueDate, language),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    paymentStatusLabel(document.status, language),
                    color = if (document.status == PaymentStatus.PAID) SafeLabSuccess else SafeLabWarning,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(money(document.totalCents, document.currencyCode, language), fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(painterResource(R.drawable.ic_nav_chevron), contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}
