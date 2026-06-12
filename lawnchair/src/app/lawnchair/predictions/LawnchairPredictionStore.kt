package app.lawnchair.predictions

import android.content.pm.PackageManager
import app.lawnchair.util.isPackageInstalled
import com.patrykmichalik.opto.core.firstBlocking
import com.patrykmichalik.opto.core.setBlocking
import com.patrykmichalik.opto.domain.Preference

/**
 * Unified prediction store that supports both ordered (usage tracking) and
 * unordered (dismissed apps) modes. Backed by an opto [Preference] for
 * DataStore persistence.
 *
 * @param preference The opto preference used for DataStore read/write.
 * @param isOrdered When `true`, entries are recorded in insertion order
 *   (most-recent-first) and duplicates are allowed for frequency ranking.
 *   When `false`, entries form a distinct set.
 * @param maxSize Maximum number of entries stored (only enforced in ordered mode).
 */
class LawnchairPredictionStore(
    private val preference: Preference<List<String>, String, *>,
    private val isOrdered: Boolean = false,
    private val maxSize: Int = DEFAULT_MAX_SIZE,
) {
    private var cache: MutableList<String> = load()

    /**
     * Adds a key to the store.
     *
     * In ordered mode, the key is prepended (most-recent-first) and the list
     * is trimmed to [maxSize]. In unordered mode, duplicates are ignored.
     */
    fun add(key: String) {
        if (key.isEmpty()) return
        if (isOrdered) {
            cache.add(0, key)
            if (cache.size > maxSize) {
                cache = cache.take(maxSize).toMutableList()
            }
        } else {
            if (cache.contains(key)) return
            cache.add(key)
        }
        save()
    }

    /**
     * Removes a key from the store.
     * @return `true` if the key was present and removed.
     */
    fun remove(key: String): Boolean {
        val removed = cache.remove(key)
        if (removed) save()
        return removed
    }

    /**
     * Returns the current entries.
     * In unordered mode, this is the distinct set of entries.
     * In ordered mode, this is the raw list (may contain duplicates).
     */
    fun getEntries(): List<String> = cache.filter { it.isNotEmpty() }

    /**
     * Returns entries ranked by frequency (most-frequent first).
     * Only meaningful for ordered stores.
     */
    fun getRanked(): List<String> = cache
        .filter { it.isNotEmpty() }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .map { it.key }

    /**
     * Removes entries whose package is no longer installed.
     */
    fun pruneUninstalled(pm: PackageManager) {
        val changed = cache.removeAll { key ->
            if (key.isEmpty()) return@removeAll true
            !pm.isPackageInstalled(PredictionAppKey.packageName(key))
        }
        if (changed) save()
    }

    /**
     * Replaces all entries with the given set. Useful for bulk updates
     * (e.g. resetting dismissed apps from the UI).
     */
    fun setEntries(entries: Collection<String>) {
        cache = entries.filter { it.isNotEmpty() }.toMutableList()
        save()
    }

    private fun load(): MutableList<String> {
        return preference.firstBlocking()
            .filter { it.isNotEmpty() }
            .toMutableList()
    }

    private fun save() {
        preference.setBlocking(cache)
    }

    companion object {
        const val DEFAULT_MAX_SIZE = 250
    }
}
