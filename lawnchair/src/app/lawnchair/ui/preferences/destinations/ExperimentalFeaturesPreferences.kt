package app.lawnchair.ui.preferences.destinations

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.lawnchair.predictions.LawnchairPredictor
import app.lawnchair.predictions.PredictionMode
import app.lawnchair.predictions.AppUsageStore
import app.lawnchair.predictions.DismissedPredictionAppsStore
import app.lawnchair.preferences.getAdapter
import app.lawnchair.preferences.preferenceManager
import app.lawnchair.preferences2.preferenceManager2
import app.lawnchair.ui.preferences.LocalIsExpandedScreen
import app.lawnchair.ui.preferences.components.NavigationActionPreference
import app.lawnchair.ui.preferences.components.WallpaperAccessPermissionDialog
import app.lawnchair.ui.preferences.components.controls.ListPreference
import app.lawnchair.ui.preferences.components.controls.ListPreferenceEntry
import app.lawnchair.ui.preferences.components.controls.SliderPreference
import app.lawnchair.ui.preferences.components.controls.SwitchPreference
import app.lawnchair.ui.preferences.components.controls.WarningPreference
import app.lawnchair.ui.preferences.components.layout.PreferenceGroup
import app.lawnchair.ui.preferences.components.layout.PreferenceLayout
import app.lawnchair.ui.preferences.navigation.DismissedPredictionApps
import app.lawnchair.ui.preferences.navigation.GeneralIconShape
import app.lawnchair.util.FileAccessManager
import app.lawnchair.util.FileAccessState
import app.lawnchair.util.isGestureNavContractCompatible
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.util.MSDLPlayerWrapper
import com.android.systemui.shared.system.BlurUtils
import com.google.android.msdl.data.model.MSDLToken
import androidx.compose.ui.platform.LocalResources

