package app.lawnchair.ui.preferences.destinations

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Process
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import app.lawnchair.predictions.AppUsageStore
import app.lawnchair.predictions.DismissedPredictionAppsStore
import app.lawnchair.predictions.LawnchairPredictor
import app.lawnchair.predictions.NoPredictor
import app.lawnchair.predictions.PredictionMode
import app.lawnchair.predictions.SystemPredictor
import app.lawnchair.preferences.PreferenceAdapter
import app.lawnchair.preferences.getAdapter
import app.lawnchair.preferences2.PreferenceManager2
import app.lawnchair.preferences2.preferenceManager2
import app.lawnchair.ui.preferences.LocalIsExpandedScreen
import app.lawnchair.ui.preferences.components.NavigationActionPreference
import app.lawnchair.ui.preferences.components.controls.ClickablePreference
import app.lawnchair.ui.preferences.components.controls.ListPreference
import app.lawnchair.ui.preferences.components.controls.ListPreferenceEntry
import app.lawnchair.ui.preferences.components.controls.MainSwitchPreference
import app.lawnchair.ui.preferences.components.controls.SwitchPreference
import app.lawnchair.ui.preferences.components.layout.PreferenceGroup
import app.lawnchair.ui.preferences.components.layout.PreferenceGroupScope
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
        val context = LocalContext.current
        val prefs2 = preferenceManager2()
        val enableGlobalPredictionAdapter = prefs2.enableGlobalPrediction.getAdapter()

        MainSwitchPreference(
            adapter = enableGlobalPredictionAdapter,
            label = stringResource(R.string.global_predictions_label),
        ) {
            AppPredictionsFeature(context, prefs2)
        }
    }
}

@Composable
private fun AppPredictionsFeature(
    context: Context,
    prefs2: PreferenceManager2,
) {
    val resources = LocalResources.current
    val appOps = context.getSystemService(AppOpsManager::class.java)

    val predictionModeAdapter = prefs2.predictionMode.getAdapter()
    val weightedUsageStatsAdapter = prefs2.lawnchairPredictorUseWeightedUsageStats.getAdapter()
    val hasUsageStatsPermission = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName,
    ) == AppOpsManager.MODE_ALLOWED
    val predictionModeEntries = rememberPredictionModeEntries(context)
    val dismissedPredictionAppsCount = rememberDismissedPredictionAppsCount(context)
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

    PreferenceGroup(
        heading = stringResource(R.string.app_predictions_label),
    ) {
        Item {
            ListPreference(
                adapter = predictionModeAdapter,
                entries = predictionModeEntries,
                label = stringResource(R.string.prediction_mode_label),
            )
        }
        when (predictionModeAdapter.state.value) {
            SystemPredictor -> SystemSuggestionsPreference()

            LawnchairPredictor -> LawnchairPredictionSettings(
                weightedUsageStatsAdapter = weightedUsageStatsAdapter,
                weightedUsageStatsDescription = weightedUsageStatsDescription,
                hasUsageStatsPermission = hasUsageStatsPermission,
                dismissedPredictionAppsSubtitle = dismissedPredictionAppsSubtitle,
            )

            NoPredictor -> Unit
        }
    }
}

@Composable
private fun PreferenceGroupScope.LawnchairPredictionSettings(
    weightedUsageStatsAdapter: PreferenceAdapter<Boolean>,
    weightedUsageStatsDescription: String,
    hasUsageStatsPermission: Boolean,
    dismissedPredictionAppsSubtitle: String,
) {
    Item {
        SwitchPreference(
            adapter = weightedUsageStatsAdapter,
            label = stringResource(R.string.prediction_weighted_usage_stats_label),
            description = weightedUsageStatsDescription,
            enabled = hasUsageStatsPermission,
        )
    }
    Item {
        NavigationActionPreference(
            label = stringResource(R.string.dismissed_prediction_apps_label),
            destination = DismissedPredictionApps,
            subtitle = dismissedPredictionAppsSubtitle,
        )
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
private fun rememberDismissedPredictionAppsCount(context: Context): Int {
    val predictionPrefs = remember { AppUsageStore.getPrefs(context) }
    val dismissedAppsStore = remember {
        DismissedPredictionAppsStore(
            predictionPrefs,
            DismissedPredictionAppsStore.DISMISS_STORE_NAME,
        )
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

@SuppressLint("WrongConstant")
@Composable
fun PreferenceGroupScope.SystemSuggestionsPreference() {
    val context = LocalContext.current
    val intent = Intent("android.settings.ACTION_CONTENT_SUGGESTIONS_SETTINGS")
    val hasPkgUsagePermission = context.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
    val canResolveToSuggestionPreference = context.packageManager.resolveActivity(intent, 0) != null
    val suggestionSettingsAvailable = hasPkgUsagePermission && canResolveToSuggestionPreference

    if (suggestionSettingsAvailable) {
        Item {
            ClickablePreference(
                label = stringResource(id = R.string.suggestion_pref_screen_title),
                onClick = {
                    context.startActivity(intent)
                },
            )
        }
    }
}
