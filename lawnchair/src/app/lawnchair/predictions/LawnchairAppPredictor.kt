package app.lawnchair.predictions

import android.Manifest
import android.app.prediction.AppTarget
import android.app.prediction.AppTargetId
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Process
import android.os.UserHandle
import androidx.annotation.WorkerThread
import app.lawnchair.preferences2.PreferenceManager2
import com.android.launcher3.AppFilter
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.LauncherModel
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_HOTSEAT
import com.android.launcher3.logger.LauncherAtom
import com.android.launcher3.logging.StatsLogManager.EventEnum
import com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_APP_LAUNCH_DRAGDROP
import com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_APP_LAUNCH_TAP
import com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_DISMISS_PREDICTION_UNDO
import com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_FOLDER_CONVERTED_TO_ICON
import com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_HOTSEAT_PREDICTION_PINNED
import com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_ITEM_DROPPED_ON_DONT_SUGGEST
import com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_ITEM_DROPPED_ON_REMOVE
import com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_ITEM_DROP_COMPLETED
import com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_ITEM_DROP_FOLDER_CREATED
import com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_QUICKSWITCH_LEFT
import com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_QUICKSWITCH_RIGHT
import com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_SYSTEM_SHORTCUT_DONT_SUGGEST_APP_TAP
import com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_TASK_LAUNCH_SWIPE_DOWN
import com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_TASK_LAUNCH_TAP
import com.android.launcher3.logging.StatsLogManager.LauncherEvent.LAUNCHER_WIDGET_ADD_BUTTON_TAP
import com.android.launcher3.model.BgDataModel
import com.android.launcher3.model.PredictionUpdateTask
import com.android.launcher3.model.PredictorState
import com.android.launcher3.model.WidgetItem
import com.android.launcher3.model.WidgetsPredictionUpdateTask
import com.android.launcher3.pm.UserCache
import com.android.launcher3.util.Executors.MODEL_EXECUTOR
import com.android.launcher3.util.UserIconInfo
import com.android.quickstep.logging.StatsLogCompatManager
import com.android.systemui.shared.system.SysUiStatsLog
import com.patrykmichalik.opto.core.firstBlocking
import java.util.ArrayList
import java.util.HashSet
import java.util.LinkedHashMap
import java.util.concurrent.TimeUnit

/**
 * Listens to app launching] events from [StatsLogCompatManager.StatsLogConsumer] and send
 * them to [AppUsageStore] depending on where the app is launched from.
 *
 * Call [register] to start receiving events, and [unregister] when the ModelDelegate is destroyed.
 */
class LawnchairAppPredictor(private val context: Context) : StatsLogCompatManager.StatsLogConsumer {

    private val usagePrefs: SharedPreferences = AppUsageStore.getPrefs(context)
    private val userCache = UserCache.INSTANCE.get(context)

    private val dismissedAppsStore = DismissedPredictionAppsStore(
        prefs = usagePrefs,
        storeName = DismissedPredictionAppsStore.DISMISS_STORE_NAME,
    )

    private val hotseatStore = AppUsageStore(
        prefs = usagePrefs,
        storeName = AppUsageStore.HOTSEAT_STORE_NAME,
    )
    private val allAppsStore = AppUsageStore(
        prefs = usagePrefs,
        storeName = AppUsageStore.ALL_APPS_STORE_NAME,
    )
    private val prefs2: PreferenceManager2 by lazy { PreferenceManager2.getInstance(context) }
    private var dismissedApps: MutableSet<String> = loadDismissedApps()

    private var activeModel: LauncherModel? = null
    private var activeDataModel: BgDataModel? = null
    private var activeAllAppsState: PredictorState? = null
    private var activeHotseatState: PredictorState? = null
    private var activeWidgetsState: PredictorState? = null
    private var activeIdp: InvariantDeviceProfile? = null

    fun register(
        model: LauncherModel,
        dataModel: BgDataModel,
        allAppsState: PredictorState,
        hotseatState: PredictorState,
        widgetsState: PredictorState,
        idp: InvariantDeviceProfile,
    ) {
        activeModel = model
        activeDataModel = dataModel
        activeAllAppsState = allAppsState
        activeHotseatState = hotseatState
        activeWidgetsState = widgetsState
        activeIdp = idp
        StatsLogCompatManager.LOGS_CONSUMER.add(this)
    }

