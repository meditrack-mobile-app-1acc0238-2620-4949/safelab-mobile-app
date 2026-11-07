package pe.edu.upc.safelab.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upc.safelab.boundedcontext.identityaccess.data.repository.LocalIdentityAccessRepository
import pe.edu.upc.safelab.shared.infrastructure.data.MultiTenantStore
import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.model.AuthenticatedUser
import pe.edu.upc.safelab.boundedcontext.identityaccess.presentation.screens.*
import pe.edu.upc.safelab.shared.presentation.screens.*
import pe.edu.upc.safelab.boundedcontext.userprofiles.data.repository.UserProfilesRepository
import pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model.ProfileSaveResult
import pe.edu.upc.safelab.boundedcontext.userprofiles.presentation.screens.UserProfileScreen
import pe.edu.upc.safelab.common.LocalAppLanguage
import pe.edu.upc.safelab.common.buildAvatarInitials
import pe.edu.upc.safelab.common.tr
import pe.edu.upc.safelab.shared.domain.model.MobileTab
import pe.edu.upc.safelab.shared.presentation.components.AppTopBar
import pe.edu.upc.safelab.shared.presentation.layouts.AppShell

/**
 * Single mobile app; the authenticated role selects its navigation and data scope.
 * All records come from the web-compatible JSON mock repository.
 */
