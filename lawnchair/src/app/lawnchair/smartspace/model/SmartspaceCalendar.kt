package app.lawnchair.smartspace.model

import androidx.annotation.StringRes
import com.android.launcher3.R

/**
 * Contains the data of a single calendar system for smartspace.
 *
 * @param formatCustomizationSupport If the calendar system supports date & time format customization.
 */
sealed class SmartspaceCalendar(@StringRes val nameResourceId: Int, val formatCustomizationSupport: Boolean = true) {

    companion object {

        fun fromString(value: String): SmartspaceCalendar = when (value) {
            "persian" -> Persian
            "chinese" -> Chinese
            else -> Gregorian
        }

        /**
         * @return The list of all calendars.
         */
        fun values() = listOf(Gregorian, Persian, Chinese)
    }

    object Gregorian : SmartspaceCalendar(nameResourceId = R.string.smartspace_calendar_gregorian) {
        override fun toString() = "gregorian"
    }
    object Persian : SmartspaceCalendar(nameResourceId = R.string.smartspace_calendar_persian) {
        // Officially known as Solar Hijri
        override fun toString() = "persian"
    }

    object Chinese : SmartspaceCalendar(nameResourceId = R.string.smartspace_calendar_chinese) {
        override fun toString() = "chinese"
    }
}
