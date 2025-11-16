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

import com.android.systemui.plugins.annotations.ProtectedInterface

/** Methods which trigger various clock animations */
@ProtectedInterface
interface ClockAnimations {
    /** Runs an enter animation (if any) */
    fun enter()

    /** Sets how far into AOD the device currently is. */
    fun doze(fraction: Float)

    /** Sets how far into the folding animation the device is. */
    fun fold(fraction: Float)

    /** Runs the battery animation (if any). */
    fun charge()

    /** Runs when the clock's position changed during the move animation. */
    fun onPositionAnimated(anim: ClockPositionAnimationArgs)

    /**
     * Runs when swiping clock picker, swipingFraction: 1.0 -> clock is scaled up in the preview,
     * 0.0 -> clock is scaled down in the shade; previewRatio is previewSize / screenSize
     */
    fun onPickerCarouselSwiping(swipingFraction: Float)

    /** Runs when an animation when the view is tapped on the lockscreen */
    fun onFidgetTap(x: Float, y: Float)

    /** Update reactive axes for this clock */
    fun onFontAxesChanged(style: ClockAxisStyle)
}

data class ClockPositionAnimationArgs(val fromLeft: Int, val direction: Int, val fraction: Float)
