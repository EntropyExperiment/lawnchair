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
                navDeepLink<General>(basePath = PreferenceDeepLink.General.basePath),
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
                navDeepLink<GeneralIconPack>(basePath = PreferenceDeepLink.GeneralIconPack.basePath),
            ),
        ) { IconPackPreferences() }
        composable<GeneralIconShape> { backStackEntry ->
            val route: GeneralIconShape = backStackEntry.toRoute()
            ShapePreference(currentTab = route.selectedId)
        }
        composable<GeneralCustomIconShapeCreator>(
            deepLinks = listOf(
                navDeepLink<GeneralCustomIconShapeCreator>(basePath = PreferenceDeepLink.GeneralCustomIconShapeCreator.basePath),
            ),
        ) { CustomIconShapePreference() }

        composable<HomeScreen>(
            deepLinks = listOf(
                navDeepLink<HomeScreen>(basePath = PreferenceDeepLink.HomeScreen.basePath),
            ),
        ) { HomeScreenPreferences() }
        composable<HomeScreenGrid>(
            deepLinks = listOf(
                navDeepLink<HomeScreenGrid>(basePath = PreferenceDeepLink.HomeScreenGrid.basePath),
            ),
        ) { HomeScreenGridPreferences() }
        composable<HomeScreenPopupEditor>(
            deepLinks = listOf(
                navDeepLink<HomeScreenPopupEditor>(basePath = PreferenceDeepLink.HomeScreenPopupEditor.basePath),
            ),
        ) { LauncherPopupPreference() }

        composable<Dock>(
            deepLinks = listOf(
                navDeepLink<Dock>(basePath = PreferenceDeepLink.Dock.basePath),
            ),
        ) { DockPreferences() }
        composable<DockSearchProvider>(
            deepLinks = listOf(
                navDeepLink<DockSearchProvider>(basePath = PreferenceDeepLink.DockSearchProvider.basePath),
            ),
        ) { SearchProviderPreferences() }

        composable<Smartspace>(
            deepLinks = listOf(
                navDeepLink<Smartspace>(basePath = PreferenceDeepLink.Smartspace.basePath),
            ),
        ) { SmartspacePreferences(fromWidget = false) }
        composable<SmartspaceWidget> { SmartspacePreferences(fromWidget = true) }

        composable<AppDrawer>(
            deepLinks = listOf(
                navDeepLink<AppDrawer>(basePath = PreferenceDeepLink.AppDrawer.basePath),
            ),
        ) { AppDrawerPreferences() }
        composable<AppDrawerHiddenApps>(
            deepLinks = listOf(
                navDeepLink<AppDrawerHiddenApps>(basePath = PreferenceDeepLink.AppDrawerHiddenApps.basePath),
            ),
        ) { HiddenAppsPreferences() }
        composable<AppDrawerAppListToFolder> { backStackEntry ->
            val args = backStackEntry.arguments!!
            val folderInfoId = args.getInt("id")
            SelectAppsForDrawerFolder(folderInfoId)
        }
        composable<AppDrawerFolder>(
            deepLinks = listOf(
                navDeepLink<AppDrawerFolder>(basePath = PreferenceDeepLink.AppDrawerFolder.basePath),
            ),
        ) { AppDrawerFoldersPreference() }

        composable<Search>(
            deepLinks = listOf(
                navDeepLink<Search>(basePath = PreferenceDeepLink.Search.basePath),
            ),
        ) { backStackEntry ->
            val route: Search = backStackEntry.toRoute()
            SearchPreferences(currentTab = route.selectedId)
        }
        composable<SearchProviderPreference>(
            deepLinks = listOf(
                navDeepLink<SearchProviderPreference>(basePath = PreferenceDeepLink.SearchProviderPreference.basePath),
            ),
        ) { backStackEntry ->
            val route: SearchProviderPreference = backStackEntry.toRoute()
            SearchProviderPreferenceScreen(route.id)
        }

        composable<Folders>(
            deepLinks = listOf(
                navDeepLink<Folders>(basePath = PreferenceDeepLink.Folders.basePath),
            ),
        ) { FolderPreferences() }

        composable<Gestures>(
            deepLinks = listOf(
                navDeepLink<Gestures>(basePath = PreferenceDeepLink.Gestures.basePath),
            ),
        ) { GesturePreferences() }
        composable<GesturesPickApp> { PickAppForGesture() }

        composable<Quickstep>(
            deepLinks = listOf(
                navDeepLink<Quickstep>(basePath = PreferenceDeepLink.Quickstep.basePath),
            ),
        ) { QuickstepPreferences() }
        composable<BackupAndRestore>(
            deepLinks = listOf(
                navDeepLink<BackupAndRestore>(basePath = PreferenceDeepLink.BackupAndRestore.basePath),
            ),
        ) { BackupAndRestorePreference() }

        composable<About>(
            deepLinks = listOf(
                navDeepLink<About>(basePath = PreferenceDeepLink.About.basePath),
            ),
        ) { About() }
        composable<AboutLicenses>(
            deepLinks = listOf(
                navDeepLink<AboutLicenses>(basePath = PreferenceDeepLink.AboutLicenses.basePath),
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
                navDeepLink<ExperimentalFeatures>(basePath = PreferenceDeepLink.ExperimentalFeatures.basePath),
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
                navDeepLink<CreateBackup>(basePath = PreferenceDeepLink.CreateBackup.basePath),
            ),
        ) { CreateBackupScreen(viewModel()) }

        restoreBackupGraph()
    }
}
