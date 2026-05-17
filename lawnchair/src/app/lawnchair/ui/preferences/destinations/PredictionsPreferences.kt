package app.lawnchair.ui.preferences.destinations

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.lawnchair.predictions.AppUsageStore
import app.lawnchair.predictions.DismissedPredictionAppsStore
import app.lawnchair.predictions.LawnchairPredictor
import app.lawnchair.predictions.NoPredictor
import app.lawnchair.predictions.PredictionMode
import app.lawnchair.preferences.getAdapter
import app.lawnchair.preferences2.preferenceManager2
import app.lawnchair.ui.preferences.LocalIsExpandedScreen
import app.lawnchair.ui.preferences.components.AnimatedVectorPreview
import app.lawnchair.ui.preferences.components.NavigationActionPreference
import app.lawnchair.ui.preferences.components.controls.ListPreference
import app.lawnchair.ui.preferences.components.controls.ListPreferenceEntry
import app.lawnchair.ui.preferences.components.controls.MainSwitchPreference
import app.lawnchair.ui.preferences.components.controls.SwitchPreference
import app.lawnchair.ui.preferences.components.layout.PreferenceGroup
import app.lawnchair.ui.preferences.components.layout.PreferenceLayout
import app.lawnchair.ui.preferences.navigation.DismissedPredictionApps
import com.android.launcher3.R

@Composable
fun PredictionsPreferences(
    modifier: Modifier = Modifier,
) {
    PreferenceLayout(
        label = stringResource(id = R.string.predictions_label),
        backArrowVisible = !LocalIsExpandedScreen.current,
        modifier = modifier,
    ) {
        PredictionsFeatures()
    }
}

@Composable
private fun PredictionsFeatures(
    modifier: Modifier = Modifier,
) {
    AppPredictionsFeature(modifier = modifier)
}

@Composable
private fun AppPredictionsFeature(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val prefs2 = preferenceManager2()

    val enableGlobalPredictionAdapter = prefs2.enableGlobalPrediction.getAdapter()
    val predictionModeAdapter = prefs2.predictionMode.getAdapter()
    val weightedUsageStatsAdapter = prefs2.lawnchairPredictorUseWeightedUsageStats.getAdapter()
    val isLawnchairPredictorSelected = predictionModeAdapter.state.value == LawnchairPredictor
    val hasUsageStatsPermission =
        context.checkSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) ==
            PackageManager.PERMISSION_GRANTED
    val predictionModeEntries = rememberPredictionModeEntries(context)
    val dismissedPredictionAppsCount = rememberDismissedPredictionAppsCount()
    val weightedUsageStatsDescription = stringResource(
        if (hasUsageStatsPermission) {
            R.string.prediction_weighted_usage_stats_description
        } else {
            R.string.prediction_weighted_usage_stats_permission_description
        },
    )
    val dismissedPredictionAppsSubtitle = resources.getQuantityString(
        R.plurals.apps_count,
        dismissedPredictionAppsCount,
        dismissedPredictionAppsCount,
    )

    MainSwitchPreference(
        adapter = enableGlobalPredictionAdapter,
        label = stringResource(R.string.predictions_label),
        modifier = modifier,
    ) {
        AppPredictionsPreview(predictionMode = predictionModeAdapter.state.value)
        PreferenceGroup {
            Item {
                ListPreference(
                    adapter = predictionModeAdapter,
                    entries = predictionModeEntries,
                    label = stringResource(R.string.prediction_mode_label),
                )
            }
            Item(visible = isLawnchairPredictorSelected) {
                SwitchPreference(
                    adapter = weightedUsageStatsAdapter,
                    label = stringResource(R.string.prediction_weighted_usage_stats_label),
                    description = weightedUsageStatsDescription,
                    enabled = isLawnchairPredictorSelected && hasUsageStatsPermission,
                )
            }
            Item(visible = isLawnchairPredictorSelected) {
                NavigationActionPreference(
                    label = stringResource(R.string.dismissed_prediction_apps_label),
                    destination = DismissedPredictionApps,
                    subtitle = dismissedPredictionAppsSubtitle,
                )
            }
        }
    }
}

@Composable
private fun rememberPredictionModeEntries(context: Context): List<ListPreferenceEntry<PredictionMode>> {
    return remember(context) {
        PredictionMode.values().map { mode ->
            ListPreferenceEntry(
                value = mode,
                label = { stringResource(mode.nameResourceId) },
                enabled = mode.isAvailable(context),
            )
        }
    }
}

@Composable
private fun rememberDismissedPredictionAppsCount(): Int {
    val context = LocalContext.current
    val predictionPrefs = remember { AppUsageStore.getPrefs(context) }
    val dismissedAppsStore = remember {
        DismissedPredictionAppsStore(predictionPrefs, DismissedPredictionAppsStore.DISMISS_STORE_NAME)
    }
    var dismissedPredictionAppsCount by remember {
        mutableIntStateOf(dismissedAppsStore.getDismissedApps().size)
    }

    DisposableEffect(predictionPrefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == DismissedPredictionAppsStore.DISMISS_STORE_NAME) {
                dismissedPredictionAppsCount = dismissedAppsStore.getDismissedApps().size
            }
        }
        predictionPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { predictionPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    return dismissedPredictionAppsCount
}

@Composable
private fun AppPredictionsPreview(
    predictionMode: PredictionMode,
    modifier: Modifier = Modifier,
) {
    val bottomPadding = 78.dp // eyeballing workarounds
    val scaleFactor = 1.7f // eyeballing workarounds

    PreferenceGroup(
        heading = stringResource(id = R.string.app_predictions_label),
        modifier = modifier,
    ) {
        Item {
            AnimatedVectorPreview(
                animatedVectorResId = R.drawable.predictions_preview,
                isPlaying = predictionMode != NoPredictor,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(
                        scaleX = scaleFactor,
                        scaleY = scaleFactor,
                    )
                    .padding(bottom = bottomPadding),
            )
        }
    }
}

