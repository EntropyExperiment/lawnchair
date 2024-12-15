package app.lawnchair.search.algorithms

import android.content.Context
import android.content.pm.ShortcutInfo
import android.util.Log
import app.lawnchair.launcher
import app.lawnchair.ui.preferences.components.HiddenAppsInSearch
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.popup.PopupPopulator
import com.android.launcher3.search.StringMatcherUtility
import com.android.launcher3.shortcuts.ShortcutRequest
import java.util.Locale
import me.xdrop.fuzzywuzzy.FuzzySearch
import me.xdrop.fuzzywuzzy.algorithms.WeightedRatio

class TrieNode {
    val children: MutableMap<Char, TrieNode> = mutableMapOf()
    val appInfos: MutableList<AppInfo> = mutableListOf()
    var isEndOfWord: Boolean = false
}

class Trie {
    private val root = TrieNode()

    fun insert(word: String, appInfo: AppInfo) {
        var node = root
        for (char in word.lowercase(Locale.getDefault())) {
            node = node.children.getOrPut(char) { TrieNode() }
            node.appInfos.add(appInfo)
        }
        node.isEndOfWord = true
    }

    fun searchPrefix(prefix: String): List<AppInfo> {
        var node = root
        for (char in prefix.lowercase(Locale.getDefault())) {
            if (!node.children.containsKey(char)) {
                return emptyList()
            }
            node = node.children[char]!!
        }
        return node.appInfos.distinct()
    }
}

object SearchUtils {
    /**
     * Searching using Trie algorithm with Weighted Levenshtein distance
     * When Trie is unable to find a match higher than maxResultsCount then it will fallback to
     * Levenshtein Search
     *
     * @param apps List of all apps
     * @param query Query string
     * @param maxResultsCount Maximum number of results to return
     * @param hiddenApps Set of hidden apps
     * @param hiddenAppsInSearch Whether to show hidden apps in search
     * @param levenshteinThreshold Threshold for fuzzy matching
     * @param componentSearch Whether to search for components as well as titles
     * @return List of apps that match the query
     */
    fun trieWeightedLevenshteinSearch(
        apps: List<AppInfo>,
        query: String,
        maxResultsCount: Int,
        hiddenApps: Set<String>,
        hiddenAppsInSearch: String,
        levenshteinThreshold: Float = 2.0f,
        componentSearch: Boolean = false,
    ): List<AppInfo> {
        val queryLower = query.lowercase(Locale.getDefault())

        val trie = Trie()
        for (app in apps) {
            trie.insert(app.title.toString(), app)
            if (componentSearch) {
                trie.insert(app.componentName?.flattenToString()?.lowercase(Locale.getDefault()) ?: "", app)
            }
        }

        val prefixMatches = trie.searchPrefix(queryLower)

        val filteredPrefixMatches = prefixMatches.asSequence()
            .filterHiddenApps(queryLower, hiddenApps, hiddenAppsInSearch)
            .toList()

        val sortedPrefixMatches = filteredPrefixMatches.sortedBy { app ->
            when {
                app.title.toString().lowercase(Locale.getDefault()) == queryLower -> 0
                componentSearch && app.componentName?.flattenToString()?.lowercase(Locale.getDefault()) == queryLower -> 0
                else -> 1
            }
        }

        if (sortedPrefixMatches.size >= maxResultsCount) {
            Log.v("SearchUtils", "Searching using Trie")
            return sortedPrefixMatches.take(maxResultsCount)
        }

        // If less than maxResultsCount, use Weighted Levenshtein for fuzzy matching so that we have more apps to show
        Log.v("SearchUtils", "Searching using WeightedLevenshtein")
        val remainingApps = apps.asSequence()
            .filter { !sortedPrefixMatches.contains(it) }
            .filterHiddenApps(queryLower, hiddenApps, hiddenAppsInSearch)
            .toList()

        val fuzzyMatches = remainingApps
            .map { app ->
                Pair(
                    app,
                    mobileWeightedLevenshtein(
                        queryLower,
                        if (componentSearch) app.title.toString().lowercase(Locale.getDefault())
                            + app.componentName?.flattenToString()?.lowercase(Locale.getDefault())
                        else app.title.toString().lowercase(Locale.getDefault())
                    )
                )
            }
            /* This value changes the tolerant fuzzy matching is to typos.
               the lower the threshold, the stricter the matching
                    - Aggressive matching, might not cover user typos
               the higher the threshold, the lenient the matching
                    - Loose matching, cover user typos but return less relevant */
            .filter { it.second <= levenshteinThreshold }
            .sortedBy { it.second }
            .map { it.first }

        return (sortedPrefixMatches + fuzzyMatches).take(maxResultsCount)
    }

