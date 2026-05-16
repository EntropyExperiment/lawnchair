package app.lawnchair.predictions

import android.content.ComponentName
import android.content.Context
import android.os.UserHandle
import androidx.core.content.edit
import app.lawnchair.util.isPackageInstalled
import com.android.launcher3.pm.UserCache

object DismissedPredictionAppsStore {
    const val STORE_NAME = "dismissed_apps_predictions"

    fun getDismissedApps(context: Context): Set<String> {
        val prefs = AppUsageStore.getPrefs(context)
        val storedApps = prefs.getStringSet(STORE_NAME, emptySet()).orEmpty()
        val prunedApps = storedApps.filterTo(mutableSetOf()) { key ->
            key.isNotEmpty() && context.packageManager.isPackageInstalled(PredictionAppKey.packageName(key))
        }
        if (prunedApps.size != storedApps.size) {
            prefs.edit { putStringSet(STORE_NAME, prunedApps) }
        }
        return prunedApps
    }

    fun setDismissedApps(context: Context, dismissedApps: Set<String>) {
        val normalizedApps = dismissedApps.filterTo(mutableSetOf()) { it.isNotEmpty() }
        AppUsageStore.getPrefs(context).edit { putStringSet(STORE_NAME, normalizedApps) }
    }

    fun toStoreKey(context: Context, componentName: ComponentName, user: UserHandle): String {
        val userSerial = UserCache.INSTANCE.get(context).getSerialNumberForUser(user)
        return PredictionAppKey.create(componentName, userSerial)
    }
}
