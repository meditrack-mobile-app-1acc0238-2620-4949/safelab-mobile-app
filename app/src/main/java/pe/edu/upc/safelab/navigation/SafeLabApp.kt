package pe.edu.upc.safelab.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import pe.edu.upc.safelab.shared.infrastructure.http.SafeLabApi
import pe.edu.upc.safelab.shared.infrastructure.http.safeLabErrorMessage
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model.validateProfile
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
    val apiScope = rememberCoroutineScope()
    var language by rememberSaveable { mutableStateOf("EN") }
    var selectedTab by rememberSaveable { mutableStateOf(MobileTab.HOME.name) }
    var moreScreen by rememberSaveable { mutableStateOf<String?>(null) }
    var profileOpen by remember { mutableStateOf(false) }
    var platformBillingOpen by remember { mutableStateOf(false) }
    var syncInProgress by remember { mutableStateOf(false) }
    var syncError by remember { mutableStateOf<String?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val version = MultiTenantStore.version // Observa cambios remotos en TODOS los roles y BC.

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
        // Navigation re-fetches the live snapshot; no logout, timer or WebSocket needed.
        LaunchedEffect(loggedUser.id, selectedTab, moreScreen, profileOpen, platformBillingOpen) {
            if (!SafeLabApi.authenticated) return@LaunchedEffect
            syncInProgress = true
            try { if (MultiTenantStore.refreshFromBackendIfIdle()) syncError = null }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (ex: Exception) { syncError = safeLabErrorMessage(ex, language) }
            finally { syncInProgress = false }
        }
        // Return from another app or background is an additional refresh trigger.
        DisposableEffect(lifecycleOwner, loggedUser.id) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME && SafeLabApi.authenticated) {
                    apiScope.launch {
                        syncInProgress = true
                        try { if (MultiTenantStore.refreshFromBackendIfIdle()) syncError = null }
                        catch (cancelled: CancellationException) { throw cancelled }
                        catch (ex: Exception) { syncError = safeLabErrorMessage(ex, language) }
                        finally { syncInProgress = false }
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
        // Refresh de seguridad mientras la app está en primer plano: cubre listas, paneles,
        // perfil, suscripción y pantallas que permanecen abiertas sin cambiar de módulo.
        // No se mantiene conexión WebSocket ni se descarga nada en segundo plano.
        LaunchedEffect(loggedUser.id, lifecycleOwner) {
            while (currentCoroutineContext().isActive) {
                delay(15_000L)
                if (SafeLabApi.authenticated &&
                    lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    try { if (MultiTenantStore.refreshFromBackendIfIdle()) syncError = null }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (ex: Exception) { syncError = safeLabErrorMessage(ex, language) }
                }
            }
        }
        // El perfil no debe conservar un snapshot separado del usuario en PostgreSQL.
        LaunchedEffect(loggedUser.id, version) {
            if (!SafeLabApi.isMock) UserProfilesRepository.syncFromBackend(loggedUser.id)
        }
        val actor = MultiTenantStore.user(loggedUser.id) ?: return@CompositionLocalProvider
        val role = actor.optString("role")
        val isPlatformAdmin = role == "platformAdmin"
        val isManager = role == "organizationAdmin"
        val profile = UserProfilesRepository.ensureFor(loggedUser)

        fun signOut() {
            // Limpiar primero el token para no bloquear el cierre por DNS o timeout.
            // El token anterior se revoca aparte, sin borrar una nueva sesión.
            val previousToken = SafeLabApi.clearLocalSession()
            user = null; selectedTab = MobileTab.HOME.name
            moreScreen = null; profileOpen = false; platformBillingOpen = false
            syncError = null; syncInProgress = false
            if (previousToken != null) apiScope.launch {
                try { SafeLabApi.revokeSession(previousToken) }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { /* El cierre local ya está completado */ }
            }
        }
        fun back() {
            when {
                platformBillingOpen -> platformBillingOpen = false
                profileOpen -> profileOpen = false
                moreScreen != null -> moreScreen = null
                selectedTab != MobileTab.HOME.name -> selectedTab = MobileTab.HOME.name
            }
        }
        suspend fun saveProfile(changes: pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model.ProfileChanges): ProfileSaveResult {
            val validation = validateProfile(changes)
            if (validation != ProfileSaveResult.SUCCESS) return validation
            if (!SafeLabApi.isMock) {
                if (!changes.email.equals(profile.email, ignoreCase = true)) return ProfileSaveResult.EMAIL_CHANGE_UNSUPPORTED
                return try {
                    val update = JSONObject().put("firstName",changes.firstNames.trim())
                        .put("lastName",changes.lastNames.trim()).put("phone",changes.phone.trim())
                        .put("city",changes.city.trim()).put("jobTitle",changes.jobTitle.trim())
                    val age = changes.age.trim().toIntOrNull()
                    if (age != null) update.put("age", age)
                    SafeLabApi.request("PATCH", "/userprofiles/me", update)
                    MultiTenantStore.refreshFromBackend()
                    UserProfilesRepository.save(profile.userId, changes) { true }
                } catch (_: Exception) { ProfileSaveResult.API_ERROR }
            }
            return UserProfilesRepository.save(profile.userId, changes) { email ->
                LocalIdentityAccessRepository().isEmailAvailable(profile.userId, email)
            }
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
            Column {
                if (syncInProgress) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (syncError != null) {
                    TextButton(onClick = {
                        apiScope.launch {
                            syncInProgress = true
                            try { MultiTenantStore.refreshFromBackend(); syncError = null }
                            catch (cancelled: CancellationException) { throw cancelled }
                            catch (ex: Exception) { syncError = safeLabErrorMessage(ex, language) }
                            finally { syncInProgress = false }
                        }
                    }) { Text(tr(language, "Sync failed — retry", "Error de sincronización — reintentar") + ": " + syncError.orEmpty()) }
                }
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
}
