package app.lawnchair.ui.preferences.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import app.lawnchair.backup.ui.CreateBackupScreen
import app.lawnchair.backup.ui.restoreBackupGraph
import app.lawnchair.preferences.BasePreferenceManager
import app.lawnchair.preferences.preferenceManager
import app.lawnchair.ui.preferences.LocalIsExpandedScreen
import app.lawnchair.ui.preferences.about.About
import app.lawnchair.ui.preferences.about.acknowledgements.Acknowledgements
import app.lawnchair.ui.preferences.components.colorpreference.ColorPreferenceModelList
import app.lawnchair.ui.preferences.components.colorpreference.ColorSelection
import app.lawnchair.ui.preferences.components.search.SearchProviderPreferenceScreen
import app.lawnchair.ui.preferences.destinations.AppDrawerFoldersPreference
import app.lawnchair.ui.preferences.destinations.AppDrawerPreferences
import app.lawnchair.ui.preferences.destinations.BackupAndRestorePreference
import app.lawnchair.ui.preferences.destinations.CustomIconShapePreference
import app.lawnchair.ui.preferences.destinations.DebugMenuPreferences
import app.lawnchair.ui.preferences.destinations.DockPreferences
import app.lawnchair.ui.preferences.destinations.DummyPreference
import app.lawnchair.ui.preferences.destinations.ExperimentalFeaturesPreferences
import app.lawnchair.ui.preferences.destinations.FeatureFlagsPreference
import app.lawnchair.ui.preferences.destinations.FolderPreferences
import app.lawnchair.ui.preferences.destinations.FontSelection
import app.lawnchair.ui.preferences.destinations.GeneralPreferences
import app.lawnchair.ui.preferences.destinations.GesturePreferences
import app.lawnchair.ui.preferences.destinations.HiddenAppsPreferences
import app.lawnchair.ui.preferences.destinations.HomeScreenGridPreferences
import app.lawnchair.ui.preferences.destinations.HomeScreenPreferences
import app.lawnchair.ui.preferences.destinations.IconPackPreferences
import app.lawnchair.ui.preferences.destinations.IconPickerPreference
import app.lawnchair.ui.preferences.destinations.IconShapePreference
import app.lawnchair.ui.preferences.destinations.LauncherPopupPreference
import app.lawnchair.ui.preferences.destinations.PickAppForGesture
import app.lawnchair.ui.preferences.destinations.PreferencesDashboard
import app.lawnchair.ui.preferences.destinations.QuickstepPreferences
import app.lawnchair.ui.preferences.destinations.SearchPreferences
import app.lawnchair.ui.preferences.destinations.SearchProviderPreferences
import app.lawnchair.ui.preferences.destinations.SelectAppsForDrawerFolder
import app.lawnchair.ui.preferences.destinations.SelectIconPreference
import app.lawnchair.ui.preferences.destinations.ShapePreference
import app.lawnchair.ui.preferences.destinations.SmartspacePreferences
import com.android.launcher3.util.ComponentKey
import soup.compose.material.motion.animation.materialSharedAxisXIn
import soup.compose.material.motion.animation.materialSharedAxisXOut
import soup.compose.material.motion.animation.rememberSlideDistance

enum class PreferenceDeepLink(val path: String) {
    General("general"),
    IconPack("iconpack"),
    GeneralIconShapeCreator("general-icon-shape-creator"),
    HomeScreen("home-screen"),
    HomeScreenGrid("home-screen-grid"),
    HomeScreenPopupEditor("home-screen-popup-editor"),
    Dock("dock"),
    DockSearchProvider("dock-search-provider"),
    Smartspace("smartspace"),
    AppDrawer("app-drawer"),
    AppDrawerHiddenApps("app-drawer-hidden-apps"),
    AppDrawerFolder("app-drawer-folder"),
    Search("search"),
    SearchProvider("search-provider"),
    Folders("folders"),
    Gestures("gestures"),
    Quickstep("quickstep"),
    BackupAndRestore("backup-restore"),
    About("about"),
    AboutLicenses("about-licenses"),
    ExperimentalFeatures("experimental-features"),
    CreateBackup("create-backup"),
    ;

    val uri: String get() = "$BASE://$SETTINGS/$path"

