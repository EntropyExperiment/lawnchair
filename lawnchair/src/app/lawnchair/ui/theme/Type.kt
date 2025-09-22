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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.android.launcher3.R

private val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semi_bold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

private val OpenRundeDisplayFontFamily = FontFamily(
    Font(R.font.openrunde_medium, FontWeight.Medium),
    Font(R.font.openrunde_semibold, FontWeight.SemiBold),
)

private val OpenRundeFontFamily = FontFamily(
    Font(R.font.openrunde_regular, FontWeight.Normal),
    Font(R.font.openrunde_medium, FontWeight.Medium),
    Font(R.font.openrunde_semibold, FontWeight.SemiBold),
    Font(R.font.openrunde_bold, FontWeight.Bold),
)

private val base = Typography()
val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = OpenRundeDisplayFontFamily),
    displayMedium = base.displayMedium.copy(fontFamily = OpenRundeDisplayFontFamily),
    displaySmall = base.displaySmall.copy(fontFamily = OpenRundeDisplayFontFamily),
    headlineLarge = base.headlineLarge.copy(fontFamily = OpenRundeDisplayFontFamily),
    headlineMedium = base.headlineMedium.copy(fontFamily = OpenRundeDisplayFontFamily),
    headlineSmall = base.headlineSmall.copy(fontFamily = OpenRundeDisplayFontFamily),
    titleLarge = base.titleLarge.copy(fontFamily = OpenRundeDisplayFontFamily),
    titleMedium = base.titleMedium.copy(fontFamily = OpenRundeFontFamily),
    titleSmall = base.titleSmall.copy(fontFamily = OpenRundeFontFamily),
    bodyLarge = base.bodyLarge.copy(fontFamily = OpenRundeFontFamily),
    bodyMedium = base.bodyMedium.copy(fontFamily = OpenRundeFontFamily),
    bodySmall = base.bodySmall.copy(fontFamily = OpenRundeFontFamily),
    labelLarge = base.labelLarge.copy(fontFamily = OpenRundeFontFamily),
    labelMedium = base.labelMedium.copy(fontFamily = OpenRundeFontFamily),
    labelSmall = base.labelSmall.copy(fontFamily = OpenRundeFontFamily),
)
