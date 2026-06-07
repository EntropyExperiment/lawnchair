package app.lawnchair.ui.preferences.destinations

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.lawnchair.predictions.AppUsageStore
import app.lawnchair.predictions.DismissedPredictionAppsStore
import app.lawnchair.ui.OverflowMenuGrouped
import app.lawnchair.ui.preferences.LocalIsExpandedScreen
import app.lawnchair.ui.preferences.components.AppItem
import app.lawnchair.ui.preferences.components.AppItemPlaceholder
import app.lawnchair.ui.preferences.components.layout.PreferenceLazyColumn
import app.lawnchair.ui.preferences.components.layout.PreferenceScaffold
import app.lawnchair.ui.preferences.components.layout.preferenceGroupItems
import app.lawnchair.util.App
import app.lawnchair.util.appComparator
import app.lawnchair.util.appsState
import com.android.launcher3.R
import java.util.Comparator.comparing

@Composable
fun DismissedPredictionAppsPreferences(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val storePrefs = remember { AppUsageStore.getPrefs(context) }
    val dismissedAppsStore = remember {
        DismissedPredictionAppsStore(storePrefs, DismissedPredictionAppsStore.DISMISS_STORE_NAME)
    }
    var dismissedApps by remember {
        mutableStateOf(dismissedAppsStore.getDismissedApps())
    }
    val pageTitle = stringResource(
        if (dismissedApps.isEmpty()) {
            R.string.dismissed_prediction_apps_label
        } else {
             R.string.dismissed_prediction_apps_label_with_count, dismissedApps.size
        }
    )
    val apps by appsState(comparator = dismissedPredictionAppsComparator(context, dismissedApps))
    val state = rememberLazyListState()

    DisposableEffect(storePrefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == DismissedPredictionAppsStore.DISMISS_STORE_NAME) {
                dismissedApps = dismissedAppsStore.getDismissedApps()
            }
        }
        storePrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { storePrefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    PreferenceScaffold(
        label = pageTitle,
        actions = {
            if (dismissedApps.isNotEmpty()) {
                ResetDismissedAppsAction(onReset = {
                    dismissedAppsStore.setDismissedApps(emptySet())
                })
            }
        },
        modifier = modifier,
        isExpandedScreen = LocalIsExpandedScreen.current,
    ) {
        Crossfade(targetState = apps.isNotEmpty(), label = "") { present ->
            if (present) {
                PreferenceLazyColumn(it, state = state) {
                    val toggleDismissedApp = { app: App ->
                        val key = DismissedPredictionAppsStore.toStoreKey(
                            context = context,
                            componentName = app.key.componentName,
                            user = app.key.user,
                        )
                        val newSet = if (!dismissedApps.contains(key)) {
                            dismissedApps + key
                        } else {
                            dismissedApps - key
                        }
                        dismissedAppsStore.setDismissedApps(newSet)
                    }
                    preferenceGroupItems(
                        items = apps,
                        isFirstChild = true,
                    ) { _, app ->
                        val dismissedKey = DismissedPredictionAppsStore.toStoreKey(
                            context = context,
                            componentName = app.key.componentName,
                            user = app.key.user,
                        )
                        AppItem(
                            app = app,
                            onClick = toggleDismissedApp,
                        ) {
                            Checkbox(
                                checked = dismissedApps.contains(dismissedKey),
                                onCheckedChange = null,
                            )
                        }
                    }
                }
            } else {
                PreferenceLazyColumn(it, enabled = false) {
                    preferenceGroupItems(
                        count = 20,
                        isFirstChild = true,
                    ) {
                        AppItemPlaceholder {
                            Spacer(Modifier.width(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ResetDismissedAppsAction(
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OverflowMenuGrouped(modifier) {
        DropdownMenuGroup(
            shapes = MenuDefaults.groupShape(0, 1),
        ) {
            DropdownMenuItem(
                onClick = {
                    onReset()
                    hideMenu()
                },
                text = {
                    Text(stringResource(R.string.action_reset))
                },
            )
        }
    }
}

@Composable
private fun dismissedPredictionAppsComparator(context: Context, dismissedApps: Set<String>) = remember(context, dismissedApps) {
    comparing<App, Int> {
        if (DismissedPredictionAppsStore.toStoreKey(
                context = context,
                componentName = it.key.componentName,
                user = it.key.user,
            ) in dismissedApps
        ) {
            0
        } else {
            1
        }
    }.then(appComparator)
}