    fun unregister() {
        StatsLogCompatManager.LOGS_CONSUMER.remove(this)
        activeModel = null
        activeDataModel = null
        activeAllAppsState = null
        activeHotseatState = null
        activeWidgetsState = null
        activeIdp = null
    }

    @WorkerThread
    override fun consume(event: EventEnum?, atomInfo: LauncherAtom.ItemInfo?) {
        MODEL_EXECUTOR.execute { handleEvent(event, atomInfo) }
    }

    @WorkerThread
    private fun handleEvent(event: EventEnum?, atomInfo: LauncherAtom.ItemInfo?) {
        val resolvedEvent = atomInfo?.let {
            ResolvedEvent(
                location = resolveLocation(it),
                componentName = resolveComponentName(it),
                user = resolveUser(it),
            )
        }

        val didRecord = when {
            event == null || resolvedEvent == null -> false
            isDismissEvent(event) -> recordDismissEvent(event, resolvedEvent)
            isLaunchEvent(event) -> recordLaunchEvent(resolvedEvent)
            else -> false
        }

        if (didRecord || isLayoutMutationEvent(event)) {
            activePredictionState()?.let { state ->
                updates(
                    model = state.model,
                    dataModel = state.dataModel,
                    allAppsState = state.allAppsState,
                    hotseatState = state.hotseatState,
                    widgetsState = state.widgetsState,
                    idp = state.idp,
                )
            }
        }
    }

    @WorkerThread
    fun updates(
        model: LauncherModel,
        dataModel: BgDataModel,
        allAppsState: PredictorState,
        hotseatState: PredictorState,
        widgetsState: PredictorState,
        idp: InvariantDeviceProfile,
    ) {
        val pm = context.packageManager
        prunePredictionStores(pm)

        val hotseatRanked = hotseatStore.getRanked()
        val allAppsRanked = allAppsStore.getRanked()
        val fallbackRanked = getFallbackRanked()
        val allAppsCandidateRanked = mergeRanked(allAppsRanked, hotseatRanked, fallbackRanked)
        val hotseatCandidateRanked = mergeRanked(hotseatRanked, allAppsRanked, fallbackRanked)
        val widgetCandidateRanked = mergeRanked(hotseatRanked, allAppsRanked, fallbackRanked)
        val currentDismissedApps = dismissedApps.toSet()
        val occupiedHotseatItems = getOccupiedHotseatItems(dataModel)
        val excludedHotseatItems = occupiedHotseatItems + currentDismissedApps

        val allAppsTargets = buildTargets(
            allAppsCandidateRanked,
            idp.numDatabaseAllAppsColumns,
            currentDismissedApps,
        )
        val hotseatTargets = buildTargets(
            hotseatCandidateRanked,
            idp.numDatabaseHotseatIcons,
            excludedHotseatItems,
        )
        val widgetTargets = buildWidgetTargets(
            widgetCandidateRanked,
            dataModel,
        )

        model.enqueueModelUpdateTask(PredictionUpdateTask(allAppsState, allAppsTargets))
        model.enqueueModelUpdateTask(PredictionUpdateTask(hotseatState, hotseatTargets))
        model.enqueueModelUpdateTask(WidgetsPredictionUpdateTask(widgetsState, widgetTargets))
    }

    @WorkerThread
    fun empty(
        model: LauncherModel,
        allAppsState: PredictorState,
        hotseatState: PredictorState,
        widgetsState: PredictorState,
    ) {
        model.enqueueModelUpdateTask(PredictionUpdateTask(allAppsState, emptyList()))
        model.enqueueModelUpdateTask(PredictionUpdateTask(hotseatState, emptyList()))
        model.enqueueModelUpdateTask(WidgetsPredictionUpdateTask(widgetsState, emptyList()))
    }

