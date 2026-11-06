package pe.edu.upc.safelab.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import pe.edu.upc.safelab.boundedcontext.alertsincidents.presentation.screens.AlertsIncidentsEntryScreen
import pe.edu.upc.safelab.boundedcontext.audittraceability.presentation.screens.AuditTraceabilityEntryScreen
import pe.edu.upc.safelab.boundedcontext.dashboardoverview.presentation.screens.DashboardOverviewEntryScreen
import pe.edu.upc.safelab.boundedcontext.equipmentmaintenance.presentation.screens.EquipmentMaintenanceEntryScreen
import pe.edu.upc.safelab.boundedcontext.environmentalcompliance.presentation.screens.EnvironmentalComplianceEntryScreen
import pe.edu.upc.safelab.boundedcontext.identityaccess.presentation.screens.IdentityAccessEntryScreen
import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.model.AuthenticatedUser
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository.LocalIdentityAccessRepository
import pe.edu.upc.safelab.boundedcontext.userprofiles.data.repository.UserProfilesRepository
import pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model.ProfileSaveResult
import pe.edu.upc.safelab.boundedcontext.userprofiles.presentation.screens.UserProfileScreen
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.data.repository.SubscriptionBillingRepository
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.screens.SubscriptionBillingScreen
import pe.edu.upc.safelab.boundedcontext.subscriptionbilling.presentation.screens.BillingDocumentDetailScreen
import pe.edu.upc.safelab.common.buildAvatarInitials
import pe.edu.upc.safelab.common.buildDisplayName
import pe.edu.upc.safelab.boundedcontext.monitoringorganization.presentation.screens.MonitoringOrganizationEntryScreen
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.screens.ReportingComplianceEntryScreen
import pe.edu.upc.safelab.boundedcontext.sensormonitoring.presentation.screens.SensorMonitoringEntryScreen
import pe.edu.upc.safelab.common.LocalAppLanguage
import pe.edu.upc.safelab.common.tr
import pe.edu.upc.safelab.shared.domain.model.AppModule
import pe.edu.upc.safelab.shared.domain.model.MobileTab
import pe.edu.upc.safelab.shared.presentation.layouts.AppShell
import pe.edu.upc.safelab.shared.presentation.screens.MoreScreen

private enum class AccountPage { PROFILE, BILLING }

