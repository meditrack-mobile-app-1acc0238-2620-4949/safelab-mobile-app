package pe.edu.upc.safelab.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.screens.AlertsIncidentsEntryScreen
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.screens.AuditTraceabilityEntryScreen
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.presentation.screens.DashboardOverviewEntryScreen
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.screens.EquipmentMaintenanceEntryScreen
import pe.edu.upc.safelab.boundedcontext.identityaccess.presentation.screens.IdentityAccessEntryScreen
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.screens.MonitoringOrganizationEntryScreen
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.screens.ReportingComplianceEntryScreen
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.screens.SensorMonitoringEntryScreen
import pe.edu.upc.safelab.shared.domain.model.AppModule
import pe.edu.upc.safelab.shared.presentation.layouts.AppShell

@Composable
fun SafeLabApp(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    var isAuthenticated by rememberSaveable { mutableStateOf(false) }
    var selectedModuleName by rememberSaveable {
        mutableStateOf(AppModule.DASHBOARD_OVERVIEW.name)
    }
    var language by rememberSaveable { mutableStateOf("EN") }

    if (!isAuthenticated) {
        IdentityAccessEntryScreen(
            onAuthenticated = {
                isAuthenticated = true
                selectedModuleName = AppModule.DASHBOARD_OVERVIEW.name
            }
        )
        return
    }

    val selectedModule = AppModule.valueOf(selectedModuleName)

    AppShell(
        selectedModule = selectedModule,
        language = language,
        isDarkTheme = isDarkTheme,
        onModuleSelected = { module ->
            if (module == AppModule.IDENTITY_ACCESS) {
                isAuthenticated = false
                selectedModuleName = AppModule.DASHBOARD_OVERVIEW.name
            } else {
                selectedModuleName = module.name
            }
        },
        onLanguageToggle = { language = if (language == "EN") "ES" else "EN" },
        onThemeToggle = onThemeToggle
    ) {
        when (selectedModule) {
            AppModule.DASHBOARD_OVERVIEW -> DashboardOverviewEntryScreen()
            AppModule.MONITORING_ORGANIZATION -> MonitoringOrganizationEntryScreen()
            AppModule.SENSOR_MONITORING -> SensorMonitoringEntryScreen()
            AppModule.ALERTS_INCIDENTS -> AlertsIncidentsEntryScreen()
            AppModule.EQUIPMENT_MAINTENANCE -> EquipmentMaintenanceEntryScreen()
            AppModule.REPORTING_COMPLIANCE -> ReportingComplianceEntryScreen()
            AppModule.AUDIT_TRACEABILITY -> AuditTraceabilityEntryScreen()
            AppModule.IDENTITY_ACCESS -> Unit
        }
    }
}
