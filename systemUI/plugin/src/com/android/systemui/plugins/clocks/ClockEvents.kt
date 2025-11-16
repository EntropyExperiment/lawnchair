/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.systemui.plugins.clocks

import android.content.Context
import android.icu.util.TimeZone
import android.text.format.DateFormat
import com.android.systemui.plugins.annotations.ProtectedInterface
import com.android.systemui.plugins.annotations.ProtectedReturn
import java.util.Locale

/** Denotes format kind that should be used when rendering the clock */
enum class TimeFormatKind {
    HALF_DAY,
    FULL_DAY;

    companion object {
        fun getFromContext(context: Context): TimeFormatKind {
            return lookup(DateFormat.is24HourFormat(context))
        }

        fun getFromContext(context: Context, userId: Int): TimeFormatKind {
            return lookup(DateFormat.is24HourFormat(context, userId))
        }

        fun lookup(is24Hr: Boolean): TimeFormatKind {
            return if (is24Hr) FULL_DAY else HALF_DAY
        }
    }
}

/** Events that should call when various rendering parameters change */
@ProtectedInterface
interface ClockEvents {
    @get:ProtectedReturn("return false;")
    /** Set to enable or disable swipe interaction */
    var isReactiveTouchInteractionEnabled: Boolean // TODO(b/364664388): Remove/Rename

    /** Call whenever timezone changes */
    fun onTimeZoneChanged(timeZone: TimeZone)

    /** Call whenever the text time format kind changes */
    fun onTimeFormatChanged(formatKind: TimeFormatKind)

    /** Call whenever the locale changes */
    fun onLocaleChanged(locale: Locale)

    /** Call whenever the weather data should update */
    fun onWeatherDataChanged(data: WeatherData)

    /** Call with alarm information */
    fun onAlarmDataChanged(data: AlarmData)

    /** Call with zen/dnd information */
    fun onZenDataChanged(data: ZenData)
}

class ClockEventListeners {
    private val listeners = mutableListOf<ClockEventListener>()

    fun attach(listener: ClockEventListener) = listeners.add(listener)

    fun detach(listener: ClockEventListener) = listeners.remove(listener)

    fun fire(func: ClockEventListener.() -> Unit) = listeners.forEach { it.func() }
}

interface ClockEventListener {
    fun onBoundsChanged(bounds: VRectF)

    fun onChangeComplete()
}
