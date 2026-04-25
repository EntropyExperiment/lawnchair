package app.lawnchair.icons

import android.content.Context
import android.util.Log
import app.lawnchair.icons.shape.IconShape
import app.lawnchair.icons.shape.PathShapeDelegate
import app.lawnchair.preferences.PreferenceChangeListener
import app.lawnchair.preferences.PreferenceManager
import app.lawnchair.preferences2.PreferenceManager2
import com.android.launcher3.LauncherPrefs
import com.android.launcher3.concurrent.annotations.Ui
import com.android.launcher3.dagger.ApplicationContext
import com.android.launcher3.dagger.LauncherAppSingleton
import com.android.launcher3.graphics.ThemeManager
import com.android.launcher3.util.DaggerSingletonTracker
import com.android.launcher3.util.LooperExecutor
import com.patrykmichalik.opto.core.firstBlocking
import javax.inject.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

@LauncherAppSingleton
class LawnchairThemeManager
@Inject
constructor(
    @ApplicationContext private val context: Context,
    @Ui private val uiExecutor: LooperExecutor,
    private val prefs: LauncherPrefs,
    private val iconControllerFactory: IconControllerFactory,
    private val lifecycle: DaggerSingletonTracker,
    private val prefs2: PreferenceManager2,
    private val prefs1: PreferenceManager,
) : ThemeManager(
    context,
    uiExecutor,
    prefs,
    iconControllerFactory,
    lifecycle,
) {
    override var iconState = parseIconStateV2(null)

    private val prefListener = PreferenceChangeListener { verifyIconState() }

    init {
        val scope = MainScope()
        merge(
            prefs2.iconShape.get(),
            prefs2.customIconShape.get(),
        ).onEach { verifyIconState() }
            .launchIn(scope)

        prefs1.wrapAdaptiveIcons.addListener(prefListener)
        prefs1.transparentIconBackground.addListener(prefListener)
        prefs1.shadowBGIcons.addListener(prefListener)
        prefs1.coloredBackgroundLightness.addListener(prefListener)
        prefs1.forceIconMonochrome.addListener(prefListener)

        lifecycle.addCloseable {
            scope.cancel()
            prefs1.wrapAdaptiveIcons.removeListener(prefListener)
            prefs1.transparentIconBackground.removeListener(prefListener)
            prefs1.shadowBGIcons.removeListener(prefListener)
            prefs1.coloredBackgroundLightness.removeListener(prefListener)
            prefs1.forceIconMonochrome.removeListener(prefListener)
        }
    }

    override fun verifyIconState() {
        val newState = parseIconStateV2(iconState)
        if (newState == iconState) return
        iconState = newState

        listeners.forEach { it.onThemeChanged() }
    }

    private fun parseIconStateV2(oldState: IconState?): IconState {
        val currentAppShape: IconShape = try {
            prefs2.iconShape.firstBlocking()
        } catch (e: Exception) {
            Log.d(TAG, "Error getting icon shape", e)
            IconShape.Circle
        }

        val currentFolderShape: IconShape = try {
            prefs2.folderShape.firstBlocking()
        } catch (e: Exception) {
            Log.d(TAG, "Error getting icon shape", e)
            IconShape.Circle
        }

        var appShapeKey = currentAppShape.getHashString()
        var folderShapeKey = currentFolderShape.getHashString()

        val prefSuffix = "${prefs1.wrapAdaptiveIcons.get()},${prefs1.transparentIconBackground.get()},${prefs1.shadowBGIcons.get()},${prefs1.coloredBackgroundLightness.get()},${prefs1.forceIconMonochrome.get()}"
        appShapeKey += prefSuffix
        folderShapeKey += prefSuffix

        val appShape =
            if (oldState != null && oldState.iconMask == appShapeKey) {
                oldState.iconShape
            } else {
                PathShapeDelegate(currentAppShape)
            }

        val folderShape =
            if (oldState != null && oldState.iconMask == folderShapeKey) {
                oldState.iconShape
            } else {
                PathShapeDelegate(currentFolderShape)
            }

        return IconState(
            iconMask = appShapeKey,
            folderRadius = 1f,
            shapeRadius = 1f,
            themeController = iconControllerFactory.createThemeController(),
            iconShape = appShape,
            folderShape = folderShape,
        )
    }
}

private const val TAG = "LawnchairThemeManager"
