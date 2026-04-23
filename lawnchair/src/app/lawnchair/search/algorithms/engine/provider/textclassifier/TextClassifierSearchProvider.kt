package app.lawnchair.search.algorithms.engine.provider.textclassifier

import android.content.Context
import android.util.Log
import android.view.textclassifier.TextClassification
import android.view.textclassifier.TextClassificationManager
import app.lawnchair.preferences.PreferenceManager
import app.lawnchair.search.algorithms.engine.SearchProvider
import app.lawnchair.search.algorithms.engine.SearchResult
import com.android.launcher3.Utilities
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

object TextClassifierSearchProvider : SearchProvider {
    private const val TAG = "TextClassifierSearchProvider"

    /** Enables detailed logging, **Warning: Contain search query data** */
    private const val SENSITIVE_LOGGING = false

    private const val MIN_QUERY_LENGTH = 3

    /** Note: Most devices can classify text within less than a second, a value higher than that is being generous */
    private const val CLASSIFICATION_TIMEOUT_MS = 1500L

    override val id = "textclassifier"

    override fun search(
        context: Context,
        query: String,
    ): Flow<List<SearchResult>> = flow {
        val legacyPrefs = PreferenceManager.INSTANCE.get(context)
        if (!Utilities.ATLEAST_P || !legacyPrefs.searchResultTextClassifier.get() || query.length < MIN_QUERY_LENGTH) {
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
                val classification = withTimeoutOrNull(CLASSIFICATION_TIMEOUT_MS.milliseconds) {
                    textClassifier.classifyText(request)
                }
                if (SENSITIVE_LOGGING) {
                    Log.d(TAG, "Classification result: $classification")
                }
                if (classification?.actions?.isNotEmpty() == true) {
                    classification.actions.forEach { action ->
                        searchResults.add(
                            SearchResult.Action.TextAction(
                                title = action.title.toString(),
                                subtitle = action.contentDescription.toString(),
                                pendingIntent = action.actionIntent,
                                icon = if (action.shouldShowIcon()) action.icon else null,
                            ),
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Classification failed", e)
            }

            searchResults
        }

        emit(results)
    }
}