    private fun buildTargets(
        ranked: List<String>,
        count: Int,
        excludedKeys: Set<String> = emptySet(),
    ): List<AppTarget> {
        val launcherApps = context.getSystemService(LauncherApps::class.java)
        return buildList(count) {
            ranked.forEach { key ->
                if (size == count) return@buildList

                val parsedKey = parseStoreKey(key) ?: return@forEach
                val componentName = ComponentName(parsedKey.packageName, parsedKey.className)
                val storeKey = toStoreKey(componentName, parsedKey.user)
                if (storeKey in excludedKeys) return@forEach

                try {
                    launcherApps?.resolveActivity(
                        android.content.Intent().setComponent(componentName),
                        parsedKey.user,
                    ) ?: return@forEach
                } catch (_: Exception) {
                    return@forEach
                }

                add(
                    createAppTarget(
                        prefix = "app",
                        componentName = componentName,
                        packageName = parsedKey.packageName,
                        user = parsedKey.user,
                    ),
                )
            }
        }
    }

    private fun getFallbackRanked(): List<String> {
        val usageStatsRanked = if (shouldUseWeightedUsageStats()) getUsageStatsRanked() else emptyList()
        val randomRanked = getRandomRanked()
        return mergeRanked(usageStatsRanked, randomRanked)
    }

