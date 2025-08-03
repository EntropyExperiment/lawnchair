package app.lawnchair.factory

import android.appwidget.AppWidgetHost
import android.content.Context
import android.widget.RemoteViews
import com.android.internal.annotations.Keep
import com.android.launcher3.config.FeatureFlags
import com.android.launcher3.widget.LauncherWidgetHolder
import java.util.function.IntConsumer

class LawnchairWidgetHolder(context: Context, hostId: Int) : LauncherWidgetHolder(context, hostId) {

    @Keep
    class LawnchairHolderFactory
    @Suppress("unused")
    constructor(
        @Suppress("UNUSED_PARAMETER") context: Context,
    ) : HolderFactory() {
        override fun newInstance(
            context: Context,
            appWidgetRemovedCallback: IntConsumer?,
        ): LauncherWidgetHolder {
            return try {
                newInstance(context, appWidgetRemovedCallback, null)
            } catch (t: Throwable) {
                super.newInstance(context, appWidgetRemovedCallback)
            }
        }

        /**
         * @param context The context of the caller
         * @param appWidgetRemovedCallback The callback that is called when widgets are removed
         * @param interactionHandler The interaction handler when the widgets are clicked
         * @return A new [LauncherWidgetHolder] instance
         */
        fun newInstance(
            context: Context,
            appWidgetRemovedCallback: Int,
            interactionHandler: RemoteViews.InteractionHandler?,
        ): LauncherWidgetHolder {
            return if (true) { // Lawnchair: Used to be ENABLE_WIDGET_HOST_IN_BACKGROUND
                object : LauncherWidgetHolder(context, appWidgetRemovedCallback) {
                    override fun createView(
                        context: Context,
                        appWidgetRemovedCallback: IntConsumer?,
                    ): AppWidgetHost {
                        val host = super.createHost(context, appWidgetRemovedCallback)
                        if (interactionHandler != null) {
                            host.setInteractionHandler(interactionHandler)
                        }
                        return host
                    }
                }
            } else {
                LawnchairWidgetHolder(context, appWidgetRemovedCallback)
            }
        }
    }
}
