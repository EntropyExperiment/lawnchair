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

import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import com.android.systemui.plugins.annotations.GeneratedImport
import com.android.systemui.plugins.annotations.ProtectedInterface
import com.android.systemui.plugins.annotations.ProtectedReturn

/** Specifies layout information for the clock face */
@ProtectedInterface
@GeneratedImport("java.util.ArrayList")
@GeneratedImport("android.view.View")
interface ClockFaceLayout {
    @get:ProtectedReturn("return new ArrayList<View>();")
    /** All clock views to add to the root constraint layout before applying constraints. */
    val views: List<View>

    @ProtectedReturn("return constraints;")
    /** Custom constraints to apply to Lockscreen ConstraintLayout. */
    fun applyConstraints(constraints: ConstraintSet): ConstraintSet

    @ProtectedReturn("return constraints;")
    /** Custom constraints to apply to the external display presentation ConstraintLayout. */
    fun applyExternalDisplayPresentationConstraints(constraints: ConstraintSet): ConstraintSet

    @ProtectedReturn("return constraints;")
    /** Custom constraints to apply to preview ConstraintLayout. */
    fun applyPreviewConstraints(
        clockPreviewConfig: ClockPreviewConfig,
        constraints: ConstraintSet,
    ): ConstraintSet

    /** Apply specified AOD BurnIn parameters to this layout */
    fun applyAodBurnIn(aodBurnInModel: AodClockBurnInModel)
}

/** Data class to contain AOD BurnIn information for correct aod rendering */
data class AodClockBurnInModel(
    /** Scale that the clock should render at to mitigate burnin */
    val scale: Float,

    /** X-Translation for the clock to mitigate burnin */
    val translationX: Float,

    /** Y-Translation for the clock to mitigate burnin */
    val translationY: Float,
)