    private fun shouldUseWeightedUsageStats(): Boolean {
        if (!prefs2.lawnchairPredictorUseWeightedUsageStats.firstBlocking()) return false
        return context.checkSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun getUsageStatsRanked(): List<String> {
        val usageStatsManager = context.getSystemService(UsageStatsManager::class.java) ?: return emptyList()
        val launcherApps = context.getSystemService(LauncherApps::class.java) ?: return emptyList()
        val appFilter = AppFilter(context)
        val now = System.currentTimeMillis()
        val scores = LinkedHashMap<String, Double>()

        try {
            addUsageStatsWindow(
                scores = scores,
                stats = usageStatsManager.queryAndAggregateUsageStats(now - RECENT_USAGE_WINDOW_MS, now),
                now = now,
                launchWeight = 12.0,
                foregroundMinutesWeight = 0.25,
                recencyWeight = 4.0,
            )
            addUsageStatsWindow(
                scores = scores,
                stats = usageStatsManager.queryAndAggregateUsageStats(now - DAILY_USAGE_WINDOW_MS, now),
                now = now,
                launchWeight = 4.0,
                foregroundMinutesWeight = 0.1,
                recencyWeight = 1.5,
            )
            addUsageStatsWindow(
                scores = scores,
                stats = usageStatsManager.queryAndAggregateUsageStats(now - WEEKLY_USAGE_WINDOW_MS, now),
                now = now,
                launchWeight = 1.0,
                foregroundMinutesWeight = 0.02,
                recencyWeight = 0.5,
            )
        } catch (_: Exception) {
            return emptyList()
        }

        if (scores.isEmpty()) return emptyList()

        return scores.entries
            .sortedByDescending { it.value }
            .mapNotNull { (packageName, _) -> resolvePackageToStoreKey(packageName, launcherApps, appFilter) }
    }

    private fun addUsageStatsWindow(
        scores: MutableMap<String, Double>,
        stats: Map<String, UsageStats>,
        now: Long,
        launchWeight: Double,
        foregroundMinutesWeight: Double,
        recencyWeight: Double,
    ) {
        stats.values.forEach { stat ->
            val packageName = stat.packageName
            if (packageName.isNullOrEmpty() || packageName == context.packageName) return@forEach

            val foregroundMinutes = TimeUnit.MILLISECONDS.toMinutes(stat.totalTimeInForeground).toDouble()
            val recencyScore = when {
                stat.lastTimeUsed <= 0L -> 0.0

                else -> {
                    val ageHours = (now - stat.lastTimeUsed).coerceAtLeast(0L).toDouble() / RECENCY_HOUR_MS
                    1.0 / (1.0 + ageHours)
                }
            }

            val score =
                launchWeight +
                    (foregroundMinutes * foregroundMinutesWeight) +
                    (recencyScore * recencyWeight)
            if (score <= 0.0) return@forEach

            scores[packageName] = (scores[packageName] ?: 0.0) + score
        }
    }

    private fun getRandomRanked(): List<String> {
        val launcherApps = context.getSystemService(LauncherApps::class.java) ?: return emptyList()
        val appFilter = AppFilter(context)
        return userProfilesInPredictionOrder()
            .asSequence()
            .flatMap { user -> launcherApps.getActivityList(null, user).asSequence() }
            .filter { activityInfo -> appFilter.shouldShowApp(activityInfo.componentName) }
            .map { activityInfo -> toStoreKey(activityInfo.componentName, activityInfo.user) }
            .distinct()
            .toList()
            .shuffled()
    }

    private fun resolvePackageToStoreKey(
        packageName: String,
        launcherApps: LauncherApps,
        appFilter: AppFilter,
    ): String? {
        userProfilesInPredictionOrder().forEach { user ->
            val activityInfo = launcherApps.getActivityList(packageName, user)
                .firstOrNull { info -> appFilter.shouldShowApp(info.componentName) }
                ?: return@forEach
            return toStoreKey(activityInfo.componentName, activityInfo.user)
        }
        return null
    }

    private fun userProfilesInPredictionOrder(): List<UserHandle> {
        val currentUser = Process.myUserHandle()
        return buildList {
            if (currentUser in userCache.userProfiles) add(currentUser)
            userCache.userProfiles.forEach { user ->
                if (user !in this) add(user)
            }
        }
    }

    private fun buildWidgetTargets(
        ranked: List<String>,
        dataModel: BgDataModel,
    ): List<AppTarget> {
        val widgetsByPackageAndUser = LinkedHashMap<String, MutableList<WidgetItem>>()
        dataModel.widgetsModel.widgetsByComponentKeyForPicker.values.forEach { widgetItem ->
            if (widgetItem.widgetInfo == null) return@forEach
            val key = packageUserKey(widgetItem.componentName.packageName, widgetItem.user)
            widgetsByPackageAndUser.getOrPut(key) { ArrayList() }.add(widgetItem)
        }

        val targets = ArrayList<AppTarget>(NUM_WIDGET_SUGGESTIONS)
        val addedComponents = HashSet<String>()
        for (rankedKey in ranked) {
            val parsedKey = parseStoreKey(rankedKey) ?: continue
            val packageName = parsedKey.packageName
            val userToken = userToken(parsedKey.user)
            val widget = widgetsByPackageAndUser["$packageName/$userToken"]?.firstOrNull() ?: continue
            val componentKey = toStoreKey(widget.componentName, widget.user)
            if (!addedComponents.add(componentKey)) continue

            targets.add(
                createAppTarget(
                    prefix = "widget",
                    componentName = widget.componentName,
                    packageName = widget.componentName.packageName,
                    user = widget.user,
                ),
            )
            if (targets.size == NUM_WIDGET_SUGGESTIONS) break
        }
        return targets
    }

    private fun getOccupiedHotseatItems(dataModel: BgDataModel): Set<String> {
        val itemsSnapshot = synchronized(dataModel) { dataModel.itemsIdMap.copy() }
        return itemsSnapshot
            .filter { it.container == CONTAINER_HOTSEAT }
            .mapNotNull { item -> item.targetComponent?.let { component -> toStoreKey(component, item.user) } }
            .toSet()
    }

    private fun toStoreKey(componentName: ComponentName, user: UserHandle): String = PredictionAppKey.create(componentName, userToken(user))

    private fun userToken(user: UserHandle): String = userCache.getSerialNumberForUser(user).toString()

    private fun packageUserKey(packageName: String, user: UserHandle): String = "$packageName/${userToken(user)}"

    private fun prunePredictionStores(pm: PackageManager) {
        allAppsStore.pruneUninstalled(pm)
        dismissedAppsStore.pruneUninstalled(pm)
        hotseatStore.pruneUninstalled(pm)
        dismissedApps = loadDismissedApps()
    }

    private fun createAppTarget(
        prefix: String,
        componentName: ComponentName,
        packageName: String,
        user: UserHandle,
    ): AppTarget {
        val storeKey = toStoreKey(componentName, user)
        return AppTarget.Builder(
            AppTargetId("$prefix:$storeKey"),
            packageName,
            user,
        ).setClassName(componentName.className).build()
    }

    private fun recordDismissEvent(event: EventEnum, resolvedEvent: ResolvedEvent): Boolean {
        val componentName = resolvedEvent.componentName ?: return false
        val key = toStoreKey(componentName, resolvedEvent.user)
        val changed = when (event) {
            LAUNCHER_ITEM_DROPPED_ON_DONT_SUGGEST,
            LAUNCHER_SYSTEM_SHORTCUT_DONT_SUGGEST_APP_TAP,
            -> dismissedApps.add(key)

            LAUNCHER_DISMISS_PREDICTION_UNDO -> dismissedApps.remove(key)

            else -> false
        }
        if (changed) {
            saveDismissedApps(dismissedApps)
        }
        return changed
    }

    private fun recordLaunchEvent(resolvedEvent: ResolvedEvent): Boolean {
        val componentName = resolvedEvent.componentName ?: return false
        val key = toStoreKey(componentName, resolvedEvent.user)
        return when {
            resolvedEvent.location.startsWith("workspace") ||
                resolvedEvent.location.startsWith("hotseat") ||
                resolvedEvent.location.startsWith("folder") -> {
                hotseatStore.record(key)
                true
            }

            resolvedEvent.location.startsWith("all-apps") ||
                resolvedEvent.location == "predictions" -> {
                allAppsStore.record(key)
                true
            }

            resolvedEvent.location.startsWith("predictions/hotseat") -> {
                /* Launches from the predicted hotseat are not recorded by default to avoid
                 * a feedback loop that can freeze dock predictions. See
                 * [PreferenceManager2.lawnchairPredictorRecordPredictionTaps].
                 */
                if (prefs2.lawnchairPredictorRecordPredictionTaps.firstBlocking()) {
                    hotseatStore.record(key)
                }
                true
            }

            else -> false
        }
    }

    private fun activePredictionState(): ActivePredictionState? {
        val model = activeModel ?: return null
        val dataModel = activeDataModel ?: return null
        val allAppsState = activeAllAppsState ?: return null
        val hotseatState = activeHotseatState ?: return null
        val widgetsState = activeWidgetsState ?: return null
        val idp = activeIdp ?: return null
        return ActivePredictionState(model, dataModel, allAppsState, hotseatState, widgetsState, idp)
    }

    private fun parseStoreKey(key: String): ParsedStoreKey? {
        val parts = PredictionAppKey.parse(key) ?: return null
        val user = resolveUserToken(parts.userToken) ?: Process.myUserHandle()
        return ParsedStoreKey(parts.packageName, parts.className, user)
    }

    private fun resolveUserToken(token: String): UserHandle? {
        token.toLongOrNull()?.let { serialNumber ->
            val user = userCache.getUserForSerialNumber(serialNumber)
            if (user != null && userToken(user) == token) return user
        }

        return userCache.userProfiles.firstOrNull { profile -> profile.hashCode().toString() == token }
    }

    private fun loadDismissedApps(): MutableSet<String> = dismissedAppsStore.getDismissedApps().toMutableSet()

    private fun saveDismissedApps(dismissedApps: Set<String>) {
        dismissedAppsStore.setDismissedApps(dismissedApps)
    }

    private fun isLaunchEvent(event: EventEnum): Boolean = event == LAUNCHER_APP_LAUNCH_TAP ||
        event == LAUNCHER_APP_LAUNCH_DRAGDROP ||
        event == LAUNCHER_TASK_LAUNCH_SWIPE_DOWN ||
        event == LAUNCHER_TASK_LAUNCH_TAP ||
        event == LAUNCHER_QUICKSWITCH_LEFT ||
        event == LAUNCHER_QUICKSWITCH_RIGHT

    private fun isLayoutMutationEvent(event: EventEnum?): Boolean = event == LAUNCHER_ITEM_DROP_COMPLETED ||
        event == LAUNCHER_ITEM_DROP_FOLDER_CREATED ||
        event == LAUNCHER_FOLDER_CONVERTED_TO_ICON ||
        event == LAUNCHER_ITEM_DROPPED_ON_REMOVE ||
        event == LAUNCHER_HOTSEAT_PREDICTION_PINNED ||
        event == LAUNCHER_WIDGET_ADD_BUTTON_TAP

    private fun isDismissEvent(event: EventEnum): Boolean = event == LAUNCHER_ITEM_DROPPED_ON_DONT_SUGGEST ||
        event == LAUNCHER_SYSTEM_SHORTCUT_DONT_SUGGEST_APP_TAP ||
        event == LAUNCHER_DISMISS_PREDICTION_UNDO

    private fun resolveComponentName(atomInfo: LauncherAtom.ItemInfo): ComponentName? {
        return when (atomInfo.itemCase) {
            LauncherAtom.ItemInfo.ItemCase.APPLICATION -> {
                val cn = atomInfo.application.componentName
                if (cn.isNullOrEmpty()) null else ComponentName.unflattenFromString(cn)
            }

            LauncherAtom.ItemInfo.ItemCase.TASK -> {
                val cn = atomInfo.task.componentName
                if (cn.isNullOrEmpty()) null else ComponentName.unflattenFromString(cn)
            }

            else -> null
        }
    }

    private fun resolveUser(atomInfo: LauncherAtom.ItemInfo): UserHandle {
        val userType = when (atomInfo.userType) {
            SysUiStatsLog.LAUNCHER_UICHANGED__USER_TYPE__TYPE_WORK -> UserIconInfo.TYPE_WORK
            SysUiStatsLog.LAUNCHER_UICHANGED__USER_TYPE__TYPE_CLONED -> UserIconInfo.TYPE_CLONED
            SysUiStatsLog.LAUNCHER_UICHANGED__USER_TYPE__TYPE_PRIVATE -> UserIconInfo.TYPE_PRIVATE
            else -> UserIconInfo.TYPE_MAIN
        }

        return userCache.userProfiles.firstOrNull { userCache.getUserInfo(it).type == userType }
            ?: Process.myUserHandle()
    }

    private fun resolveLocation(atomInfo: LauncherAtom.ItemInfo): String {
        if (!atomInfo.hasContainerInfo()) return ""
        return when (atomInfo.containerInfo.containerCase) {
            LauncherAtom.ContainerInfo.ContainerCase.WORKSPACE -> "workspace"

            LauncherAtom.ContainerInfo.ContainerCase.HOTSEAT -> "hotseat"

            LauncherAtom.ContainerInfo.ContainerCase.FOLDER -> {
                val parent = atomInfo.containerInfo.folder.parentContainerCase
                when (parent) {
                    LauncherAtom.FolderContainer.ParentContainerCase.WORKSPACE -> "folder/workspace"
                    LauncherAtom.FolderContainer.ParentContainerCase.HOTSEAT -> "folder/hotseat"
                    else -> "folder"
                }
            }

            LauncherAtom.ContainerInfo.ContainerCase.ALL_APPS_CONTAINER -> "all-apps"

            LauncherAtom.ContainerInfo.ContainerCase.PREDICTION_CONTAINER -> "predictions"

            LauncherAtom.ContainerInfo.ContainerCase.PREDICTED_HOTSEAT_CONTAINER -> "predictions/hotseat"

            LauncherAtom.ContainerInfo.ContainerCase.TASK_SWITCHER_CONTAINER -> "task-switcher"

            LauncherAtom.ContainerInfo.ContainerCase.TASK_BAR_CONTAINER -> "taskbar"

            LauncherAtom.ContainerInfo.ContainerCase.SEARCH_RESULT_CONTAINER -> "search-results"

            else -> ""
        }
    }

    companion object {
        private const val NUM_WIDGET_SUGGESTIONS = 20
        private val RECENT_USAGE_WINDOW_MS = TimeUnit.HOURS.toMillis(6)
        private val DAILY_USAGE_WINDOW_MS = TimeUnit.DAYS.toMillis(1)
        private val WEEKLY_USAGE_WINDOW_MS = TimeUnit.DAYS.toMillis(7)
        private val RECENCY_HOUR_MS = TimeUnit.HOURS.toMillis(1).toDouble()
    }

    private fun mergeRanked(vararg rankedLists: List<String>): List<String> = buildList {
        rankedLists.forEach { ranked ->
            ranked.forEach { key ->
                if (key !in this) add(key)
            }
        }
    }

    private data class ParsedStoreKey(
        val packageName: String,
        val className: String,
        val user: UserHandle,
    )

    private data class ResolvedEvent(
        val location: String,
        val componentName: ComponentName?,
        val user: UserHandle,
    )

    private data class ActivePredictionState(
        val model: LauncherModel,
        val dataModel: BgDataModel,
        val allAppsState: PredictorState,
        val hotseatState: PredictorState,
        val widgetsState: PredictorState,
        val idp: InvariantDeviceProfile,
    )
}
