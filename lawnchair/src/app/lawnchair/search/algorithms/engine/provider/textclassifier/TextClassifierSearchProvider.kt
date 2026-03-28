package app.lawnchair.search.algorithms.engine.provider.textclassifier

import android.content.Context
import android.util.Log
import android.view.textclassifier.TextClassification
import android.view.textclassifier.TextClassificationManager
import app.lawnchair.preferences.PreferenceManager
import app.lawnchair.search.algorithms.engine.SearchProvider
import app.lawnchair.search.algorithms.engine.SearchResult
import com.android.launcher3.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

object TextClassifierSearchProvider : SearchProvider {
    /** Enables detailed logging, **Warning: Contain search query data** */
    const val SENSITIVE_LOGGING = false

    override val id = "textclassifier"

    override fun search(
        context: Context,
        query: String,
    ): Flow<List<SearchResult>> = flow {
        val legacyPrefs = PreferenceManager.INSTANCE.get(context)
        if (!Utilities.ATLEAST_P || !legacyPrefs.searchResultTextClassifier.get() || query.isBlank()) {
            emit(emptyList())
            return@flow
        }

        val results = withContext(Dispatchers.IO) {
            val searchResults = mutableListOf<SearchResult>()
            val tcm = context.getSystemService(TextClassificationManager::class.java)
            val textClassifier = tcm?.textClassifier ?: return@withContext emptyList()

            try {
                val request = TextClassification.Request.Builder(query, 0, query.length)
                    .build()
                val classification = textClassifier.classifyText(request)
                if (SENSITIVE_LOGGING) {
                    Log.d("TextClassifierSearch", "Classification result: $classification")
                }
                if (classification.actions.isNotEmpty()) {
                    classification.actions.forEach { action ->
                        searchResults.add(
                            SearchResult.Action.TextAction(
                                title = action.title.toString(),
                                subtitle = action.contentDescription.toString(),
                                pendingIntent = action.actionIntent,
                                icon = action.icon,
                            ),
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("TextClassifierSearch", "Classification failed", e)
            }

            searchResults
        }

        emit(results)
    }
}
