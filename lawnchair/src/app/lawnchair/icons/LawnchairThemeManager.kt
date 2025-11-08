package app.lawnchair.icons

import android.content.Context
import android.util.Log
import app.lawnchair.icons.shape.IconShape
import app.lawnchair.icons.shape.PathShapeDelegate
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
) : ThemeManager(
    context,
    uiExecutor,
    prefs,
    iconControllerFactory,
    lifecycle,
) {
    override var iconState = parseIconStateV2(null)

    override fun verifyIconState() {
        val newState = parseIconStateV2(iconState)
        if (newState == iconState) return
        iconState = newState

        listeners.forEach { it.onThemeChanged() }
    }

    private fun parseIconStateV2(oldState: IconState?): IconState {
        val currentShape: IconShape = try {
            prefs2.iconShape.firstBlocking()
        } catch (e: Exception) {
            Log.d(TAG, "Error getting icon shape", e)
            IconShape.Circle
        }

        val shapePath = currentShape.getMaskPath()
        val shapeKey = currentShape.getHashString()

        val iconShape =
            if (oldState != null && oldState.iconMask == shapeKey) {
                oldState.iconShape
            } else {
                PathShapeDelegate(shapePath)
            }

        return IconState(
            iconMask = shapeKey,
            folderRadius = 1f,
            shapeRadius = 1f,
            themeController = iconControllerFactory.createThemeController(),
            iconShape = iconShape,
            folderShape = iconShape,
        )
    }
}

private const val TAG = "LawnchairThemeManager"