    fun normalSearch(apps: List<AppInfo>, query: String, maxResultsCount: Int, hiddenApps: Set<String>, hiddenAppsInSearch: String): List<AppInfo> {
        // Do an intersection of the words in the query and each title, and filter out all the
        // apps that don't match all of the words in the query.
        val queryTextLower = query.lowercase(Locale.getDefault())
        val matcher = StringMatcherUtility.StringMatcher.getInstance()
        return apps.asSequence()
            .filter { StringMatcherUtility.matches(queryTextLower, it.title.toString(), matcher) }
            .filterHiddenApps(queryTextLower, hiddenApps, hiddenAppsInSearch)
            .take(maxResultsCount)
            .toList()
    }

    fun fuzzySearch(apps: List<AppInfo>, query: String, maxResultsCount: Int, hiddenApps: Set<String>, hiddenAppsInSearch: String): List<AppInfo> {
        val queryTextLower = query.lowercase(Locale.getDefault())
        val filteredApps = apps.asSequence()
            .filterHiddenApps(queryTextLower, hiddenApps, hiddenAppsInSearch)
            .toList()
        val matches = FuzzySearch.extractSorted(
            queryTextLower,
            filteredApps,
            { it.sectionName + it.title },
            WeightedRatio(),
            65,
        )

        return matches.take(maxResultsCount)
            .map { it.referent }
    }

    fun getShortcuts(app: AppInfo, context: Context): List<ShortcutInfo> {
        val shortcuts = ShortcutRequest(context.launcher, app.user)
            .withContainer(app.targetComponent)
            .query(ShortcutRequest.PUBLISHED)
        return PopupPopulator.sortAndFilterShortcuts(shortcuts)
    }

    /**
     * Calculate the weighted Levenshtein distance between the query and the title. Lower values
     * indicate a better match.
     *
     * @return The weighted Levenshtein distance between the query and the title.
     */
    private fun mobileWeightedLevenshtein(query: String, title: String): Float {
        val queryLength = query.length
        val titleLength = title.length

        if (queryLength == 0) return titleLength.toFloat()
        if (titleLength == 0) return queryLength.toFloat()

        val distanceMatrix = Array(queryLength + 1) { IntArray(titleLength + 1) }

        for (i in 0..queryLength) distanceMatrix[i][0] = i // Edit Distance of query
        for (j in 0..titleLength) distanceMatrix[0][j] = j // Edit Distance of title

        for (i in 1..queryLength) {
            for (j in 1..titleLength) {
                val cost = if (query[i - 1] == title[j - 1]) 0 else 1
                distanceMatrix[i][j] = minOf(
                    distanceMatrix[i - 1][j] + 1, // Deletion Cost
                    distanceMatrix[i][j - 1] + 1, // Insertion Cost
                    distanceMatrix[i - 1][j - 1] + cost // Substitution Cost
                )
            }
        }

        return distanceMatrix[queryLength][titleLength].toFloat()
    }
}

fun Sequence<AppInfo>.filterHiddenApps(
    query: String,
    hiddenApps: Set<String>,
    hiddenAppsInSearch: String,
): Sequence<AppInfo> {
    return when (hiddenAppsInSearch) {
        HiddenAppsInSearch.ALWAYS -> {
            this
        }
        HiddenAppsInSearch.IF_NAME_TYPED -> {
            filter {
                it.toComponentKey().toString() !in hiddenApps ||
                    it.title.toString().lowercase(Locale.getDefault()) == query
            }
        }
        else -> {
            filter { it.toComponentKey().toString() !in hiddenApps }
        }
    }
}
