package app.lawnchair.predictions

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.content.edit
import app.lawnchair.util.isPackageInstalled

class AppUsageStore(
    private val prefs: SharedPreferences,
    private val storeName: String,
    private val maxSize: Int = DEFAULT_MAX_SIZE,
) {
    private var list: MutableList<String> = load()

    fun record(key: String) {
        list.add(0, key)
        if (list.size > maxSize) {
            list = list.take(maxSize).toMutableList()
        }
        save()
    }

    fun getRanked(): List<String> = list.filter { it.isNotEmpty() }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .map { it.key }

    fun pruneUninstalled(pm: PackageManager) {
        val changed = list.removeAll { key ->
            if (key.isEmpty()) return@removeAll true
            !pm.isPackageInstalled(PredictionAppKey.packageName(key))
        }
        if (changed) save()
    }

    private fun load(): MutableList<String> {
        val storedEntries = prefs.getString(storeName, "").orEmpty()
        return storedEntries
            .split(HISTORY_DELIMITER)
            .filter(String::isNotEmpty)
            .toMutableList()
    }

    private fun save() {
        prefs.edit { putString(storeName, list.joinToString(HISTORY_DELIMITER)) }
    }

    companion object {
        private const val HISTORY_DELIMITER = ";"
        private const val PREFS_FILE = "lawnchair_prediction_usage"

        const val HOTSEAT_STORE_NAME = "hotseat_lc_usage"

        const val ALL_APPS_STORE_NAME = "allapps_lc_usage"

        const val DEFAULT_MAX_SIZE = 250

        fun getPrefs(context: Context): SharedPreferences = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
    }
}