@Composable
fun SafeLabApp(isDarkTheme: Boolean, onThemeToggle: () -> Unit) {
    var user by remember { mutableStateOf<AuthenticatedUser?>(null) }
    var language by rememberSaveable { mutableStateOf("EN") }
    var selectedTab by rememberSaveable { mutableStateOf(MobileTab.HOME.name) }
    var moreScreen by rememberSaveable { mutableStateOf<String?>(null) }
    var profileOpen by remember { mutableStateOf(false) }
    var platformBillingOpen by remember { mutableStateOf(false) }
    val version = MultiTenantStore.version

    androidx.compose.runtime.CompositionLocalProvider(LocalAppLanguage provides language) {
        if (user == null) {
            IdentityAccessEntryScreen(language = language, isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                onLanguageToggle = { language = if (language == "EN") "ES" else "EN" },
                onAuthenticated = { logged ->
                    user = logged
                    UserProfilesRepository.ensureFor(logged)
                    profileOpen = false; selectedTab = MobileTab.HOME.name; moreScreen = null
                }, onRegistered = { _, _, _ -> })
            return@CompositionLocalProvider
        }

        val loggedUser = user ?: return@CompositionLocalProvider
        val actor = MultiTenantStore.user(loggedUser.id) ?: return@CompositionLocalProvider
        val role = actor.optString("role")
        val isPlatformAdmin = role == "platformAdmin"
        val isManager = role == "organizationAdmin"
        val profile = UserProfilesRepository.ensureFor(loggedUser)

        fun signOut() {
            user = null; selectedTab = MobileTab.HOME.name
            moreScreen = null; profileOpen = false; platformBillingOpen = false
        }
        fun back() {
            when {
                platformBillingOpen -> platformBillingOpen = false
                profileOpen -> profileOpen = false
                moreScreen != null -> moreScreen = null
                selectedTab != MobileTab.HOME.name -> selectedTab = MobileTab.HOME.name
            }
        }
        fun saveProfile(changes: pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model.ProfileChanges): ProfileSaveResult {
            val result = UserProfilesRepository.save(profile.userId, changes) { email ->
                LocalIdentityAccessRepository().isEmailAvailable(profile.userId, email)
            }
            return result
        }
        val toggleLanguage = { language = if (language == "EN") "ES" else "EN" }

        if (isPlatformAdmin && !profileOpen && !platformBillingOpen) {
            PlatformAdminEntryScreen(actor, language, isDarkTheme, toggleLanguage, onThemeToggle,
                onProfile = { profileOpen = true }, onSignOut = ::signOut)
            return@CompositionLocalProvider
        }

        if (isPlatformAdmin) {
            BackHandler { back() }
            Scaffold(containerColor = MaterialTheme.colorScheme.background,
                topBar = { AppTopBar(
                    tr(language, "My profile", "Mi perfil"), language, isDarkTheme,
                    ::back, toggleLanguage, onThemeToggle,
                    buildAvatarInitials(profile.firstNames, profile.lastNames), onProfileClick = { profileOpen = true }
                ) }
            ) { padding ->
                androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().padding(padding)) {
                    if (platformBillingOpen) {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            item { PageBanner(tr(language, "Subscriptions", "Suscripciones"),
                                tr(language, "Subscriptions across organizations", "Suscripciones de todas las empresas")) }
                            item { RecordCollectionScreen(language, "subscriptions", tr(language, "All subscriptions", "Todas las suscripciones"), actor) }
                        }
                    } else UserProfileScreen(profile, language, onOpenSubscription = {
                        profileOpen = false; platformBillingOpen = true
                    }, onSave = ::saveProfile)
                }
            }
            return@CompositionLocalProvider
        }

        val allowedTabs = MobileTab.entries.filter { tab ->
            when (tab) {
                MobileTab.HOME -> MultiTenantStore.can(actor, "dashboard.view")
                MobileTab.SENSORS -> MultiTenantStore.can(actor, "sensors.view")
                MobileTab.ALERTS -> MultiTenantStore.can(actor, "incidents.view")
                MobileTab.EQUIPMENT -> MultiTenantStore.can(actor, "maintenance.view")
                MobileTab.MORE -> true
            }
        }.toSet()
        val tab = MobileTab.valueOf(selectedTab).takeIf { it in allowedTabs } ?: MobileTab.MORE
        val accountTitle = when {
            profileOpen -> tr(language, "My profile", "Mi perfil")
            moreScreen == "users" -> tr(language, "Users & permissions", "Usuarios y permisos")
            moreScreen == "billing" -> tr(language, "Subscription & billing", "Suscripción y pagos")
            moreScreen == "compliance" -> tr(language, "Compliance", "Cumplimiento")
            moreScreen == "remote" -> tr(language, "Remote control", "Control remoto")
            moreScreen == "facilities" -> tr(language, "Facilities & assets", "Sedes y activos")
            moreScreen == "reports" -> tr(language, "Reports & analytics", "Reportes y análisis")
            moreScreen == "audit" -> tr(language, "Audit trail", "Auditoría")
            else -> tab.label(language)
        }
        BackHandler(enabled = profileOpen || moreScreen != null || tab != MobileTab.HOME) { back() }
        AppShell(
            selectedTab = tab, title = accountTitle, language = language,
            isDarkTheme = isDarkTheme,
            onTabSelected = { next ->
                if (next in allowedTabs) {
                    selectedTab = next.name; moreScreen = null; profileOpen = false
                }
            },
            onBack = if (profileOpen || moreScreen != null) ::back else null,
            onLanguageToggle = toggleLanguage, onThemeToggle = onThemeToggle,
            avatarInitials = buildAvatarInitials(profile.firstNames, profile.lastNames),
            onProfileClick = { profileOpen = true },
            allowedTabs = allowedTabs
        ) {
            when {
                profileOpen -> UserProfileScreen(profile, language, onOpenSubscription = {
                    if (isManager) { profileOpen = false; moreScreen = "billing" }
                }, onSave = ::saveProfile)
                moreScreen == "users" && isManager -> OrganizationUsersScreen(actor, language)
                moreScreen == "billing" && isManager -> LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    item { BillingSummary(actor.optString("organizationId"), language) }
                }
                moreScreen in listOf("facilities", "compliance", "remote", "reports", "audit") ->
                    BusinessRecords(actor, language, moreScreen.orEmpty())
                tab == MobileTab.HOME -> BusinessDashboard(actor, language) { requested ->
                    when (requested) {
                        "alerts" -> selectedTab = MobileTab.ALERTS.name
                        "sensors" -> selectedTab = MobileTab.SENSORS.name
                    }
                }
                tab == MobileTab.SENSORS -> BusinessRecords(actor, language, "sensors")
                tab == MobileTab.ALERTS -> BusinessRecords(actor, language, "alerts")
                tab == MobileTab.EQUIPMENT -> BusinessRecords(actor, language, "equipment")
                else -> BusinessMore(actor, language, onOpen = { id ->
                    if (id == "profile") profileOpen = true else moreScreen = id
                }, onSignOut = ::signOut)
            }
        }
    }
}
