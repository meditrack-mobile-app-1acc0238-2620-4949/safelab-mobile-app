package pe.edu.upc.safelab.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import pe.edu.upc.safelab.common.LocalAppLanguage
import pe.edu.upc.safelab.common.tr
import pe.edu.upc.safelab.shared.domain.model.AppModule
import pe.edu.upc.safelab.shared.domain.model.MobileTab
import pe.edu.upc.safelab.shared.presentation.layouts.AppShell
import pe.edu.upc.safelab.shared.presentation.screens.MoreScreen

@Composable
fun SafeLabApp(isDarkTheme: Boolean, onThemeToggle: () -> Unit) {
    var isAuthenticated by rememberSaveable { mutableStateOf(false) }
    var selectedTabName by rememberSaveable { mutableStateOf(MobileTab.HOME.name) }
    var moreModuleName by rememberSaveable { mutableStateOf<String?>(null) }
    var language by rememberSaveable { mutableStateOf("EN") }

    CompositionLocalProvider(LocalAppLanguage provides language) {
        if (!isAuthenticated) {
            IdentityAccessEntryScreen(
                language = language,
                isDarkTheme = isDarkTheme,
                onLanguageToggle = { language = if (language == "EN") "ES" else "EN" },
                onThemeToggle = onThemeToggle,
                onAuthenticated = {
                    isAuthenticated = true
                    selectedTabName = MobileTab.HOME.name
                    moreModuleName = null
                }
            )
            return@CompositionLocalProvider
        }

        val selectedTab = MobileTab.valueOf(selectedTabName)
        val moreModule = moreModuleName?.let { AppModule.valueOf(it) }
        val title = when (selectedTab) {
            MobileTab.HOME -> tr(language, "Home", "Inicio")
            MobileTab.SENSORS -> tr(language, "Sensors", "Sensores")
            MobileTab.ALERTS -> tr(language, "Alerts", "Alertas")
            MobileTab.EQUIPMENT -> tr(language, "Equipment", "Equipos")
            MobileTab.MORE -> moreModule?.shortLabel(language) ?: tr(language, "More", "Más")
        }

        fun selectTab(tab: MobileTab) {
            if (tab == MobileTab.MORE) moreModuleName = null
            selectedTabName = tab.name
        }
        fun backToMore() { moreModuleName = null }

        BackHandler(enabled = selectedTab != MobileTab.HOME) {
            if (selectedTab == MobileTab.MORE && moreModule != null) backToMore()
            else selectTab(MobileTab.HOME)
        }

        AppShell(
            selectedTab = selectedTab,
            title = title,
            language = language,
            isDarkTheme = isDarkTheme,
            onTabSelected = ::selectTab,
            onBack = if (selectedTab == MobileTab.MORE && moreModule != null) ::backToMore else null,
            onLanguageToggle = { language = if (language == "EN") "ES" else "EN" },
            onThemeToggle = onThemeToggle
        ) {
            when (selectedTab) {
                MobileTab.HOME -> DashboardOverviewEntryScreen()
                MobileTab.SENSORS -> SensorMonitoringEntryScreen()
                MobileTab.ALERTS -> AlertsIncidentsEntryScreen()
                MobileTab.EQUIPMENT -> EquipmentMaintenanceEntryScreen()
                MobileTab.MORE -> when (moreModule) {
                    AppModule.MONITORING_ORGANIZATION -> MonitoringOrganizationEntryScreen()
                    AppModule.REPORTING_COMPLIANCE -> ReportingComplianceEntryScreen()
                    AppModule.AUDIT_TRACEABILITY -> AuditTraceabilityEntryScreen()
                    else -> MoreScreen(
                        language = language,
                        onOpenModule = { moreModuleName = it.name },
                        onSignOut = {
                            moreModuleName = null
                            selectedTabName = MobileTab.HOME.name
                            isAuthenticated = false
                        }
                    )
                }
            }
        }
    }
}