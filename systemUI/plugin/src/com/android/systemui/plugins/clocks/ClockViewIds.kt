/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.plugins.clocks

import android.view.View

/**
 * Defines several view ids which are useful for sharing views between the host process and the
 * client. Normally these would be defined in ids.xml, but android_library is incapable of being
 * dynamically referenced by the plugin apks. This approach means the identifiers are no longer
 * compile-time constants, but is preferable as it eliminates our need to look them up them by name.
 */
object ClockViewIds {
    val LOCKSCREEN_CLOCK_VIEW_LARGE = View.generateViewId()
    val LOCKSCREEN_CLOCK_VIEW_SMALL = View.generateViewId()

    // View ids for different digit views
    val HOUR_DIGIT_PAIR = View.generateViewId()
    val MINUTE_DIGIT_PAIR = View.generateViewId()
    val HOUR_FIRST_DIGIT = View.generateViewId()
    val HOUR_SECOND_DIGIT = View.generateViewId()
    val MINUTE_FIRST_DIGIT = View.generateViewId()
    val MINUTE_SECOND_DIGIT = View.generateViewId()
    val TIME_FULL_FORMAT = View.generateViewId()
    val DATE_FORMAT = View.generateViewId()

    // View ids for elements in large weather clock
    // TODO(b/364680879): Move these to the weather clock apk w/ WeatherClockSection
    val WEATHER_CLOCK_TIME = View.generateViewId()
    val WEATHER_CLOCK_DATE = View.generateViewId()
    val WEATHER_CLOCK_ICON = View.generateViewId()
    val WEATHER_CLOCK_TEMP = View.generateViewId()
    val WEATHER_CLOCK_ALARM_DND = View.generateViewId()
    val WEATHER_CLOCK_DATE_BARRIER_BOTTOM = View.generateViewId()
}
