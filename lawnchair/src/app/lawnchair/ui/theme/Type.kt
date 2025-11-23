/*
 * Copyright 2021, Lawnchair
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.lawnchair.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.android.launcher3.R
import kotlin.require

/**
 * Google Sans Flex Normal (400), optimised for large text with grade axis put to max (100)
 **/
@OptIn(ExperimentalTextApi::class)
private val GoogleSansFlexDisplayNormal = FontFamily(
    Font(
        R.font.googlesansflex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
            FontVariation.grade(100),
            googleSansFlexRound(100),
        ),
    ),
)

/**
 * Google Sans Flex Normal (400), optimised for small text
 **/
@OptIn(ExperimentalTextApi::class)
private val GoogleSansFlexNormal = FontFamily(
    Font(
        R.font.googlesansflex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
            googleSansFlexRound(100),
        ),
    ),
)

/**
 * Google Sans Flex Medium (500), optimised for small/medium text
 **/
@OptIn(ExperimentalTextApi::class)
private val GoogleSansFlexMedium = FontFamily(
    Font(
        R.font.googlesansflex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Medium.weight),
            googleSansFlexRound(100),
        ),
    ),
)

/**
 * Typographic feature axis for googleSansFlexRound (ROND) variations
 *
 * [OpenType Variable Axes Definition](https://fonts.google.com/variablefonts#axis-definitions)
 *
 * @param ROND Round axis integer value, only goes from 0 to 100
 **/
private fun googleSansFlexRound(ROND: Int): FontVariation.Setting {
    val featureTagType = "ROND"

    require(ROND in 0..100) { "Google Sans Flex 'Round' axis must be in 0..100" }
    return FontVariation.Setting(featureTagType, ROND.toFloat())
}

private val base = Typography()
val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = GoogleSansFlexDisplayNormal),
    displayMedium = base.displayMedium.copy(fontFamily = GoogleSansFlexDisplayNormal),
    displaySmall = base.displaySmall.copy(fontFamily = GoogleSansFlexDisplayNormal),
    headlineLarge = base.headlineLarge.copy(fontFamily = GoogleSansFlexDisplayNormal),
    headlineMedium = base.headlineMedium.copy(fontFamily = GoogleSansFlexDisplayNormal),
    headlineSmall = base.headlineSmall.copy(fontFamily = GoogleSansFlexNormal),
    titleLarge = base.titleLarge.copy(fontFamily = GoogleSansFlexDisplayNormal),
    titleMedium = base.titleMedium.copy(fontFamily = GoogleSansFlexMedium),
    titleSmall = base.titleSmall.copy(fontFamily = GoogleSansFlexMedium),
    bodyLarge = base.bodyLarge.copy(fontFamily = GoogleSansFlexNormal, letterSpacing = 0.sp),
    bodyMedium = base.bodyMedium.copy(fontFamily = GoogleSansFlexNormal, letterSpacing = 0.1.sp),
    bodySmall = base.bodySmall.copy(fontFamily = GoogleSansFlexNormal),
    labelLarge = base.labelLarge.copy(fontFamily = GoogleSansFlexMedium),
    labelMedium = base.labelMedium.copy(fontFamily = GoogleSansFlexMedium),
    labelSmall = base.labelSmall.copy(fontFamily = GoogleSansFlexMedium),
)
