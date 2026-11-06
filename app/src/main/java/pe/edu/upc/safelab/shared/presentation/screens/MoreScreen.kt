package pe.edu.upc.safelab.shared.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.R
import pe.edu.upc.safelab.shared.domain.model.AppModule

@Composable
fun MoreScreen(
    language: String,
    onOpenModule: (AppModule) -> Unit,
    onSignOut: () -> Unit
) {
    val spanish = language == "ES"
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(shape = RoundedCornerShape(24.dp), shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(
                        Brush.horizontalGradient(listOf(Color(0xFF1D4ED8), Color(0xFF312EAA)))
                    ).padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).background(Color.White, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(painterResource(R.drawable.safelab_shield), "SafeLab logo", modifier = Modifier.size(39.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("SafeLab", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Black)
                        Text(
                            if (spanish) "Monitoreo de la cadena de frío" else "Clinical cold chain monitoring",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = .85f)
                        )
                    }
                }
            }
        }
        item {
            Text(
                if (spanish) "OPERACIONES" else "OPERATIONS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
        }
        item {
            MoreModuleRow(
                title = if (spanish) "Organización" else "Organization",
                subtitle = if (spanish) "Sedes, áreas y registro de equipos" else "Sites, storage areas and equipment registry",
                icon = R.drawable.ic_nav_organization,
                onClick = { onOpenModule(AppModule.MONITORING_ORGANIZATION) }
            )
        }
        item {
            MoreModuleRow(
                title = if (spanish) "Reportes y cumplimiento" else "Reports & Compliance",
                subtitle = if (spanish) "Reportes, historial y exportaciones" else "Reports, history and exports",
                icon = R.drawable.ic_nav_reports,
                onClick = { onOpenModule(AppModule.REPORTING_COMPLIANCE) }
            )
        }
        item {
            MoreModuleRow(
                title = if (spanish) "Auditoría y trazabilidad" else "Audit & Traceability",
                subtitle = if (spanish) "Registro de acciones y evidencias" else "Activity history and evidence trail",
                icon = R.drawable.ic_nav_audit,
                onClick = { onOpenModule(AppModule.AUDIT_TRACEABILITY) }
            )
        }
        item {
            Text(
                if (spanish) "CUENTA" else "ACCOUNT",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        if (spanish) "Sesión activa" else "Active session",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("SafeLab Administrator", fontWeight = FontWeight.Bold)
                    Divider(modifier = Modifier.padding(vertical = 9.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = onSignOut).padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(painterResource(R.drawable.ic_nav_logout), contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(23.dp))
                        Text(if (spanish) "Cerrar sesión" else "Sign out", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item { Spacer(Modifier.size(4.dp)) }
    }
}

@Composable
private fun MoreModuleRow(title: String, subtitle: String, icon: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Box(
                modifier = Modifier.size(47.dp).background(Color(0xFFEDE9FE), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(icon), contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(25.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(painterResource(R.drawable.ic_nav_chevron), contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}