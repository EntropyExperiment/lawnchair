package app.lawnchair

import android.content.Context
import android.content.pm.LauncherActivityInfo
import androidx.annotation.Keep
import app.lawnchair.preferences.PreferenceManager
import com.android.launcher3.icons.cache.CachingLogic
import com.android.launcher3.util.ComponentKey

@Keep
abstract class LauncherActivityCachingLogic(context: Context) : CachingLogic<LauncherActivityInfo> {
    private val prefs = PreferenceManager.getInstance(context)

    override fun getLabel(item: LauncherActivityInfo): CharSequence? {
        val key = ComponentKey(item.componentName, item.user)
        val customLabel = prefs.customAppName[key]
        if (!customLabel.isNullOrEmpty()) {
            return customLabel
        }
        return getLabel(item)
    }
}
