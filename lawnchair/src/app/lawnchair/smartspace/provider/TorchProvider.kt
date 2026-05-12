package app.lawnchair.smartspace.provider

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.getSystemService
import app.lawnchair.smartspace.model.SmartspaceAction
import app.lawnchair.smartspace.model.SmartspaceScores
import app.lawnchair.smartspace.model.SmartspaceTarget
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

class TorchProvider(context: Context) :
    SmartspaceDataSource(
        context,
        R.string.smartspace_torch,
        { smartspaceTorch },
    ) {
    private val cameraManager = context.getSystemService<CameraManager>()

    override val internalTargets = torchFlow()
        .map { enabled -> listOfNotNull(if (enabled) getSmartspaceTarget() else null) }

    private fun torchFlow(): Flow<Boolean> = callbackFlow {
        val cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }

        if (cameraId == null || cameraManager == null) {
            trySend(false)
            close()
            return@callbackFlow
        }

        val callback = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(id: String, enabled: Boolean) {
                if (id == cameraId) trySend(enabled)
            }

            override fun onTorchModeUnavailable(id: String) {
                if (id == cameraId) trySend(false)
            }
        }

        cameraManager.registerTorchCallback(callback, Handler(Looper.getMainLooper()))
        awaitClose { cameraManager.unregisterTorchCallback(callback) }
    }

    class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val cameraManager = context.getSystemService<CameraManager>() ?: return
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return
            runCatching { cameraManager.setTorchMode(cameraId, false) }
        }
    }

    private fun getSmartspaceTarget(): SmartspaceTarget {
        val intent = Intent(TOGGLE_TORCH).apply {
            setPackage(context.packageName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return SmartspaceTarget(
            id = "torchStatus",
            headerAction = SmartspaceAction(
                id = "torchStatusAction",
                icon = Icon.createWithResource(context, R.drawable.ic_flashlight_off),
                title = context.getString(R.string.torch_status_on),
                subtitle = context.getString(R.string.torch_action_off),
                pendingIntent = pendingIntent,
            ),
            score = SmartspaceScores.SCORE_FLASHLIGHT,
            featureType = SmartspaceTarget.FeatureType.FEATURE_FLASHLIGHT,
        )
    }

    companion object {
        const val TOGGLE_TORCH = "${BuildConfig.APPLICATION_ID}.action.TOGGLE_TORCH"
    }
}
