package app.lawnchair.preferences2

import com.patrykmichalik.opto.core.PreferenceImpl
import com.patrykmichalik.opto.core.getFromPreferences

/**
 * Returns the current value of this preference from the in-memory cache.
 */
fun <C, S> PreferenceImpl<C, S>.firstCached(prefs2: PreferenceManager2): C {
    return getFromPreferences(prefs2.getCachedPreferences())
}
