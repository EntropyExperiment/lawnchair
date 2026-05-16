package app.lawnchair.predictions

import android.content.ComponentName

object PredictionAppKey {
    private const val DELIMITER = "/"

    data class TargetInfo(
        val packageName: String,
        val className: String,
        val userToken: String,
    )

    fun create(componentName: ComponentName, userToken: String): String = "${componentName.packageName}$DELIMITER${componentName.className}$DELIMITER$userToken"

    fun create(componentName: ComponentName, userSerial: Long): String = create(componentName, userSerial.toString())

    fun packageName(key: String): String = key.substringBefore(DELIMITER)

    fun parse(key: String): TargetInfo? {
        val packageNameEnd = key.indexOf(DELIMITER)
        val userTokenStart = key.lastIndexOf(DELIMITER)
        if (packageNameEnd <= 0 || userTokenStart <= packageNameEnd + 1 || userTokenStart >= key.lastIndex) {
            return null
        }

        return TargetInfo(
            packageName = key.substring(0, packageNameEnd),
            className = key.substring(packageNameEnd + 1, userTokenStart),
            userToken = key.substring(userTokenStart + 1),
        )
    }
}
