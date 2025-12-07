package app.lawnchair.util

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log

private const val TAG = "Compatibility"

val isOnePlusStock = checkOnePlusStock()

val isGoogle = checkGoogle()

val isSamsung = checkSamsung()

val isGestureNavContractCompatible = checkGestureNavContract()

private fun checkOnePlusStock(): Boolean = when {
    getSystemProperty("ro.rom.version", "")
        .contains(Regex("Oxygen OS|Hydrogen OS|O2_BETA|H2_BETA")) -> true

    getSystemProperty("ro.oxygen.version", "").isNotEmpty() -> true

    getSystemProperty("ro.hydrogen.version", "").isNotEmpty() -> true

    else -> false
}

private fun checkGoogle(): Boolean = when {
    Build.BRAND.contains("google", true) -> true
    Build.MANUFACTURER.contains("google", true) -> true
    Build.FINGERPRINT.contains("google", true) -> true
    else -> false
}

private fun checkSamsung(): Boolean = when {
    Build.BRAND.contains("samsung", true) -> true
    Build.MANUFACTURER.contains("samsung", true) -> true
    Build.FINGERPRINT.contains("samsung", true) -> true
    Build.MODEL.contains("SM-", true) -> true
    else -> false
}

private fun checkGestureNavContract(): Boolean = when {
    checkGoogle() -> true
    else -> false
}

fun getSystemProperty(property: String, defaultValue: String): String {
    try {
        @SuppressLint("PrivateApi")
        val value = Class.forName("android.os.SystemProperties")
            .getDeclaredMethod("get", String::class.java)
            .apply { isAccessible }
            .invoke(null, property) as String
        if (value.isNotEmpty()) {
            return value
        }
    } catch (_: Exception) {
        Log.d(TAG, "Unable to read system properties")
    }
    return defaultValue
}
