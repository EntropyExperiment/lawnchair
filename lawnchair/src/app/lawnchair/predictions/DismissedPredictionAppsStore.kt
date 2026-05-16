package app.lawnchair.predictions

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.UserHandle
import androidx.core.content.edit
import com.android.launcher3.pm.UserCache

object DismissedPredictionAppsStore {
    const val STORE_NAME = "dismissed_all_apps_predictions"

    fun getDismissedApps(context: Context): Set<String> {
        val prefs = AppUsageStore.getPrefs(context)
        val storedApps = prefs.getStringSet(STORE_NAME, emptySet()).orEmpty()
        val prunedApps = storedApps.filterTo(mutableSetOf()) { key ->
            key.isNotEmpty() && isInstalled(context, key.substringBefore("/"))
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
        return "${componentName.packageName}/${componentName.className}/$userSerial"
    }

    private fun isInstalled(context: Context, packageName: String): Boolean {
        if (packageName.isEmpty()) return false
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }
}

