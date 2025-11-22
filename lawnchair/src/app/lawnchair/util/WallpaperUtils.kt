package app.lawnchair.util

import android.app.WallpaperColors
import android.content.Context
import androidx.core.graphics.ColorUtils
import app.lawnchair.wallpaper.WallpaperColorsCompat
import app.lawnchair.wallpaper.WallpaperManagerCompat

fun isWallpaperDark(context: Context): Boolean {
    val wallpaperManager = WallpaperManagerCompat.INSTANCE.get(context)
    val colors: WallpaperColorsCompat? = wallpaperManager.wallpaperColors
    if (colors != null) {
        val hints = colors.colorHints
        if ((hints and WallpaperColors.HINT_SUPPORTS_DARK_TEXT) != 0) {
            return false
        }
        if ((hints and WallpaperColors.HINT_SUPPORTS_DARK_THEME) != 0) {
            return true
        }
    }

    val primaryColor = colors?.primaryColor ?: return false
    return ColorUtils.calculateLuminance(primaryColor) < 0.5
}
