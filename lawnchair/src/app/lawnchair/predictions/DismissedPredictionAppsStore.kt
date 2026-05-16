package app.lawnchair.predictions

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.UserHandle
import androidx.core.content.edit
import app.lawnchair.util.isPackageInstalled
import com.android.launcher3.pm.UserCache

class DismissedPredictionAppsStore(
    private val prefs: SharedPreferences,
    private val storeName: String,
) {
    private var set: MutableSet<String> = load()

    fun getDismissedApps(): Set<String> = set.toSet()

    fun setDismissedApps(dismissedApps: Set<String>) {
        set = dismissedApps.filterTo(mutableSetOf()) { it.isNotEmpty() }
        save()
    }

    fun pruneUninstalled(pm: PackageManager) {
        val changed = set.removeAll { key ->
            if (key.isEmpty()) return@removeAll true
            !pm.isPackageInstalled(PredictionAppKey.packageName(key))
        }
        if (changed) save()
    }

    private fun load(): MutableSet<String> {
        return prefs.getStringSet(storeName, emptySet()).orEmpty().toMutableSet()
    }

    private fun save() {
        prefs.edit { putStringSet(storeName, set) }
    }

    companion object {
        const val DISMISS_STORE_NAME = "dismissed_apps_predictions"

        fun toStoreKey(context: Context, componentName: ComponentName, user: UserHandle): String {
            val userSerial = UserCache.INSTANCE.get(context).getSerialNumberForUser(user)
            return PredictionAppKey.create(componentName, userSerial)
        }
    }
}