@Composable
fun ExperimentalFeaturesPreferences(
    modifier: Modifier = Modifier,
) {
    val prefs = preferenceManager()
    val prefs2 = preferenceManager2()

    val mMSDLPlayerWrapper = MSDLPlayerWrapper.INSTANCE.get(LocalContext.current)
    PreferenceLayout(
        label = stringResource(id = R.string.experimental_features_label),
        backArrowVisible = !LocalIsExpandedScreen.current,
        modifier = modifier,
    ) {
        val enableWallpaperBlur = prefs.enableWallpaperBlur.getAdapter()
        val context = LocalContext.current
        val fileAccessManager = remember { FileAccessManager.getInstance(context) }
        val allFilesAccessState by fileAccessManager.allFilesAccessState.collectAsStateWithLifecycle()
        val wallpaperAccessState by fileAccessManager.wallpaperAccessState.collectAsStateWithLifecycle()
        val hasPermission = wallpaperAccessState != FileAccessState.Denied
        var showPermissionDialog by remember { mutableStateOf(false) }

        val folderIconShapeAdapter = prefs2.folderShape.getAdapter()
        val folderIconShapeSubtitle = iconShapeEntries(context)
            .firstOrNull { it.value == folderIconShapeAdapter.state.value }
            ?.label?.invoke()
            ?: stringResource(id = R.string.custom)

        PreferenceGroup(
            Modifier,
            stringResource(R.string.workspace_label),
        ) {
            Item {
                SwitchPreference(
                    adapter = prefs2.enableFontSelection.getAdapter(),
                    label = stringResource(id = R.string.font_picker_label),
                    description = stringResource(id = R.string.font_picker_description),
                )
            }
            Item {
                SwitchPreference(
                    adapter = prefs.workspaceIncreaseMaxGridSize.getAdapter(),
                    label = stringResource(id = R.string.workspace_increase_max_grid_size_label),
                    description = stringResource(id = R.string.workspace_increase_max_grid_size_description),
                )
            }
            Item {
                SwitchPreference(
                    adapter = prefs2.iconSwipeGestures.getAdapter(),
                    label = stringResource(R.string.icon_swipe_gestures),
                    description = stringResource(R.string.icon_swipe_gestures_description),
                )
            }
            Item {
                SwitchPreference(
                    adapter = prefs2.showDeckLayout.getAdapter(),
                    label = stringResource(R.string.show_deck_layout),
                    description = stringResource(R.string.show_deck_layout_description),
                )
            }
            Item {
                SwitchPreference(
                    checked = hasPermission && enableWallpaperBlur.state.value,
                    onCheckedChange = {
                        if (!hasPermission) {
                            showPermissionDialog = true
                        } else {
                            enableWallpaperBlur.onChange(it)
                        }
                    },
                    label = stringResource(id = R.string.wallpaper_blur),
                )
            }

            val canBlur = hasPermission && enableWallpaperBlur.state.value
            Item(
                "wallpaper_background_blur",
                canBlur,
            ) {
                SliderPreference(
                    label = stringResource(id = R.string.wallpaper_background_blur),
                    adapter = prefs.wallpaperBlur.getAdapter(),
                    step = 5,
                    valueRange = 0..100,
                    showUnit = "%",
                )
            }
            Item(
                "wallpaper_background_blur",
                canBlur,
            ) {
                SliderPreference(
                    label = stringResource(id = R.string.wallpaper_background_blur_factor),
                    adapter = prefs.wallpaperBlurFactorThreshold.getAdapter(),
                    step = 1F,
                    valueRange = 0F..10F,
                )
            }
        }
        if (showPermissionDialog) {
            WallpaperAccessPermissionDialog(
                managedFilesChecked = allFilesAccessState != FileAccessState.Denied,
                onDismiss = {
                    showPermissionDialog = false
                },
                onPermissionRequest = { fileAccessManager.refresh() },
            )
        }
        LifecycleResumeEffect(Unit) {
            showPermissionDialog = false
            fileAccessManager.refresh()
            onPauseOrDispose { }
        }

        val alwaysReloadIconsAdapter = prefs2.alwaysReloadIcons.getAdapter()
        val enableGncAdapter = prefs.enableGnc.getAdapter()

        val predictionModeAdapter = prefs2.predictionMode.getAdapter()
        val weightedUsageStatsAdapter = prefs2.lawnchairPredictorUseWeightedUsageStats.getAdapter()
        val recordPredictionTapsAdapter = prefs2.lawnchairPredictorRecordPredictionTaps.getAdapter()
        val isLawnchairPredictorSelected = predictionModeAdapter.state.value == LawnchairPredictor
        val predictionPrefs = remember { AppUsageStore.getPrefs(context) }
        var dismissedPredictionAppsCount by remember {
            mutableIntStateOf(DismissedPredictionAppsStore.getDismissedApps(context).size)
        }
        val hasUsageStatsPermission =
            context.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) ==
                PackageManager.PERMISSION_GRANTED

        DisposableEffect(predictionPrefs, context) {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == DismissedPredictionAppsStore.STORE_NAME) {
                    dismissedPredictionAppsCount = DismissedPredictionAppsStore.getDismissedApps(context).size
                }
            }
            predictionPrefs.registerOnSharedPreferenceChangeListener(listener)
            onDispose { predictionPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }

        PreferenceGroup(
            Modifier,
            stringResource(R.string.app_prediction_label),
        ) {
            Item {
                ListPreference(
                    adapter = predictionModeAdapter,
                    entries = PredictionMode.values().map { mode ->
                        ListPreferenceEntry(
                            value = mode,
                            label = { stringResource(mode.nameResourceId) },
                            enabled = mode.isAvailable(context),
                        )
                    },
                    label = stringResource(R.string.prediction_mode_label),
                )
            }
            Item {
                SwitchPreference(
                    adapter = weightedUsageStatsAdapter,
                    label = stringResource(R.string.prediction_weighted_usage_stats_label),
                    description = if (hasUsageStatsPermission) {
                        stringResource(R.string.prediction_weighted_usage_stats_description)
                    } else {
                        stringResource(R.string.prediction_weighted_usage_stats_permission_description)
                    },
                    enabled = isLawnchairPredictorSelected,
                )
            }
            Item {
                SwitchPreference(
                    adapter = recordPredictionTapsAdapter,
                    label = stringResource(R.string.prediction_record_taps_label),
                    description = stringResource(R.string.prediction_record_taps_description),
                    enabled = isLawnchairPredictorSelected,
                )
            }
            Item {
                NavigationActionPreference(
                    label = stringResource(R.string.dismissed_prediction_apps_label),
                    destination = DismissedPredictionApps,
                    subtitle = LocalResources.current.getQuantityString(
                        R.plurals.apps_count,
                        dismissedPredictionAppsCount,
                        dismissedPredictionAppsCount,
                    ),
                )
            }
        }

        PreferenceGroup(
            Modifier,
            stringResource(R.string.internal_label),
            stringResource(R.string.internal_description),
        ) {
            Item {
                SwitchPreference(
                    adapter = alwaysReloadIconsAdapter,
                    label = stringResource(id = R.string.always_reload_icons_label),
                    description = stringResource(id = R.string.always_reload_icons_description),
                )
            }
            Item(
                "always_reload_icons_warning",
                alwaysReloadIconsAdapter.state.value,
            ) {
                WarningPreference(stringResource(R.string.always_reload_icons_warning))
            }

            Item {
                SwitchPreference(
                    adapter = enableGncAdapter,
                    label = stringResource(id = R.string.gesturenavcontract_label),
                    description = stringResource(id = R.string.gesturenavcontract_description),
                    enabled = Utilities.ATLEAST_Q,
                )
            }
            Item(
                "gesturenavcontract_warning",
                enableGncAdapter.state.value && !isGestureNavContractCompatible,
            ) {
                WarningPreference(stringResource(R.string.gesturenavcontract_warning_incompatibility))
            }
        }
    }
}