@Composable
fun SafeLabApp(isDarkTheme: Boolean, onThemeToggle: () -> Unit) {
    // Keep the session in memory: a restored UI state must not bypass login.
    var signedInUser by remember { mutableStateOf<AuthenticatedUser?>(null) }
    var accountPage by remember { mutableStateOf<AccountPage?>(null) }
    var selectedBillingDocumentId by remember { mutableStateOf<String?>(null) }
    var selectedTabName by rememberSaveable { mutableStateOf(MobileTab.HOME.name) }
    var moreModuleName by rememberSaveable { mutableStateOf<String?>(null) }
    var language by rememberSaveable { mutableStateOf("EN") }

    CompositionLocalProvider(LocalAppLanguage provides language) {
        if (signedInUser == null) {
            IdentityAccessEntryScreen(
                language = language,
                isDarkTheme = isDarkTheme,
                onLanguageToggle = { language = if (language == "EN") "ES" else "EN" },
                onThemeToggle = onThemeToggle,
                onAuthenticated = { user ->
                    signedInUser = user
                    UserProfilesRepository.ensureFor(user)
                    selectedTabName = MobileTab.HOME.name
                    moreModuleName = null
                    accountPage = null
                    selectedBillingDocumentId = null
                },
                onRegistered = { id, username, request ->
                    UserProfilesRepository.addPending(id, username, request.firstNames,
                        request.lastNames, request.email, request.phone)
                }
            )
            return@CompositionLocalProvider
        }

        val sessionUser = signedInUser ?: return@CompositionLocalProvider
        val profile = UserProfilesRepository.ensureFor(sessionUser)
        val accountName = buildDisplayName(profile.firstNames, profile.lastNames)
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
            accountPage = null
            selectedBillingDocumentId = null
        }
        fun backToMore() { moreModuleName = null }
        fun goBack() {
            when {
                accountPage == AccountPage.BILLING && selectedBillingDocumentId != null ->
                    selectedBillingDocumentId = null
                accountPage == AccountPage.BILLING -> accountPage = AccountPage.PROFILE
                accountPage == AccountPage.PROFILE -> accountPage = null
                selectedTab == MobileTab.MORE && moreModule != null -> backToMore()
                else -> selectTab(MobileTab.HOME)
            }
        }

        BackHandler(enabled = accountPage != null || selectedTab != MobileTab.HOME) { goBack() }

        AppShell(
            selectedTab = selectedTab,
            title = when {
                accountPage == AccountPage.BILLING && selectedBillingDocumentId != null ->
                    tr(language, "Document details", "Detalle de documento")
                accountPage == AccountPage.BILLING -> tr(language, "Subscription & billing", "Suscripción y pagos")
                accountPage == AccountPage.PROFILE -> tr(language, "My profile", "Mi perfil")
                else -> title
            },
            language = language,
            isDarkTheme = isDarkTheme,
            onTabSelected = ::selectTab,
            onBack = if (accountPage != null || (selectedTab == MobileTab.MORE && moreModule != null))
                ::goBack else null,
            onLanguageToggle = { language = if (language == "EN") "ES" else "EN" },
            onThemeToggle = onThemeToggle,
            avatarInitials = buildAvatarInitials(profile.firstNames),
            onProfileClick = {
                accountPage = AccountPage.PROFILE
                selectedBillingDocumentId = null
            }
        ) {
            when (accountPage) {
                AccountPage.PROFILE -> UserProfileScreen(
                    profile = profile,
                    language = language,
                    onOpenSubscription = { accountPage = AccountPage.BILLING },
                    onSave = { changes ->
                        val auth = LocalIdentityAccessRepository()
                        val result = UserProfilesRepository.save(profile.userId, changes) { email ->
                            auth.isEmailAvailable(profile.userId, email)
                        }
                        if (result == ProfileSaveResult.SUCCESS) auth.updateEmail(profile.userId, changes.email)
                        result
                    }
                )
                AccountPage.BILLING -> {
                    val billing = SubscriptionBillingRepository.getForUser(profile.userId)
                    val documentId = selectedBillingDocumentId
                    if (documentId == null) {
                        SubscriptionBillingScreen(
                            billing = billing,
                            language = language,
                            onOpenDocument = { selectedBillingDocumentId = it }
                        )
                    } else {
                        BillingDocumentDetailScreen(
                            document = billing?.findDocument(documentId),
                            language = language
                        )
                    }
                }
                null -> when (selectedTab) {
                    MobileTab.HOME -> DashboardOverviewEntryScreen()
                    MobileTab.SENSORS -> SensorMonitoringEntryScreen()
                    MobileTab.ALERTS -> AlertsIncidentsEntryScreen()
                    MobileTab.EQUIPMENT -> EquipmentMaintenanceEntryScreen()
                    MobileTab.MORE -> when (moreModule) {
                        AppModule.MONITORING_ORGANIZATION -> MonitoringOrganizationEntryScreen()
                        AppModule.REPORTING_COMPLIANCE -> ReportingComplianceEntryScreen(
                            onOpenEnvironmentalCompliance = {
                                moreModuleName = AppModule.ENVIRONMENTAL_COMPLIANCE.name
                            }
                        )
                        AppModule.ENVIRONMENTAL_COMPLIANCE -> EnvironmentalComplianceEntryScreen()
                        AppModule.AUDIT_TRACEABILITY -> AuditTraceabilityEntryScreen()
                        else -> MoreScreen(
                            language = language,
                            onOpenModule = { moreModuleName = it.name },
                            onOpenProfile = { accountPage = AccountPage.PROFILE },
                            accountName = accountName,
                            onSignOut = {
                                moreModuleName = null
                                selectedTabName = MobileTab.HOME.name
                                signedInUser = null
                                accountPage = null
                                selectedBillingDocumentId = null
                            }
                        )
                    }
                }
            }
        }
    }
}