    companion object {
        const val BASE = "lawnchair"
        const val SETTINGS = "settings"
    }
}

@Composable
fun PreferenceNavigation(
    navController: NavHostController,
    startDestination: PreferenceRoute,
    intent: Intent? = null,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val slideDistance = rememberSlideDistance()

    LaunchedEffect(intent) {
        intent?.let { navController.handleDeepLink(it) }
    }

    // TODO: navigate to nav3: https://developer.android.com/guide/navigation/navigation-3
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { materialSharedAxisXIn(!isRtl, slideDistance) },
        exitTransition = { materialSharedAxisXOut(!isRtl, slideDistance) },
        popEnterTransition = { materialSharedAxisXIn(isRtl, slideDistance) },
        popExitTransition = { materialSharedAxisXOut(isRtl, slideDistance) },
    ) {
        composable<Root> {
            val isExpandedScreen = LocalIsExpandedScreen.current

            PreferencesDashboard(
                currentRoute = Root,
                onNavigate = {
                    navController.navigate(it)
                },
            )

            LaunchedEffect(isExpandedScreen) {
                if (isExpandedScreen) {
                    navController.navigate(General) {
                        launchSingleTop = true
                        popUpTo(navController.graph.id)
                    }
                }
            }
        }
        composable<Dummy> {
            DummyPreference()
        }

        composable<General>(
            deepLinks = listOf(
                navDeepLink<General>(basePath = PreferenceDeepLink.General.uri),
            ),
        ) { GeneralPreferences() }
        composable<GeneralFontSelection> { backStackEntry ->
            val route: GeneralFontSelection = backStackEntry.toRoute()
            val pref = preferenceManager().prefsMap[route.prefKey]
                as? BasePreferenceManager.FontPref ?: return@composable
            FontSelection(pref)
        }
        composable<GeneralIconPack>(
            deepLinks = listOf(
                navDeepLink<GeneralIconPack>(basePath = PreferenceDeepLink.IconPack.uri),
            ),
        ) { IconPackPreferences() }
        composable<GeneralIconShape> { backStackEntry ->
            val route: GeneralIconShape = backStackEntry.toRoute()
            ShapePreference(currentTab = route.selectedId)
        }
        composable<GeneralCustomIconShapeCreator>(
            deepLinks = listOf(
                navDeepLink<GeneralCustomIconShapeCreator>(basePath = PreferenceDeepLink.GeneralIconShapeCreator.uri),
            ),
        ) { CustomIconShapePreference() }

        composable<HomeScreen>(
            deepLinks = listOf(
                navDeepLink<HomeScreen>(basePath = PreferenceDeepLink.HomeScreen.uri),
            ),
        ) { HomeScreenPreferences() }
        composable<HomeScreenGrid>(
            deepLinks = listOf(
                navDeepLink<HomeScreenGrid>(basePath = PreferenceDeepLink.HomeScreenGrid.uri),
            ),
        ) { HomeScreenGridPreferences() }
        composable<HomeScreenPopupEditor>(
            deepLinks = listOf(
                navDeepLink<HomeScreenPopupEditor>(basePath = PreferenceDeepLink.HomeScreenPopupEditor.uri),
            ),
        ) { LauncherPopupPreference() }

        composable<Dock>(
            deepLinks = listOf(
                navDeepLink<Dock>(basePath = PreferenceDeepLink.Dock.uri),
            ),
        ) { DockPreferences() }
        composable<DockSearchProvider>(
            deepLinks = listOf(
                navDeepLink<DockSearchProvider>(basePath = PreferenceDeepLink.DockSearchProvider.uri),
            ),
        ) { SearchProviderPreferences() }

        composable<Smartspace>(
            deepLinks = listOf(
                navDeepLink<Smartspace>(basePath = PreferenceDeepLink.Smartspace.uri),
            ),
        ) { SmartspacePreferences(fromWidget = false) }
        composable<SmartspaceWidget> { SmartspacePreferences(fromWidget = true) }

        composable<AppDrawer>(
            deepLinks = listOf(
                navDeepLink<AppDrawer>(basePath = PreferenceDeepLink.AppDrawer.uri),
            ),
        ) { AppDrawerPreferences() }
        composable<AppDrawerHiddenApps>(
            deepLinks = listOf(
                navDeepLink<AppDrawerHiddenApps>(basePath = PreferenceDeepLink.AppDrawerHiddenApps.uri),
            ),
        ) { HiddenAppsPreferences() }
        composable<AppDrawerAppListToFolder> { backStackEntry ->
            val args = backStackEntry.arguments!!
            val folderInfoId = args.getInt("id")
            SelectAppsForDrawerFolder(folderInfoId)
        }
        composable<AppDrawerFolder>(
            deepLinks = listOf(
                navDeepLink<AppDrawerFolder>(basePath = PreferenceDeepLink.AppDrawerFolder.uri),
            ),
        ) { AppDrawerFoldersPreference() }

        composable<Search>(
            deepLinks = listOf(
                navDeepLink<Search>(basePath = PreferenceDeepLink.Search.uri),
            ),
        ) { backStackEntry ->
            val route: Search = backStackEntry.toRoute()
            SearchPreferences(currentTab = route.selectedId)
        }
        composable<SearchProviderPreference>(
            deepLinks = listOf(
                navDeepLink<SearchProviderPreference>(basePath = PreferenceDeepLink.SearchProvider.uri),
            ),
        ) { backStackEntry ->
            val route: SearchProviderPreference = backStackEntry.toRoute()
            SearchProviderPreferenceScreen(route.id)
        }

        composable<Folders>(
            deepLinks = listOf(
                navDeepLink<Folders>(basePath = PreferenceDeepLink.Folders.uri),
            ),
        ) { FolderPreferences() }

        composable<Gestures>(
            deepLinks = listOf(
                navDeepLink<Gestures>(basePath = PreferenceDeepLink.Gestures.uri),
            ),
        ) { GesturePreferences() }
        composable<GesturesPickApp> { PickAppForGesture() }

        composable<Quickstep>(
            deepLinks = listOf(
                navDeepLink<Quickstep>(basePath = PreferenceDeepLink.Quickstep.uri),
            ),
        ) { QuickstepPreferences() }
        composable<BackupAndRestore>(
            deepLinks = listOf(
                navDeepLink<BackupAndRestore>(basePath = PreferenceDeepLink.BackupAndRestore.uri),
            ),
        ) { BackupAndRestorePreference() }

        composable<About>(
            deepLinks = listOf(
                navDeepLink<About>(basePath = PreferenceDeepLink.About.uri),
            ),
        ) { About() }
        composable<AboutLicenses>(
            deepLinks = listOf(
                navDeepLink<AboutLicenses>(basePath = PreferenceDeepLink.AboutLicenses.uri),
            ),
        ) { Acknowledgements() }

        composable<DebugMenu> { DebugMenuPreferences() }
        composable<FeatureFlags> { FeatureFlagsPreference() }

        composable<SelectIcon> { backStackEntry ->
            val args: SelectIcon = backStackEntry.toRoute()
            val componentKey = args.componentKey
            val key = ComponentKey.fromString(componentKey)!!
            SelectIconPreference(key)
        }
        composable<IconPicker> { backStackEntry ->
            val args: IconPicker = backStackEntry.toRoute()
            IconPickerPreference(packageName = args.packageName)
        }

        composable<ExperimentalFeatures>(
            deepLinks = listOf(
                navDeepLink<ExperimentalFeatures>(basePath = PreferenceDeepLink.ExperimentalFeatures.uri),
            ),
        ) { ExperimentalFeaturesPreferences() }
        composable<ColorSelection> { backStackEntry ->
            val screen: ColorSelection = backStackEntry.toRoute()
            val modelList = ColorPreferenceModelList.INSTANCE.get(LocalContext.current)
            val model = modelList[screen.prefKey]
            ColorSelection(
                label = stringResource(id = model.labelRes),
                preference = model.prefObject,
                dynamicEntries = model.dynamicEntries,
            )
        }

        composable<CreateBackup>(
            deepLinks = listOf(
                navDeepLink<CreateBackup>(basePath = PreferenceDeepLink.CreateBackup.uri),
            ),
        ) { CreateBackupScreen(viewModel()) }

        restoreBackupGraph()
    }
}
